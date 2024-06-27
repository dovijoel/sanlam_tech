package joel.dovi.sanlam.service;

import com.tigerbeetle.*;
import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.model.CashTransactionsToExecute;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TigerBeetleService {
    public static final int LIABILITY_CODE = 1000;
    public static final int CUSTOMER_CODE = 2000;
    public static final int LEDGER_ID = 1;
    public static final int COH_ACCOUNT_ID = 10000;
    public static final int CASH_TRANSACTION_CODE = 1001;
    private final String[] replicaAddresses;
    private final byte[] clusterId;
    private final Client client;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    public TigerBeetleService(
            @Value("${tigerbeetle.replica_address}") String replicaAddress,
            @Value("${tigerbeetle.cluster_id}") int clusterId
    ) {
        this.clusterId = UInt128.asBytes(clusterId);
        this.replicaAddresses = new String[] {replicaAddress == null ? "3000" : replicaAddress};
        this.client = getTigerBeetleClient();
    }

    private Client getTigerBeetleClient() {
        return new Client(clusterId, replicaAddresses);
    }

    public CreateAccountResultBatch initialiseTigerBeetle(List<Account> accounts) throws ConcurrencyExceededException {
        AccountBatch accountBatch = new AccountBatch(accounts.size() + 1);
        accountBatch.add();
        accountBatch.setId(COH_ACCOUNT_ID);
        accountBatch.setLedger(LEDGER_ID);
        accountBatch.setCode(LIABILITY_CODE); // liability code

        accounts.forEach(account -> {
            accountBatch.add();
            accountBatch.setId(account.getId());
            accountBatch.setLedger(LEDGER_ID);
            accountBatch.setCode(CUSTOMER_CODE); // customers code
            accountBatch.setFlags(AccountFlags.DEBITS_MUST_NOT_EXCEED_CREDITS); // account can't go into negative
        });
        try {
            return client.createAccounts(accountBatch);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public AccountBatch getTbAccounts(List<Long> ids) {
        try {
            IdBatch idBatch = new IdBatch(ids.size());
            ids.forEach(idBatch::add);
            return client.lookupAccounts(idBatch);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public BigDecimal getAccountBalance(Long accountId) {
        AccountBatch accountBatch = getTbAccounts(List.of(accountId));
        accountBatch.next(); // assuming one account
        BigInteger creditsPosted = accountBatch.getCreditsPosted();
        BigInteger debitsPosted = accountBatch.getDebitsPosted();
        BigInteger balance = accountBatch.getCode() == CUSTOMER_CODE ?
                creditsPosted.subtract(debitsPosted) :
                debitsPosted.subtract(creditsPosted);
        return (new BigDecimal(balance)).setScale(2, RoundingMode.UNNECESSARY).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    public void executeCashTransactions(List<CashTransactionsToExecute> transactions) {
        try {
            HashMap<Long, BigInteger> ids = new HashMap<>();
            TransferBatch transferBatch = new TransferBatch(transactions.size());
            for (var transaction: transactions
                 ) {
                byte[] id = UInt128.id();
                transaction.setTigerbeetleId(UInt128.asBigInteger(id));
                transferBatch.add();
                transferBatch.setId(UInt128.id());
                transferBatch.setAmount(transaction.getAmount().abs().multiply(BigDecimal.valueOf(100)).toBigInteger());
                if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    transferBatch.setDebitAccountId(transaction.getAccountId());
                    transferBatch.setCreditAccountId(COH_ACCOUNT_ID);
                } else {
                    transferBatch.setDebitAccountId(COH_ACCOUNT_ID);
                    transferBatch.setCreditAccountId(transaction.getAccountId());
                }
                transferBatch.setLedger(LEDGER_ID);
                transferBatch.setUserData64(transaction.getTransferId());
                transferBatch.setCode(CASH_TRANSACTION_CODE);
            }
            var resultBatch = client.createTransfers(transferBatch);
            boolean isEmpty = true;
            while (resultBatch.next()) { // if all are successful, result batch returned is empty
                isEmpty = false;
                var transaction = transactions.get(resultBatch.getIndex());
                log.info("Transaction {}: {}", transaction.getAccountId(), resultBatch.getResult().toString());
                transaction.setNote(resultBatch.getResult().toString());
                switch (resultBatch.getResult()) {
                    case Ok:
                        transaction.setStatus(ETransactionStatus.SUCCESSFUL);
                        break;
                    case ExceedsCredits:
                        transaction.setStatus(ETransactionStatus.INSUFFICIENT_FUNDS);
                        break;
                    default:
                        transaction.setStatus(ETransactionStatus.UNKNOWN_ERROR);
                        break;
                }
            }
            if (isEmpty) {
                for (var transaction: transactions
                     ) {
                    transaction.setStatus(ETransactionStatus.SUCCESSFUL);
                }
            }
        } catch (ConcurrencyExceededException e) {
            throw new RuntimeException(e);
        }
    }

}

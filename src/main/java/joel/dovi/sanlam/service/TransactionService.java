package joel.dovi.sanlam.service;

import com.tigerbeetle.ConcurrencyExceededException;
import com.tigerbeetle.CreateTransferResultBatch;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import joel.dovi.sanlam.model.CashTransactionsToExecute;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.model.WithdrawalEvent;
import joel.dovi.sanlam.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired SnsService snsService;

    @Autowired TigerBeetleService tigerBeetleService;

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Transactional
    public Transaction getTransaction(long transactionId) {
        return transactionRepository.findById(transactionId).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void executePendingCashTransactions() throws ConcurrencyExceededException {
        List<Transaction> transactions = transactionRepository.findTransactionsByStatusIs(ETransactionStatus.PENDING);
        if (!transactions.isEmpty()) {
            int batchSize = Math.min(transactions.size(), 5000);
            transactions = transactions.subList(0, batchSize);
            List<CashTransactionsToExecute> transactionsToExecutes = getCashTransactionsToExecutes(transactions);
            tigerBeetleService.executeCashTransactions(transactionsToExecutes);
            List<WithdrawalEvent> withdrawalEvents = new ArrayList<>();
            for (int i = 0; i < transactions.size(); i++) {
                var transaction = transactions.get(i);
                var transactionToExecute = transactionsToExecutes.get(i);
                    transaction.setStatus(transactionToExecute.getStatus());
                    transaction.setTigerBeetleId(transactionToExecute.getTigerbeetleId());
                    transaction.setNote(transactionToExecute.getNote());
                    if (transaction.getTransactionValue().compareTo(BigDecimal.ZERO) < 0) {
                        assert transaction.getAccount() != null;
                        withdrawalEvents.add(new WithdrawalEvent(transaction.getTransactionValue(), transaction.getAccount().getId(), transaction.getStatus()));
                    }
            }
            transactionRepository.saveAll(transactions);
            withdrawalEvents.forEach(event -> snsService.publish(event.toJson(), "withdrawals"));
            log.info("Processed {} transactions.", transactions.size());
        }
    }

    private List<CashTransactionsToExecute> getCashTransactionsToExecutes(List<Transaction> transactions) {
        List<CashTransactionsToExecute> transactionsToExecutes = new ArrayList<>();
        for (var transaction: transactions
        ) {
            assert transaction.getAccount() != null;
            CashTransactionsToExecute cashTransactionsToExecute = new CashTransactionsToExecute(
                    transaction.getAccount().getId(),
                    transaction.getId(),
                    transaction.getTransactionValue(),
                    null,
                    ETransactionStatus.UNKNOWN_ERROR,
                    ""
            );
            transactionsToExecutes.add(cashTransactionsToExecute);
        }
        return transactionsToExecutes;
    }
}

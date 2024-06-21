package joel.dovi.sanlam.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.repository.AccountRepository;
import joel.dovi.sanlam.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AccountService {
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    @Transactional
    public Transaction processWithdrawal(Long accountId, BigDecimal withdrawalValue) {
        Account account = accountRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        ETransactionStatus transactionStatus = null;
        StringBuilder noteSb = new StringBuilder();
        Instant now = Instant.now();

        // balance sanity check
        Transaction latestTransaction = transactionRepository.findFirstByAccountAndStatusIsOrderByTransactionTimestampDesc(account, ETransactionStatus.SUCCESSFUL);
        if (!latestTransaction.getTransactionTimestamp().equals(account.getLastUpdated())) {
            transactionStatus = ETransactionStatus.UNKNOWN_ERROR;
            noteSb.append("Account balance timestamp mismatch.").append('\n');
        } else {
            if (account.getCurrentBalance().compareTo(withdrawalValue) < 0) {
                transactionStatus = ETransactionStatus.INSUFFICIENT_FUNDS;
            } else {
                transactionStatus = ETransactionStatus.SUCCESSFUL;
                account.setCurrentBalance(account.getCurrentBalance().subtract(withdrawalValue));
                account.setLastUpdated(now);
            }
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionValue(withdrawalValue)
                .transactionTimestamp(now)
                .status(transactionStatus)
                .note(noteSb.toString())
                .build();

        transactionRepository.save(transaction);
        accountRepository.save(account);

        return transaction;

    }
}

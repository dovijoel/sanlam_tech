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
import org.springframework.beans.factory.annotation.Value;
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
    private TigerBeetleService tigerBeetleService;

    @Transactional
    public Transaction processCashTransaction(Long accountId, BigDecimal withdrawalValue) {
        Account account = accountRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        Instant now = Instant.now();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionValue(withdrawalValue)
                .transactionTimestamp(now)
                .status(ETransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);
        return transaction;
    }

    @Transactional
    public Account getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(EntityNotFoundException::new);
        account.setAccountBalance(tigerBeetleService.getAccountBalance(accountId));
        return account;
    }
}

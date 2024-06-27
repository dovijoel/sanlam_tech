package joel.dovi.sanlam.repository;

import jakarta.persistence.LockModeType;
import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findFirstByAccountAndStatusIsOrderByTransactionTimestampDesc(Account account, ETransactionStatus status);
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Transaction> findTransactionsByStatusIs(ETransactionStatus status);
}
package joel.dovi.sanlam.repository;

import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findFirstByAccountAndStatusIsOrderByTransactionTimestampDesc(Account account, ETransactionStatus status);
}
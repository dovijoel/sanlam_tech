package joel.dovi.sanlam.repository;

import joel.dovi.sanlam.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

package joel.dovi.sanlam.repository;

import joel.dovi.sanlam.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}

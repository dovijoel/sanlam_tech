package joel.dovi.sanlam.repository;

import jakarta.persistence.LockModeType;
import joel.dovi.sanlam.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_READ) // pessimistic read to avoid changes to account based on dirty data
    @NonNull
    Optional<Account> findById(@NonNull Long accountId);
}

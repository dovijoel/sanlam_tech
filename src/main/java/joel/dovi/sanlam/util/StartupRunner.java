package joel.dovi.sanlam.util;

import com.tigerbeetle.CreateAccountResult;
import com.tigerbeetle.CreateAccountResultBatch;
import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.repository.AccountRepository;
import joel.dovi.sanlam.service.AccountService;
import joel.dovi.sanlam.service.TigerBeetleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartupRunner implements CommandLineRunner {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TigerBeetleService tigerBeetleService;
    private static final Logger LOG =
            LoggerFactory.getLogger(StartupRunner.class);


    @Override
    public void run(String... args) throws Exception {
        List<Account> accounts = accountRepository.findAll();
        CreateAccountResultBatch resultBatch = tigerBeetleService.initialiseTigerBeetle(accounts);
        while (resultBatch.next()) {
            if (resultBatch.getResult().equals(CreateAccountResult.Ok)) {
                LOG.info("Created account {}", resultBatch.getIndex());
            } else {
                LOG.error("Error creating account {}: {}",
                        resultBatch.getIndex(),
                        resultBatch.getResult());
            }
        }
    }
}

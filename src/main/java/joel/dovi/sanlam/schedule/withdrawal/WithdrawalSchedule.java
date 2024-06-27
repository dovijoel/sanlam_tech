package joel.dovi.sanlam.schedule.withdrawal;

import com.tigerbeetle.CreateTransferResultBatch;
import jakarta.transaction.Transactional;
import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.model.CashTransactionsToExecute;
import joel.dovi.sanlam.repository.TransactionRepository;
import joel.dovi.sanlam.service.TigerBeetleService;
import joel.dovi.sanlam.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WithdrawalSchedule {
    @Autowired TigerBeetleService tigerBeetleService;
    @Autowired
    TransactionService transactionService;


    @Scheduled(fixedDelay = 50) // run every 50ms
    public void processCashTransactions() throws Exception {
        transactionService.executePendingCashTransactions();
    }
}

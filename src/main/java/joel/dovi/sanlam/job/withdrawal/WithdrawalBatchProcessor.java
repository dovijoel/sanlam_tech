package joel.dovi.sanlam.job.withdrawal;

import joel.dovi.sanlam.model.Transaction;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class WithdrawalBatchProcessor implements ItemProcessor<List<Transaction>, List<Transaction>> {
    @Override
    public List<Transaction> process(final List<Transaction> withdrawals) throws Exception {

    }
}

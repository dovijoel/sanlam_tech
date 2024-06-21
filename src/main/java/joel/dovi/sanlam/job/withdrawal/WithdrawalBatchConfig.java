package joel.dovi.sanlam.job.withdrawal;

import joel.dovi.sanlam.model.ETransactionStatus;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@Configuration
public class WithdrawalBatchConfig {
    @Autowired private TransactionRepository transactionRepository;

    @Bean
    public Job withdrawalJob() {
        return new JobBuilder("withdrawalJob", transactionRepository)
                .incrementer(new RunIdIncrementer())
                .rea
    }

    @Bean
    public Step processWithdrawalsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, RepositoryItemWriter<Transaction> writer) {
        return new StepBuilder("withdrawalsStep", jobRepository)
                .<Transaction, Transaction> chunk(10, transactionManager)
                .reader(reader())
                .processor(withdrawalsProcessor())
                .writer(writer())
                .build();
    }

    @Bean
    public RepositoryItemReader<Transaction> reader() {
        return new RepositoryItemReaderBuilder<Transaction>().repository(transactionRepository)
                .methodName("findByStatusIs")
                .arguments(ETransactionStatus.PENDING)
                .build();
    }

    @Bean
    public RepositoryItemWriter<Transaction> writer() {
        return new RepositoryItemWriterBuilder<Transaction>().repository(transactionRepository)
                .methodName("save")
                .build();
    }
}

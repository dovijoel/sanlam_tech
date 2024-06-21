package joel.dovi.sanlam;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class SanlamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanlamApplication.class, args);
    }

}

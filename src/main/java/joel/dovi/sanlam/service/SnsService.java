package joel.dovi.sanlam.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@PropertySource("classpath:application.properties")
@Setter
@Getter
public class SnsService {

    private SnsClient snsClient;
    @Value("${aws.sns.region}")
    private String awsRegion;
    @Value("${aws.sns.account_id}")
    private String snsAccountId;
    @Value("${aws.sns.endpoint_override}")
    private String snsEndpoint;

    public SnsService(@Value("${aws.sns.region}")
                      String awsRegion,
    @Value("${aws.sns.account_id}")
    String snsAccountId,
    @Value("${aws.sns.endpoint_override}") String snsEndpoint) throws URISyntaxException {
        this.awsRegion = awsRegion;
        this.snsAccountId = snsAccountId;
        this.snsEndpoint = snsEndpoint;
        this.snsClient = SnsClient.builder()
                .region(Region.of(this.awsRegion)) // Specify your region
                .endpointOverride(new URI(this.snsEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }

    public void publish(String eventJson, String topicName) {
        String snsTopicArn = String.format("arn:aws:sns:%s:%s:%s", awsRegion, snsAccountId, topicName);
        PublishRequest publishRequest = PublishRequest.builder()
                .message(eventJson)
                .topicArn(snsTopicArn)
                .build();
        snsClient.publish(publishRequest);
    }
}

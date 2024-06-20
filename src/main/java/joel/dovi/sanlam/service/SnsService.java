package joel.dovi.sanlam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class SnsService {

    private SnsClient snsClient;
    @Value("aws.sns.region")
    private String awsRegion;
    @Value("aws.sns.account_id")
    private String snsAccountId;

    public SnsService() {
        this.snsClient = SnsClient.builder()
                .region(Region.EU_CENTRAL_1) // Specify your region
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

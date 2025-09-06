package rebound.backend.ai.config;

import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AIConfig {

    @Bean
    public PredictionServiceClient predictionServiceClient() throws IOException {
        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint("us-central1-aiplatform.googleapis.com:443")
                .build();
        return PredictionServiceClient.create(settings);
    }
}
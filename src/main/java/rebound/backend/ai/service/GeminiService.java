package rebound.backend.ai.service;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final PredictionServiceClient predictionServiceClient;

    @Value("${gemini.project.id}")
    private String projectId;

    @Value("${gemini.location.id}")
    private String locationId;

    @Value("${gemini.publisher.id}")
    private String publisherId;

    @Value("${gemini.model.id}")
    private String modelId;

    public String generateContent(String prompt) {
        EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                projectId, locationId, publisherId, modelId);

        Value instance = Value.newBuilder().setStringValue(prompt).build();
        List<Value> instances = List.of(instance);

        PredictRequest predictRequest = PredictRequest.newBuilder()
                .setEndpoint(endpointName.toString())
                .addAllInstances(instances)
                .build();

        try {
            PredictResponse response = predictionServiceClient.predict(predictRequest);
            if (response.getPredictionsCount() > 0) {
                return response.getPredictions(0).getStringValue();
            }
            return "No content generated.";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI content generation failed.", e);
        }
    }
}

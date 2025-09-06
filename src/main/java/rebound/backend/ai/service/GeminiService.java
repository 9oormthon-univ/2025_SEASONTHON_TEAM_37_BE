import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.protobuf.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}") // application.properties에서 API 키를 가져옵니다.
    private String geminiApiKey;

    public String generateContent(String prompt) {
        // AI 모델에 요청을 보내는 로직을 여기에 작성
        // ...
        return "AI가 생성한 내용"; // 임시 반환 값
    }

    // GeminiService.java
    public String generateContent(String prompt) {
        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            String endpoint = "projects/YOUR_PROJECT_ID/locations/us-central1/endpoints/YOUR_ENDPOINT_ID"; // 실제 엔드포인트 정보로 변경
            EndpointName name = EndpointName.parse(endpoint);

            // 프롬프트(텍스트)를 Value 객체로 변환
            Value instance = Value.newBuilder().setStringValue(prompt).build();

            PredictRequest request = PredictRequest.newBuilder()
                    .setEndpoint(name)
                    .addInstances(instance)
                    .build();

            // API 호출 및 응답 받기
            PredictResponse response = client.predict(request);

            // 응답에서 생성된 텍스트 추출
            String generatedText = response.getPredictions(0).getStringValue();
            return generatedText;

        } catch (IOException e) {
            e.printStackTrace();
            return "AI 응답 생성 실패";
        }
    }
}
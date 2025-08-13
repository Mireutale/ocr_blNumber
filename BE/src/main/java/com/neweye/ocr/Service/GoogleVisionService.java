package com.neweye.ocr.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neweye.ocr.Dto.GoogleVisionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.*;

@Service
public class GoogleVisionService {

    @Value("${google.vision.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_BATCH = 16; // images:annotate 한 요청당 최대 16장
    private static final float JPEG_QUALITY = 0.85f; // 트래픽 절감용

    public GoogleVisionService() {
        // 타임아웃 설정
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        rf.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
        this.restTemplate = new RestTemplate(rf);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 이미지 리스트를 Google Vision API로 처리하여 OCR 결과를 반환
     */
    public String processImages(List<BufferedImage> pages) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Google Vision API key is not configured");
        }

        // 이미지 → Base64(JPEG) 변환
        List<String> base64Images = new ArrayList<>(pages.size());
        for (BufferedImage page : pages) {
            base64Images.add(ImageEncodingService.encodeToBase64Jpeg(page, JPEG_QUALITY));
        }

        // 16장씩 청크로 분할하여 배치 요청
        StringBuilder out = new StringBuilder();
        int pageIndex = 0;
        for (int i = 0; i < base64Images.size(); i += MAX_BATCH) {
            List<String> chunk = base64Images.subList(i, Math.min(i + MAX_BATCH, base64Images.size()));
            String jsonReq = buildVisionBatchRequest(chunk);
            String jsonRes = callVisionAnnotate(jsonReq);

            // 응답 파싱: responses[n].fullTextAnnotation.text
            Map<Integer, String> pageTexts = parseTextsFromResponse(jsonRes);

            for (int k = 0; k < chunk.size(); k++) {
                pageIndex++;
                out.append("----- PAGE ").append(pageIndex).append(" -----\n");
                out.append(pageTexts.getOrDefault(k, "")); // 청크 내 순서가 페이지 순서와 매칭됨
                if (!out.toString().endsWith("\n")) out.append("\n");
            }
        }

        return out.toString();
    }

    /* ========================= Vision API 호출부 ========================= */

    private String callVisionAnnotate(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        String url = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;
        return restTemplate.postForObject(url, entity, String.class);
    }

    private String buildVisionBatchRequest(List<String> base64Images) throws Exception {
        GoogleVisionDto.VisionBatchRequest body = new GoogleVisionDto.VisionBatchRequest();
        body.setRequests(new ArrayList<>(base64Images.size()));

        // 공통 Feature / ImageContext(언어 힌트)
        GoogleVisionDto.VisionFeature feature = new GoogleVisionDto.VisionFeature();
        feature.setType("DOCUMENT_TEXT_DETECTION");

        GoogleVisionDto.VisionImageContext ctx = new GoogleVisionDto.VisionImageContext();
        ctx.setLanguageHints(Arrays.asList("ko", "en")); // 혼합문서 인식률 개선

        for (String b64 : base64Images) {
            GoogleVisionDto.VisionRequest req = new GoogleVisionDto.VisionRequest();

            GoogleVisionDto.VisionImage img = new GoogleVisionDto.VisionImage();
            img.setContent(b64);

            req.setImage(img);
            req.setFeatures(Collections.singletonList(feature));
            req.setImageContext(ctx);

            body.getRequests().add(req);
        }
        return objectMapper.writeValueAsString(body);
    }

    /**
     * responses[n].fullTextAnnotation.text 추출
     * 반환: 청크 내 인덱스 → 텍스트
     */
    private Map<Integer, String> parseTextsFromResponse(String json) throws Exception {
        Map<Integer, String> map = new HashMap<>();
        var node = objectMapper.readTree(json);
        var responses = node.path("responses");
        if (responses.isMissingNode() || !responses.isArray()) return map;

        int idx = 0;
        for (var r : responses) {
            String text = r.path("fullTextAnnotation").path("text").asText("");
            map.put(idx++, text);
        }
        return map;
    }
}

package com.neweye.ocr.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Service
public class OcrService {

    @Value("${google.vision.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OcrService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 파일을 받아서 OCR 처리를 수행합니다.
     * @param file 업로드된 파일 (PDF 또는 이미지)
     * @return OCR 처리된 텍스트
     */
    public String processOcr(MultipartFile file) throws Exception {
        String text;
        
        if (file.getOriginalFilename().endsWith(".pdf")) {
            // PDF 파일 처리
            List<BufferedImage> images = pdfToImages(file);
            StringBuilder sb = new StringBuilder();
            for (BufferedImage img : images) {
                sb.append(callGoogleVision(img));
            }
            text = sb.toString();
        } else {
            // 이미지 파일 처리
            BufferedImage img = ImageIO.read(file.getInputStream());
            text = callGoogleVision(img);
        }
        
        return text;
    }

    /**
     * PDF 파일을 이미지로 변환합니다.
     * @param file PDF 파일
     * @return 변환된 이미지 리스트
     */
    private List<BufferedImage> pdfToImages(MultipartFile file) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                images.add(bim);
            }
        }
        return images;
    }
    
    /**
     * Google Vision API를 호출하여 OCR을 수행합니다.
     * @param image 처리할 이미지
     * @return OCR 결과 텍스트
     */
    private String callGoogleVision(BufferedImage image) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Google Vision API key is not configured");
            }

            // 이미지를 Base64로 인코딩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

            // Google Vision API 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode requests = objectMapper.createArrayNode();
            ObjectNode request = objectMapper.createObjectNode();
            
            ObjectNode imageNode = objectMapper.createObjectNode();
            imageNode.put("content", base64Image);
            request.set("image", imageNode);
            
            ArrayNode features = objectMapper.createArrayNode();
            ObjectNode feature = objectMapper.createObjectNode();
            feature.put("type", "DOCUMENT_TEXT_DETECTION");
            features.add(feature);
            request.set("features", features);
            
            requests.add(request);
            requestBody.set("requests", requests);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Google Vision API 호출
            String response = restTemplate.postForObject(
                "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey,
                entity,
                String.class
            );

            // 응답 파싱
            ObjectNode responseJson = objectMapper.readValue(response, ObjectNode.class);
            ArrayNode responses = (ArrayNode) responseJson.get("responses");
            
            if (responses != null && responses.size() > 0) {
                ObjectNode firstResponse = (ObjectNode) responses.get(0);
                ObjectNode fullTextAnnotation = (ObjectNode) firstResponse.get("fullTextAnnotation");
                
                if (fullTextAnnotation != null && fullTextAnnotation.has("text")) {
                    return fullTextAnnotation.get("text").asText();
                }
            }
            
            return "No text found";
            
        } catch (Exception e) {
            throw new RuntimeException("Error calling Google Vision API: " + e.getMessage(), e);
        }
    }
}

package com.neweye.ocr.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Google Vision API 요청/응답을 위한 DTO 클래스들
 */
public class GoogleVisionDto {

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionBatchRequest {
        private List<VisionRequest> requests;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionRequest {
        private VisionImage image;
        private List<VisionFeature> features;
        private VisionImageContext imageContext; // 옵션
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionImage {
        private String content;               // Base64
        private VisionImageSource source;     // GCS/URL 사용 시
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionImageSource {
        private String imageUri; // gs://bucket/path 또는 https://...
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionFeature {
        private String type;        // DOCUMENT_TEXT_DETECTION 등
        private Integer maxResults; // 옵션
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VisionImageContext {
        private List<String> languageHints; // ["ko","en"]
        // private List<LatLongRect> cropHintsParams; // 필요 시 확장
    }
}

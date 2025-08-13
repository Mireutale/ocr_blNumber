package com.neweye.ocr.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class OcrService {

    @Autowired
    private GoogleVisionService googleVisionService;
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private BlNumberExtractorService blNumberExtractorService;

    /**
     * OCR 결과를 담는 내부 클래스
     */
    public static class OcrResult {
        private final String fullText;
        private final List<String> blNumbers;

        public OcrResult(String fullText, List<String> blNumbers) {
            this.fullText = fullText;
            this.blNumbers = blNumbers;
        }

        public String getFullText() {
            return fullText;
        }

        public List<String> getBlNumbers() {
            return blNumbers;
        }
    }

    /**
     * 파일(PDF/이미지)을 Vision API로 OCR하고 전체 텍스트와 B/L 넘버를 함께 반환
     */
    public OcrResult processOcr(MultipartFile file) throws Exception {
        final String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        final boolean isPdf = filename.toLowerCase(Locale.ROOT).endsWith(".pdf");

        List<BufferedImage> pages;
        if (isPdf) {
            pages = imageProcessingService.pdfToImages(file);
        } else {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) throw new IllegalArgumentException("Unsupported image");
            pages = Collections.singletonList(img);
        }

        String fullText = googleVisionService.processImages(pages);
        List<String> blNumbers = blNumberExtractorService.extractBlNumbers(fullText);
        
        return new OcrResult(fullText, blNumbers);
    }

    /**
     * 파일(PDF/이미지)을 Vision API로 OCR하고 페이지 구분자와 함께 텍스트를 반환 (기존 메서드)
     */
    public String processOcrTextOnly(MultipartFile file) throws Exception {
        final String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        final boolean isPdf = filename.toLowerCase(Locale.ROOT).endsWith(".pdf");

        List<BufferedImage> pages;
        if (isPdf) {
            pages = imageProcessingService.pdfToImages(file);
        } else {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) throw new IllegalArgumentException("Unsupported image");
            pages = Collections.singletonList(img);
        }

        return googleVisionService.processImages(pages);
    }

    /**
     * 파일을 OCR 처리하고 B/L 넘버만 추출하여 반환
     */
    public String processOcrForBlNumber(MultipartFile file) throws Exception {
        // 전체 텍스트 추출
        String fullText = processOcrTextOnly(file);
        
        // B/L 넘버만 추출
        return blNumberExtractorService.extractBlNumbersOnly(fullText);
    }

    /**
     * 파일을 OCR 처리하고 주요 B/L 넘버 하나만 반환
     */
    public String processOcrForPrimaryBlNumber(MultipartFile file) throws Exception {
        // 전체 텍스트 추출
        String fullText = processOcrTextOnly(file);
        
        // 주요 B/L 넘버 추출
        return blNumberExtractorService.extractPrimaryBlNumber(fullText);
    }
}

package com.neweye.ocr.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.neweye.ocr.Service.OcrService;
import com.neweye.ocr.Service.BlNumberExtractorService;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;
    
    @Autowired
    private BlNumberExtractorService blNumberExtractorService;

    /*
     * text, blNumbers, blNumberCount 모두 호출
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleOcr(@RequestParam("file") MultipartFile file) throws Exception {
        OcrService.OcrResult result = ocrService.processOcr(file);
        
        Map<String, Object> response = new HashMap<>();
        response.put("fullText", result.getFullText());
        response.put("blNumbers", result.getBlNumbers());
        response.put("blNumberCount", result.getBlNumbers().size());
        
        return ResponseEntity.ok(response);
    }

    /*
     * text 호출
     */
    @PostMapping("/text-only")
    public ResponseEntity<Map<String, String>> handleOcrTextOnly(@RequestParam("file") MultipartFile file) throws Exception {
        String text = ocrService.processOcrTextOnly(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("text", text);
        return ResponseEntity.ok(result);
    }

    /*
     * blNumbers 호출
     */
    @PostMapping("/bl-number")
    public ResponseEntity<Map<String, String>> handleBlNumberExtraction(@RequestParam("file") MultipartFile file) throws Exception {
        String blNumbers = ocrService.processOcrForBlNumber(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("blNumbers", blNumbers);
        return ResponseEntity.ok(result);
    }

    /*
     * primaryBlNumber 호출
     */
    @PostMapping("/bl-number/primary")
    public ResponseEntity<Map<String, String>> handlePrimaryBlNumber(@RequestParam("file") MultipartFile file) throws Exception {
        String primaryBlNumber = ocrService.processOcrForPrimaryBlNumber(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("primaryBlNumber", primaryBlNumber);
        return ResponseEntity.ok(result);
    }

    /*
     * debug 호출
     */
    @PostMapping("/debug")
    public ResponseEntity<Map<String, String>> handleDebug(@RequestParam("file") MultipartFile file) throws Exception {
        String text = ocrService.processOcrTextOnly(file);
        String debugInfo = blNumberExtractorService.debugPatternMatching(text);
        
        Map<String, String> result = new HashMap<>();
        result.put("debug", debugInfo);
        return ResponseEntity.ok(result);
    }

    /*
     * batch 호출, text 포함
     */
    @PostMapping("/test/batch")
    public ResponseEntity<Map<String, Object>> handleBatchTest(@RequestParam("files") MultipartFile[] files) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map<String, Object> fileResult = new HashMap<>();
            
            try {
                // 파일 정보
                fileResult.put("index", i + 1);
                fileResult.put("filename", file.getOriginalFilename());
                fileResult.put("size", file.getSize());
                fileResult.put("contentType", file.getContentType());
                
                // OCR 처리
                OcrService.OcrResult ocrResult = ocrService.processOcr(file);
                
                fileResult.put("fullText", ocrResult.getFullText());
                fileResult.put("blNumbers", ocrResult.getBlNumbers());
                fileResult.put("blNumberCount", ocrResult.getBlNumbers().size());
                fileResult.put("status", "success");
                
            } catch (Exception e) {
                fileResult.put("status", "error");
                fileResult.put("error", e.getMessage());
            }
            
            results.add(fileResult);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalFiles", files.length);
        response.put("results", results);
        
        return ResponseEntity.ok(response);
    }

    /*
     * batch-simple 호출, text 제외
     */
    @PostMapping("/test/batch-simple")
    public ResponseEntity<Map<String, Object>> handleBatchSimple(@RequestParam("files") MultipartFile[] files) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Map<String, Object> fileResult = new HashMap<>();
            
            try {
                // 간단한 정보만
                fileResult.put("index", i + 1);
                fileResult.put("filename", file.getOriginalFilename());
                
                // B/L 넘버만 추출
                String primaryBlNumber = ocrService.processOcrForPrimaryBlNumber(file);
                fileResult.put("primaryBlNumber", primaryBlNumber);
                fileResult.put("status", "success");
                
            } catch (Exception e) {
                fileResult.put("status", "error");
                fileResult.put("error", e.getMessage());
            }
            
            results.add(fileResult);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalFiles", files.length);
        response.put("results", results);
        
        return ResponseEntity.ok(response);
    }
}

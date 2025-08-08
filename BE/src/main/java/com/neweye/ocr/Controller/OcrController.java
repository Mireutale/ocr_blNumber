package com.neweye.ocr.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.neweye.ocr.Service.OcrService;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping
    public ResponseEntity<Map<String, String>> handleOcr(@RequestParam("file") MultipartFile file) throws Exception {
        String text = ocrService.processOcr(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("text", text);
        return ResponseEntity.ok(result);
    }
}

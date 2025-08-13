package com.neweye.ocr.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageProcessingService {

    private static final int PDF_DPI = 300;

    /**
     * PDF 파일을 이미지로 변환합니다.
     * @param file PDF 파일
     * @return 변환된 이미지 리스트
     */
    public List<BufferedImage> pdfToImages(MultipartFile file) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int p = 0; p < doc.getNumberOfPages(); p++) {
                // 300DPI 렌더링
                BufferedImage src = renderer.renderImageWithDPI(p, PDF_DPI);
                // Vision 품질/용량 밸런스를 위해 RGB로 표준화
                images.add(toRgb(src));
            }
        }
        return images;
    }

    /**
     * 이미지를 RGB 형식으로 변환합니다.
     */
    public static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, Color.WHITE, null);
        g.dispose();
        return rgb;
    }
}

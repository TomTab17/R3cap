package it.uniroma3.siw.R3cap.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfPreviewGenerator {

    // Genero immagine JPG della prima pagina del PDF
    public static String generatePreview(File pdfFile, String outputFolder, String previewFileName) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 150);
            File outputFile = new File(outputFolder + File.separator + previewFileName + ".jpg");
            ImageIO.write(image, "jpg", outputFile);
            return "/previews/" + previewFileName + ".jpg";
        }
    }
}


package com.myStore.myStore.utils;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightPdfGenerator {

    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightPdfGenerator() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public byte[] generatePdf(String html) {
        Page page = browser.newPage();
        page.setContent(html);

        byte[] pdfBytes = page.pdf(
                new Page.PdfOptions()
                        .setFormat("A4")
                        .setPrintBackground(true)
        );

        page.close();
        return pdfBytes;
    }
}


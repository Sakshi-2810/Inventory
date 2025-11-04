package com.myStore.myStore.utils;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaywrightPdfGenerator {

    private Playwright playwright;
    private Browser browser;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private void init() {
        if (initialized.compareAndSet(false, true)) {
            playwright = Playwright.create();

            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(java.util.List.of(
                                    "--no-sandbox",
                                    "--disable-dev-shm-usage"
                            ))
            );
        }
    }

    public byte[] generatePdf(String html) {
        init(); // lazy initialize Playwright & Browser only when first PDF is requested

        Page page = browser.newPage();
        page.setContent(html);

        byte[] pdf = page.pdf(new Page.PdfOptions()
                .setFormat("A4")
                .setPrintBackground(true)
        );

        page.close();
        return pdf;
    }

    @PreDestroy
    public void close() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}

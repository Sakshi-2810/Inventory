package com.myStore.myStore.utils;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaywrightPdfGenerator {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private synchronized void init() {
        if (!initialized.get()) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(java.util.List.of(
                            "--no-sandbox",
                            "--disable-dev-shm-usage"
                    ))
            );
            // Reuse a single BrowserContext to reduce overhead
            context = browser.newContext();
            initialized.set(true);
        }
    }

    public byte[] generatePdf(String html) {
        init();

        // Create a new page inside the single context (fast)
        Page page = context.newPage();
        try {
            page.setContent(html);
            return page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
            );
        } finally {
            page.close(); // Only close the page, not the context
        }
    }

    @PreDestroy
    public void close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}

package com.myStore.myStore.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaywrightPdfGenerator {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @PostConstruct
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

            // Reuse a single context for speed
            context = browser.newContext();

            // Optional warm-up to reduce first PDF latency
            try (Page page = context.newPage()) {
                page.setContent("<html><body></body></html>");
                page.pdf(); // generate a dummy PDF
            } catch (Exception e) {
                // ignore warm-up errors
            }

            initialized.set(true);
        }
    }

    public byte[] generatePdf(String html) {
        init();

        try (Page page = context.newPage()) {
            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            return page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setMargin(new Margin().setTop("20mm").setBottom("20mm").setLeft("15mm").setRight("15mm"))
            );
        }
    }

    @PreDestroy
    public void close() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}

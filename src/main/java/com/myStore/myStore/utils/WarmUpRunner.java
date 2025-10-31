package com.myStore.myStore.utils;

import com.myStore.myStore.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WarmUpRunner implements CommandLineRunner {

    @Autowired
    InvoiceService invoiceService;

    @Override
    public void run(String... args) throws Exception {
        invoiceService.warmUp();
    }
}

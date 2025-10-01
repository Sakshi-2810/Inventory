package com.myStore.myStore.controller;

import com.myStore.myStore.dto.InvoiceDto;
import com.myStore.myStore.model.Invoice;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.model.StockBill;
import com.myStore.myStore.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InvoiceController {

    @Autowired
    public InvoiceService invoiceService;
    @Autowired
    public ModelMapper modelMapper;

    @PostMapping(value = "/generateInvoice", produces = "application/json")
    public ResponseEntity<Response> saveInvoice(@RequestBody @Valid InvoiceDto invoice) {
        return ResponseEntity.ok(invoiceService.saveInvoice(modelMapper.map(invoice, Invoice.class)));
    }

    @GetMapping(produces = "application/json", value = "/invoices/partyname")
    public ResponseEntity<Response> getInvoicesByPartyName(@RequestParam String partyName) {
        return ResponseEntity.ok(invoiceService.getInvoicesByPartyName(partyName));
    }

    @PostMapping(produces = "application/json", value = "/calculate/cost" )
    public ResponseEntity<Response> calculateCost(@RequestBody Invoice invoice) {
        Integer cost = invoiceService.calculateTotalCost(invoice.getStockBills()) - invoice.getAdditionalDiscount();
        return ResponseEntity.ok(new Response(cost, "Total cost calculated successfully"));
    }

    @GetMapping(produces = "application/json", value = "/invoice" )
    public ResponseEntity<Response> getInvoiceById(@RequestParam Integer invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(invoiceId));
    }

    @GetMapping("/print")
    public ResponseEntity generateAndPrintInvoice(@RequestParam Integer invoiceId, HttpServletResponse response, @RequestParam String gstDetails) throws Exception {
        invoiceService.downloadInvoice(invoiceId, response, gstDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(produces = "application/json", value = "/invoice/delete" )
    public ResponseEntity<Response> deleteInvoice(@RequestParam Integer invoiceId) {
        return ResponseEntity.ok(invoiceService.deleteInvoice(invoiceId));
    }
}

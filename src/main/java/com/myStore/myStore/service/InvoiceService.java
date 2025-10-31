package com.myStore.myStore.service;

import com.myStore.myStore.dto.InvoiceDto;
import com.myStore.myStore.exception.CustomDataException;
import com.myStore.myStore.model.Invoice;
import com.myStore.myStore.model.Party;
import com.myStore.myStore.model.Response;
import com.myStore.myStore.model.StockBill;
import com.myStore.myStore.repository.InvoiceRepository;
import com.myStore.myStore.repository.PartyRepository;
import com.myStore.myStore.utils.DateUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class InvoiceService {
    @Autowired
    private PartyRepository partyRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PartyService partyService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StockDetailService stockDetailService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    public Integer generateNewInvoiceId() {
        Integer maxId = Optional.ofNullable(invoiceRepository.findTopByOrderByInvoiceIdDesc()).map(Invoice::getInvoiceId).orElse(0);
        return maxId + 1;
    }

    public Response saveInvoice(InvoiceDto invoiceDto) {
        Invoice invoice = modelMapper.map(invoiceDto, Invoice.class);

        Invoice existingInvoice = invoice.getInvoiceId() == null ? null : invoiceRepository.findById(invoice.getInvoiceId()).orElse(null);
        if (existingInvoice == null) {
            invoice.setInvoiceId(generateNewInvoiceId());
            invoice.setDate(DateUtils.getCurrentFormattedDate());
        }

        Integer totalCost = calculateTotalCost(invoice.getStockBills());
        invoice.setSubTotal(calculateSubTotal(invoice.getStockBills()));
        invoice.setTax(totalCost - invoice.getSubTotal());

        if (!invoice.getAdditionalDiscount().isBlank()) {
            if (invoice.getAdditionalDiscount().contains("%")) {
                double discountValue = Double.parseDouble(invoice.getAdditionalDiscount().replace("%", "").trim());
                totalCost -= (int) Math.round((totalCost * discountValue) / 100.0);
            } else {
                totalCost -= (int) Double.parseDouble(invoice.getAdditionalDiscount());
            }
        }

        invoice.setTotalCost(totalCost);
        invoice.setDueAmount(totalCost - invoice.getPaidAmount());

        if (!partyRepository.existsByName(invoice.getPartyName())) {
            Party party = new Party();
            party.setPartyId(partyService.generateNewPartyId());
            party.setName(invoice.getPartyName());
            partyRepository.save(party);
        }

        invoiceRepository.save(invoice);
        stockDetailService.saveStocks(invoice.getStockBills());
        log.info("Invoice saved/updated with ID: {}", invoice.getInvoiceId());
        return new Response(invoice.getInvoiceId(), "Invoice saved successfully");
    }

    public Integer calculateTotalCost(List<StockBill> stockBills) {
        return (int) Math.round(stockBills.stream().mapToDouble(stockBill -> (stockBill.getQuantity() * stockBill.getPrice()) * (100.0 + stockBill.getGst()) / 100.0).sum());
    }

    public double calculateSubTotal(List<StockBill> stockBills) {
        return stockBills.stream().mapToDouble(stockBill -> stockBill.getQuantity() * stockBill.getPrice()).sum();
    }

    public Response calcCostInJson(Invoice invoice) {
        Integer cost = calculateTotalCost(invoice.getStockBills());
        double subTotal = calculateSubTotal(invoice.getStockBills());

        double tax = cost - subTotal;
        if (!invoice.getAdditionalDiscount().isBlank()) {
            if (invoice.getAdditionalDiscount().contains("%")) {
                double discountValue = Double.parseDouble(invoice.getAdditionalDiscount().replace("%", "").trim());
                cost -= (int) Math.round((cost * discountValue) / 100.0);
            } else {
                cost -= (int) Double.parseDouble(invoice.getAdditionalDiscount());
            }
        }
        return new Response(Map.of("Cost", cost, "Tax", tax), "Total cost calculated successfully");
    }

    public Response getInvoiceById(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new CustomDataException("Invoice does not exist");
        }
        InvoiceDto invoiceDto = modelMapper.map(invoice, InvoiceDto.class);
        log.info("Fetched invoice with ID: {}", invoiceId);
        return new Response(invoiceDto, "Invoice fetched successfully");
    }

    public Response getInvoicesByPartyName(String partyName) {
        List<Invoice> invoices = invoiceRepository.findByPartyName(partyName);
        log.info("Fetched {} invoices for party: {}", invoices.size(), partyName);
        return new Response(invoices, "Invoices fetched successfully");
    }

    public void downloadInvoice(Integer invoiceId, HttpServletResponse response, String gstDetails) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new CustomDataException("Invoice does not exist"));

        String htmlContent = getHtmlContent(gstDetails, invoice);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice_" + invoice.getPartyName().replaceAll("\\s+", "_") + ".pdf");

        try (OutputStream os = response.getOutputStream()) {
            renderPdf(htmlContent, os);
            os.flush();
            log.info("✅ PDF generated and sent for invoice ID: {}", invoiceId);
        }
    }

    private static void renderPdf(String htmlContent, OutputStream os) {
        String xhtml = Jsoup.parse(htmlContent, "UTF-8")
                .outputSettings(new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml))
                .outerHtml();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml);
        renderer.layout();
        renderer.createPDF(os);
    }

    private String getHtmlContent(String gstDetails, Invoice invoice) {
        gstDetails = (gstDetails == null || gstDetails.isBlank()) ? "Deepak Agrawal<br>9425921009,7000347100" : gstDetails.replace("\n", "<br>");

        Party party = partyRepository.findByName(invoice.getPartyName());
        String partyAddress = (party != null && party.getAddress() != null) ? party.getAddress() : "";
        String partyPhone = (party != null && party.getPhoneNumber() != null) ? party.getPhoneNumber() : "";
        String partyGstin = (party != null && party.getGstin() != null) ? party.getGstin() : "";

        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("gstDetails", gstDetails);
        context.setVariable("partyAddress", partyAddress);
        context.setVariable("partyPhone", partyPhone);
        context.setVariable("partyGstin", partyGstin);
        context.setVariable("halfTax", String.format("%.2f", invoice.getTax() / 2.0));
        context.setVariable("formattedDiscount", formatDiscount(invoice.getAdditionalDiscount()));

        return templateEngine.process("templateInvoice", context);
    }

    /**
     * Formats discount field gracefully.
     */
    private String formatDiscount(String discount) {
        if (discount == null || discount.isBlank()) return "₹ 0";
        return discount.contains("%") ? discount : "₹ " + discount;
    }

    public Response deleteInvoice(Integer invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new CustomDataException("Invoice does not exist");
        }
        invoiceRepository.deleteById(invoiceId);
        log.info("Deleted invoice with ID: {}", invoiceId);
        return new Response(invoiceId, "Invoice deleted successfully");
    }

    public Response getAllInvoices() {
        log.info("Fetching all invoices");
        return new Response(invoiceRepository.findAll(), "All invoices fetched successfully");
    }

    public void warmUp() {
        try {
            log.info("🚀 Warmup: preloading invoice PDF engine...");

            // Dummy invoice object
            Invoice inv = new Invoice();
            inv.setInvoiceId(0);
            inv.setPartyName("Warmup");
            inv.setTax(0.0);
            inv.setAdditionalDiscount("0");

            String html = getHtmlContent("Warmup<br>Init", inv);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            renderPdf(html, bos);  // No HTTP response, just warm-up

            bos.close();
            log.info("✅ Warmup completed.");

        } catch (Exception e) {
            log.warn("Warm-up failed: {}", e.getMessage());
        }
    }

}

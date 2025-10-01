package com.myStore.myStore.service;

import com.itextpdf.html2pdf.HtmlConverter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    public Integer generateNewInvoiceId() {
        Integer maxId = Optional.ofNullable(invoiceRepository.findTopByOrderByInvoiceIdDesc()).map(Invoice::getInvoiceId).orElse(0);
        return maxId + 1;
    }

    public Response saveInvoice(Invoice invoice) {
        Invoice existingInvoice = invoice.getInvoiceId() == null ? null : invoiceRepository.findById(invoice.getInvoiceId()).orElse(null);
        if (existingInvoice == null) {
            invoice.setInvoiceId(generateNewInvoiceId());
            invoice.setDate(DateUtils.getCurrentFormattedDate());
        }

        Integer totalCost = calculateTotalCost(invoice.getStockBills());
        totalCost -= invoice.getAdditionalDiscount();

        invoice.setTotalCost(totalCost);
        invoice.setDueAmount(totalCost - invoice.getPaidAmount());

        if (!partyRepository.existsByName(invoice.getPartyName())) {
            Party party = new Party();
            party.setPartyId(partyService.generateNewPartyId());
            party.setName(invoice.getPartyName());
            partyRepository.save(party);
        }

        invoiceRepository.save(invoice);
        return new Response(invoice.getInvoiceId(), "Invoice saved successfully");
    }

    public Integer calculateTotalCost(List<StockBill> stockBills) {
        return (int) Math.round(stockBills.stream().mapToDouble(stockBill -> (stockBill.getQuantity() * stockBill.getPrice()) * (100.0 + stockBill.getGst()) / 100.0).sum());
    }

    public Response getInvoiceById(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new CustomDataException("Invoice does not exist");
        }
        return new Response(invoice, "Invoice fetched successfully");
    }

    public Response getInvoicesByPartyName(String partyName) {
        List<Invoice> invoices = invoiceRepository.findByPartyName(partyName);
        return new Response(invoices, "Invoices fetched successfully");
    }

    public void downloadInvoice(Integer invoiceId, HttpServletResponse response, String gstDetails) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (gstDetails.isBlank()) {
            gstDetails = "<h3>M/s Chhitarmal Motilal</h3>14 M.G. Road Shujalpur Mandi<br>9425921009<br>GSTIN: 23AFKPA6567R1ZW<br>FSSAI Licence No. 11420830000041<br>Maan No. 2110069<br>Email: deepakagrawalsjp@gmail.com";
        } else {
            gstDetails = "<h3>" + gstDetails + "</h3>";
        }
        if (invoice == null) {
            throw new CustomDataException("Invoice does not exist");
        }
        InputStream inputStream = getClass().getResourceAsStream("/templates/templateInvoice.html");
        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        htmlContent = htmlContent.replace("{{invoiceId}}", invoiceId.toString());
        htmlContent = htmlContent.replace("{{invoiceDate}}", invoice.getDate());
        htmlContent = htmlContent.replace("{{partyName}}", invoice.getPartyName());
        htmlContent = htmlContent.replace("{{firm}}", gstDetails);

        Party party = partyRepository.findByName(invoice.getPartyName());

        htmlContent = htmlContent.replace("{{partyAddress}}", party.getAddress() == null ? "" : party.getAddress());
        htmlContent = htmlContent.replace("{{partyPhone}}", party.getPhoneNumber() == null ? "" : party.getPhoneNumber());
        htmlContent = htmlContent.replace("{{partyGSTIN}}", party.getGstin() == null ? "" : party.getGstin());
        htmlContent = htmlContent.replace("{{Total}}", String.valueOf(invoice.getTotalCost()));

        double subTotal = invoice.getStockBills().stream().map(stockBill -> stockBill.getQuantity() * stockBill.getPrice()).reduce(0.0, Double::sum);
        htmlContent = htmlContent.replace("{{subtotal}}", String.valueOf(subTotal));
        htmlContent = htmlContent.replace("{{Tax}}", String.valueOf((invoice.getTotalCost() + invoice.getAdditionalDiscount() - subTotal) / 2.0));

        htmlContent = htmlContent.replace("{{Discount}}", String.valueOf(invoice.getAdditionalDiscount()));

        StringBuilder itemsRows = new StringBuilder();
        for (StockBill partBill : invoice.getStockBills()) {
            Integer totalPrice = (int) Math.round((partBill.getQuantity() * partBill.getPrice()) * (100.0 + partBill.getGst()) / 100.0);
            itemsRows.append("<tr>").append("<td style='padding:8px; border:1px solid #ddd;'>").append(partBill.getItemName()).append("</td>").append("<td style='text-align:right;padding:8px; border:1px solid #ddd;'>").append(partBill.getQuantity()).append("</td>").append("<td style='text-align:right;padding:8px; border:1px solid #ddd;'>").append(partBill.getPrice()).append("</td>").append("<td style='text-align:right;padding:8px; border:1px solid #ddd;'>").append(partBill.getGst()).append("</td>").append("<td style='text-align:right;padding:8px; border:1px solid #ddd;'>").append(totalPrice).append("</td>").append("</tr>");
        }
        htmlContent = htmlContent.replace("{{invoiceItems}}", itemsRows.toString());

        // Set response headers for file download
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");

        try (OutputStream out = response.getOutputStream()) {
            HtmlConverter.convertToPdf(htmlContent, out);
            log.info("PDF generated and sent to client for invoice: {}", invoiceId);
        }
    }

    public Response deleteInvoice(Integer invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new CustomDataException("Invoice does not exist");
        }
        invoiceRepository.deleteById(invoiceId);
        return new Response(invoiceId, "Invoice deleted successfully");
    }

    public Response getAllInvoices() {
        return new Response(invoiceRepository.findAll(), "All invoices fetched successfully");
    }
}

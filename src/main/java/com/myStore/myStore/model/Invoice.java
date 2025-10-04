package com.myStore.myStore.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    @Id
    private Integer invoiceId;
    @NotBlank
    private String partyName;
    private List<StockBill> stockBills = new ArrayList<>();
    private String date;
    private Integer totalCost;
    private double subTotal = 0.0;
    private double tax = 0.0;
    @NotBlank
    @Pattern(regexp = "BUY|SELL|CASH", message = "Transaction type must be BUY or SELL or CASH")
    private String transactionType;
    private Integer paidAmount = 0;
    private Integer dueAmount;
    private String additionalDiscount;
}

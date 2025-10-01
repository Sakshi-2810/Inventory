package com.myStore.myStore.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockBill {
    @NotBlank
    private String itemName;
    @NotNull
    private double quantity;
    private double gst = 0.0;
    @NotNull
    private double price;
    private String unit;
}

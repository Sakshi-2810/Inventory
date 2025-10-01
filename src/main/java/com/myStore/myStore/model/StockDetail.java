package com.myStore.myStore.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDetail {
    @Id
    private Integer itemId;
    @Indexed(unique = true)
    private String itemName;
    private double gst = 0.0;
    @NotNull
    private double price;
    @NotBlank
    private String unit;
}

package com.myStore.myStore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyDto {
    private Integer partyId;
    private String name;
    private String phoneNumber;
    private String address;
    private String gstin;
    private Integer totalDue;
    private List<InvoiceDto> invoices;
    private Integer totalCost;
}

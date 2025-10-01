package com.myStore.myStore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Party {
    @Id
    private Integer partyId;
    @Indexed(unique = true)
    private String name;
    private String phoneNumber="";
    private String address="";
    private String gstin = "";
    private Integer amountAdded = 0;
}

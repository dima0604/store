package com.store.storeapp.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfirmDTO {
    private String shipment_no;
    private Boolean done = true;
    public ConfirmDTO(String shipment_no){
        this.shipment_no = shipment_no;
    }
}

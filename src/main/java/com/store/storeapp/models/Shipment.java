package com.store.storeapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.store.storeapp.models.dto.ShipmentDTO;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shipment {
    private String shipment_no;
    private String date;
    private String customer_name;
    private String transport_no;
    private String city;
    private String sender_code;
    private String warehouse_no;
    private String ready;
    private String done;
    private List<Item> items;

    public ShipmentDTO toDTO() {
        ShipmentDTO result = new ShipmentDTO();
        result.setShipment_no(shipment_no);
        result.setCity(city);
        result.setDate(date);
        result.setCustomer_name(customer_name);
        result.setTransport_no(transport_no);
        result.setItems(items);
        return result;
    }
}

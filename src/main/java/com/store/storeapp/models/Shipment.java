package com.store.storeapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String warehouse_no;
    private List<Item> items;
}

package com.store.storeapp.models.dto;

import com.store.storeapp.models.Item;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
@Data
public class ShipmentDTO implements Comparable<ShipmentDTO> {
    private String shipment_no;
    private String date;
    private String customer_name;
    private String transport_no;
    private String city;
    private List<Item> items;

    @Override
    public int compareTo(ShipmentDTO o) {
        return this.shipment_no.compareTo(o.getShipment_no());
    }
}

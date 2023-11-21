package com.store.storeapp.models;

import com.store.storeapp.models.dto.ShipmentDTO;
import com.store.storeapp.models.dto.ShipmentsDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class Shipments {
    private List<Shipment> shipments;

    public ShipmentsDTO toDTO(String done) {
        ShipmentsDTO result = new ShipmentsDTO();
        List<ShipmentDTO> shipments = new ArrayList<>();
        for (var shipment : this.shipments) {
            if ("1".equals(shipment.getReady()) && shipment.getDone().equals(done)) {
                shipments.add(shipment.toDTO());
            }
        }
        result.setShipments(shipments);
        return result;
    }

    public HashMap<String, Integer> getShipmentsInfo() {
        HashMap<String, Integer> result = new HashMap<>();
        for (var shipment : this.shipments) {
            if ("".equals(shipment.getSender_code())){
                result.put(shipment.getTransport_no(), 0);
                continue;
            }
            result.put(shipment.getTransport_no(), Integer.parseInt(shipment.getSender_code()) - 1);
        }
        return result;
    }
}

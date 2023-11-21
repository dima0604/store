package com.store.storeapp.models;

import lombok.Data;

@Data
public class Item {
    private String item_code;
    private String item_description;
    private String quantity;
    private String item_full_description;
}

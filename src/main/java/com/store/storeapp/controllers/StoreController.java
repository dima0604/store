package com.store.storeapp.controllers;

import com.store.storeapp.models.Shipments;
import com.store.storeapp.models.dto.ConfirmDTO;
import com.store.storeapp.models.dto.ShipmentDTO;
import com.store.storeapp.models.dto.ShipmentsDTO;
import com.store.storeapp.services.UserService;
import com.store.storeapp.components.Utility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.*;

@Controller
public class StoreController {
    private final PasswordEncoder encoder;
    private final UserService userService;

    private List<ShipmentDTO> shipments;
    private List<ShipmentDTO> shipmentsClosed;
    private HashMap<String, Integer> shipmentsInfo;
    private Utility utility;
    private static final Logger LOGGER = LogManager.getLogger(StoreController.class);

    @Value("${server.domain}")
    private String domain;
    @Value("${apiKey}")
    private List<String> apiKey;
    @Value("${url}")
    private String url;
    @Value("${urlAccept}")
    private String urlAccept;
    @Value("${urlConfirmAll}")
    private String urlConfirmAll;

    public StoreController(PasswordEncoder encoder, UserService userService, Utility utility) {
        this.encoder = encoder;
        this.userService = userService;
        this.utility = utility;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        User user = utility.getCurrentUser();

        Shipments shipmentsJson = utility.sendRequest(url, Shipments.class);
        if (shipmentsJson == null){
            return "unauthorized";
        }
        shipments = shipmentsJson.toDTO("0").getShipments();
        Collections.sort(shipments);
        shipmentsClosed = shipmentsJson.toDTO("1").getShipments();
        Collections.sort(shipmentsClosed);
        shipmentsInfo = shipmentsJson.getShipmentsInfo();

        model.addAttribute("login", user.getUsername());
        model.addAttribute("count", shipments.size());
        model.addAttribute("countClosed", shipmentsClosed.size());
        model.addAttribute("shipments", shipments);
        model.addAttribute("flag", true);
        return "index";
    }

    @GetMapping("/closed")
    public String indexClosed(Model model, HttpServletRequest request) {
        User user = utility.getCurrentUser();

        model.addAttribute("login", user.getUsername());
        model.addAttribute("count", shipments.size());
        model.addAttribute("countClosed", shipmentsClosed.size());
        model.addAttribute("shipments", shipmentsClosed);
        model.addAttribute("flag", false);
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, HttpServletRequest request) {
        if (error != null) {
            LOGGER.warn("Authentication denied, IP: " + utility.getClientIpAddress(request));
        }
        return "login";
    }

    @GetMapping("/unauthorized")
    public String unauthorized(Model model) {
        return "unauthorized";
    }


    @PostMapping("/download")
    public void download(@RequestParam String transport_no,
                         HttpServletResponse response) throws IOException {
        Integer keyIndex = shipmentsInfo.get(transport_no);
        if (keyIndex == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String fileUrl = "https://my.novaposhta.ua/orders/printMarking100x100/orders[]/"
                         + transport_no + "/type/pdf/apiKey/"
                         + apiKey.get(keyIndex) + "/zebra";

        byte[] fileContent = utility.downloadFile(fileUrl);

        if (fileContent.length == 0) {
            LOGGER.error("TTH download error, file not found, TTH: " + transport_no);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"ttn.pdf\"");
        response.setContentLength(fileContent.length);
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(fileContent);
        } catch (IOException e) {
            LOGGER.error("TTH download error \n" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String shipment_no,
                                          HttpServletResponse response) throws IOException {
        for (int i = 0; i < shipments.size(); i++) {
            ShipmentDTO shipment = shipments.get(i);
            if (shipment_no.equals(shipment.getShipment_no())) {
                shipmentsClosed.add(shipment);
                shipments.remove(shipment);

                String confirmResult = utility.sendSaveRequest(new ConfirmDTO(shipment_no), urlAccept);
                if ("ok".equals(confirmResult)) {
                    LOGGER.info("Shipment confirmed № " + shipment.getShipment_no()
                                + " (user: " + utility.getCurrentUser().getUsername() + ")");
                    return new ResponseEntity<>("Shipment confirmed", HttpStatus.OK);
                }
            }
        }
        LOGGER.error("Shipment not found № " + shipment_no);
        return new ResponseEntity<>("Shipment not found", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/check-session")
    public ResponseEntity<String> checkSession() {
        if (utility.isSessionValid()) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
        }
    }

    @GetMapping("/confirmAllShipments")
    public ResponseEntity<String> confirmAllShipments() {
        String confirmResult = utility.sendConfirmAllRequest(urlConfirmAll);
        if ("ok".equals(confirmResult)) {
            LOGGER.info("All Shipments confirmed"
                        + " (user: " + utility.getCurrentUser().getUsername() + ")");
            return new ResponseEntity<>("Shipments confirmed", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Server not found", HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/confirmTest")
    public ResponseEntity<String> confirmTest() {
            return new ResponseEntity<>("ok", HttpStatus.OK);
//            return new ResponseEntity<>("Server not found", HttpStatus.BAD_REQUEST);
    }



}








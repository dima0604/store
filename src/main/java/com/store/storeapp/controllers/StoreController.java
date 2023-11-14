package com.store.storeapp.controllers;

import com.store.storeapp.models.Shipment;
import com.store.storeapp.models.Shipments;
import com.store.storeapp.services.UserService;
import com.store.storeapp.components.Utility;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class StoreController {
    private final PasswordEncoder encoder;
    private final UserService userService;
    private String fileName = null;
    private String contentType = null;

    private List<Shipment> shipments;
    private List<Shipment> shipmentsClosed = new ArrayList<>();
    private Utility utility;
    private static final Logger LOGGER = LogManager.getLogger(StoreController.class);

    @Value("${server.domain}")
    private String domain;
    @Value("${apiKey}")
    private String apiKey;
    @Value("${url}")
    private String url;

    public StoreController(PasswordEncoder encoder, UserService userService, Utility utility) {
        this.encoder = encoder;
        this.userService = userService;
        this.utility = utility;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        User user = utility.getCurrentUser();
        LOGGER.info("Пользователь зашел на стартовую страницу IP: " + utility.getClientIpAddress(request));

//        shipments = utility.getTestDB();
        Shipments shipments1 = utility.sendRequest(url, Shipments.class);
        if (shipments1==null) return "unauthorized";
        shipments = shipments1.getShipments();

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
        LOGGER.info("Пользователь зашел на стартовую страницу IP: " + utility.getClientIpAddress(request));

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
            LOGGER.warn("Ошибка входа IP: " + utility.getClientIpAddress(request));
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

        String fileUrl = "https://my.novaposhta.ua/orders/printMarking100x100/orders[]/"
                         + transport_no + "/type/pdf/apiKey/"
                         + apiKey + "/zebra";

        byte[] fileContent = utility.downloadFile(fileUrl);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"ttn.pdf\"");
        response.setContentLength(fileContent.length);
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(fileContent);
        } catch (IOException e) {
            LOGGER.error("Ошибка при отправке ТТН \n" + e.getMessage());
        }

    }
    @PostMapping("/accept")
    public ResponseEntity<String> accept(@RequestParam String shipment_no,
                         HttpServletResponse response) throws IOException{
        for (int i=0;i<shipments.size();i++) {
            Shipment shipment = shipments.get(i);
            if (shipment_no.equals(shipment.getShipment_no())){
                shipmentsClosed.add(shipment);
                shipments.remove(shipment);
                LOGGER.info("Подтверждение загрузки № "+shipment.getShipment_no());
                return new ResponseEntity<>("Отгрузка закрыта", HttpStatus.OK);
            }
        }
        LOGGER.error("Ошибка поиска загрузки № "+ shipment_no);
        return new ResponseEntity<>("Отгрузка не найдена", HttpStatus.BAD_REQUEST);

    }

}








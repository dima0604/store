package com.store.storeapp.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.storeapp.controllers.StoreController;
import com.store.storeapp.models.Shipment;
import com.store.storeapp.models.Shipments;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
public class Utility {
    private static final Logger LOGGER = LogManager.getLogger(StoreController.class);
    private ObjectMapper objectMapper;

    public Utility(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public User getCurrentUser() {
        Object object = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if ("anonymousUser".equals(object)) {
            return null;
        }
        return (User) object;
    }

    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0];
    }

    public List<Shipment> getTestDB() {
        Shipments shipments = null;
        File jsonFile = new File("store.json");
        try {
            shipments = objectMapper.readValue(jsonFile, Shipments.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (shipments == null) {
            return null;
        }
        return shipments.getShipments();
    }

    public <T> T sendRequest(String url, Class<T> clazz) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String json = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            T shipments = objectMapper.readValue(json, clazz);
            return shipments;
        } catch (Exception e) {
            LOGGER.error("Ошибка конвертации json \n"+e.getMessage());
        }
        return null;
    }

    private <T> List<T> sendRequest_d(String url, Class<T[]> clazz) {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        try {
            ResponseEntity<T[]> response = restTemplate.getForEntity(builder.toUriString(), clazz);
            if (response.getBody() == null) {
                return null;
            }
            return Arrays.asList(response.getBody());
        } catch (ResourceAccessException e) {
            LOGGER.error("Ошибка отправки/получения данных при отправке списка отгрузок  " + e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            LOGGER.error("Ошибка отправки/получения данных при отправке списка отгрузок (код: " + e.getStatusCode() + ")  " + e.getMessage());
            return null;
        }
    }

    private String sendRequestAndGetString(String url) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(url, String.class);
            return response;
        } catch (ResourceAccessException e) {
            LOGGER.error("Ошибка отправки/получения данных при отправке списка отгрузок  " + e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            LOGGER.error("Ошибка отправки/получения данных при отправке списка отгрузок (код: " + e.getStatusCode() + ")  " + e.getMessage());
            return null;
        }
    }

    public byte[] downloadFile(String urlInput) throws IOException {
        byte[] byteContent = new byte[1024];

        URL url = new URL(urlInput);

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            InputStream connInputStream = httpConn.getInputStream();

            byteContent = downloadFile(url.openStream());

            connInputStream.close();

        } else {

            String failedReponse = "Download URL: " + urlInput + "/r/n/nNo file to download. Server replied HTTP code: "
                                   + Integer.toString(responseCode);
            byteContent = failedReponse.getBytes();
        }

        httpConn.disconnect();

        return byteContent;
    }
    private byte[] downloadFile(InputStream connectionInput) throws IOException {

        BufferedInputStream bis = new BufferedInputStream(connectionInput);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {

            os.write(buffer, 0, count);

        }

        return os.toByteArray();
    }
}

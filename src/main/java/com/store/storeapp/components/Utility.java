package com.store.storeapp.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.storeapp.controllers.StoreController;
import com.store.storeapp.models.Shipment;
import com.store.storeapp.models.Shipments;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
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

    public boolean isSessionValid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
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
            LOGGER.error("Json conversion error \n"+e.getMessage());
        }
        return null;
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

    public  <T> String sendSaveRequest(T obj, String url) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<T> entity = new HttpEntity<>(obj, headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else {
                    LOGGER.error("Error sending confirmation request (HTTP status: " + response.getStatusCode() + ")");
                    return null;
                }
            } catch (ResourceAccessException e) {
                LOGGER.error("Error sending confirmation request (server not found)  " + e.getMessage());
                return null;
            } catch (HttpClientErrorException e) {
                LOGGER.error("Error sending confirmation request (cod: " + e.getStatusCode() + ")  " + e.getMessage());
                return null;
            }
    }
    public  <T> String sendConfirmAllRequest(String url) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<T> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOGGER.error("Error sending confirmation request (HTTP status: " + response.getStatusCode() + ")");
                return null;
            }
        } catch (ResourceAccessException e) {
            LOGGER.error("Error sending confirmation request (server not found)  " + e.getMessage());
            return null;
        } catch (HttpClientErrorException e) {
            LOGGER.error("Error sending confirmation request (cod: " + e.getStatusCode() + ")  " + e.getMessage());
            return null;
        }
    }
}

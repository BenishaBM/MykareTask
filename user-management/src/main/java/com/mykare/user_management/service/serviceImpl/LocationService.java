package com.mykare.user_management.service.serviceImpl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String getIpAddress() {
        String response = restTemplate.getForObject("https://api.ipify.org?format=json", String.class);
        // Simple parsing since the response is in format {"ip":"xxx.xxx.xxx.xxx"}
        return response.substring(response.indexOf(":\"") + 2, response.indexOf("\"}"));
    }
    
    public String getCountryFromIp(String ip) {
        String url = "http://ip-api.com/json/" + ip;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return response != null ? (String) response.get("country") : "Unknown";
    }
}
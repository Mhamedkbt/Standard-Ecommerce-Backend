package com.ecommerce.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class KeepAliveService {

    @Value("${app.url}")
    private String appUrl;

    // 🎯 600,000 ms = 10 minutes
    @Scheduled(fixedRate = 600000)
    public void keepAppAlive() {
        try {
            // It now uses the URL from your Environment Variables
            URL url = new URL(appUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            System.out.println("Self-ping sent to: " + appUrl + " | Status: " + responseCode);

        } catch (Exception e) {
            System.err.println("Self-ping failed for URL " + appUrl + ": " + e.getMessage());
        }
    }
}
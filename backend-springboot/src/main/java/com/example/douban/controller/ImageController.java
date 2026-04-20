package com.example.douban.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/image")
public class ImageController {
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024;

    @GetMapping
    public void getImage(@RequestParam("url") String imageUrl, HttpServletResponse response) {
        List<String> candidates;
        try {
            candidates = buildImageCandidates(imageUrl);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (String candidate : candidates) {
            if (tryWriteImage(candidate, response)) {
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private boolean tryWriteImage(String imageUrl, HttpServletResponse response) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            String protocol = url.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                return false;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Referer", "https://movie.douban.com/");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");

            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                return false;
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return false;
            }

            byte[] imageBytes;
            try (InputStream inputStream = connection.getInputStream()) {
                imageBytes = readImageBytes(inputStream);
            }

            response.setContentType(contentType);
            response.setContentLength(imageBytes.length);
            response.setHeader("Cache-Control", "public, max-age=86400, immutable");

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(imageBytes);
                outputStream.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<String> buildImageCandidates(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("Unsupported image protocol");
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(imageUrl);

        String host = url.getHost();
        if (host != null && host.toLowerCase(Locale.ROOT).matches("img\\d+\\.doubanio\\.com")) {
            String[] doubanHosts = {"img1.doubanio.com", "img2.doubanio.com", "img3.doubanio.com", "img9.doubanio.com"};
            for (String doubanHost : doubanHosts) {
                if (!doubanHost.equalsIgnoreCase(host)) {
                    candidates.add(new URL(url.getProtocol(), doubanHost, url.getPort(), url.getFile()).toString());
                }
            }
        }

        return candidates;
    }

    private byte[] readImageBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        int totalBytes = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytes += bytesRead;
            if (totalBytes > MAX_IMAGE_BYTES) {
                throw new IllegalStateException("Image is too large");
            }
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}

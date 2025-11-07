package com.sohamkamani.mcp_shopping_list;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Tool(
        name = "getWeather",
        description = "Get the current weather for a given city. Example: getWeather('Mysuru') or getWeather('London,UK')."
    )
    public String getWeather(String city) {
        if (city == null || city.trim().isEmpty()) {
            return "Please specify a valid city name.";
        }

        city = city.trim();

        // ✅ Normalize a few common Indian city aliases
        switch (city.toLowerCase()) {
            case "bangalore" -> city = "Bengaluru";
            case "mysuru" -> city = "Mysore";
            case "bombay" -> city = "Mumbai";
            case "madras" -> city = "Chennai";
        }

        // ✅ Add country code only if user didn’t specify one
        if (!city.contains(",")) {
            city = city + ",IN";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // ✅ Let Spring handle encoding properly
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build(true) // 'true' → keeps commas, handles encoding safely
                    .toUriString();

            System.out.println("🌍 Fetching weather from: " + url);

            // ✅ Make the API request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.isEmpty()) {
                return "No weather data found for " + city + ".";
            }

            // ✅ Handle “city not found” gracefully
            Object cod = response.get("cod");
            if (cod != null && cod instanceof Number && ((Number) cod).intValue() != 200) {
                return "City not found or invalid: " + city;
            }

            // ✅ Extract details safely
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");

            if (main == null || weatherList == null || weatherList.isEmpty()) {
                return "Weather information not available for " + city + ".";
            }

            Map<String, Object> weather = weatherList.get(0);
            double temp = ((Number) main.get("temp")).doubleValue();
            String condition = (String) weather.get("description");
            String cityName = (String) response.get("name");

            return String.format(
                "The current weather in %s is %s with a temperature of %.1f°C.",
                cityName != null ? cityName : city, condition, temp
            );

        } catch (HttpClientErrorException e) {
            // ✅ Clear messages for common errors
            if (e.getStatusCode().value() == 404) {
                return "City not found: " + city;
            } else if (e.getStatusCode().value() == 401) {
                return "Invalid API key. Please check your OpenWeatherMap key.";
            } else {
                return "Weather API error (" + e.getStatusCode() + "): " + e.getMessage();
            }

        } catch (Exception e) {
            // ✅ Catch any unexpected issues
            return "Error retrieving weather for " + city + ": " + e.getMessage();
        }
    }
}

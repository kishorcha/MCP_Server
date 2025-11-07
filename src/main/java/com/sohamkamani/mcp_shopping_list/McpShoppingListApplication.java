package com.sohamkamani.mcp_shopping_list;

import java.util.Arrays;
import java.util.List;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpShoppingListApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpShoppingListApplication.class, args);
    }

    // ✅ Clean and correct version: combines both tools safely
    @Bean
    public List<ToolCallback> tools(ShoppingCart shoppingCart, WeatherService weatherService) {
        return Arrays.stream(new ToolCallback[][] {
                ToolCallbacks.from(shoppingCart),
                ToolCallbacks.from(weatherService)
        })
        .flatMap(Arrays::stream)
        .toList();
    }
}

package ru.fixbyte.monitoring.metrika;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YandexMetrikaService {
    private final String token;
    private final String counterId;
    private final HttpClient client = HttpClient.newHttpClient();

    public YandexMetrikaService(String token, String counterId) {
        this.token = token;
        this.counterId = counterId;
    }

    // Пример метода: получить статистику по визитам, просмотрам и пользователям - за сегодня
    public MetrikaStats getTodayStats() throws Exception {
        String date = LocalDate.now().toString();
        String url = "https://api-metrika.yandex.net/stat/v1/data" +
                "?ids=" + counterId +
                "&metrics=ym:s:visits,ym:s:pageviews,ym:s:users" +
                "&date1=" + date +
                "&date2=" + date;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "OAuth " + token)
                .GET()
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Простейший парсинг через Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.body());
        JsonNode dataArray = root.path("data");
        int visits = 0, pageviews = 0, users = 0;

        if (dataArray.isArray() && dataArray.size() > 0) {
            JsonNode metrics = dataArray.get(0).path("metrics");
            if (metrics.isArray() && metrics.size() == 1) {
                JsonNode arr = metrics.get(0);
                visits = arr.get(0).asInt();
                pageviews = arr.get(1).asInt();
                users = arr.get(2).asInt();
            }
        }

        return new MetrikaStats(visits, pageviews, users, date);
    }
}
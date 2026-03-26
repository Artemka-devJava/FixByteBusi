package ru.fixbyte.monitoring.metrika;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class YandexMetrikaService {

    private static final String CALL_GOAL_ID = "503081052";

    private final String token;
    private final String counterId;

    public YandexMetrikaService(String token, String counterId) {
        this.token = token;
        this.counterId = counterId;
    }

    public MetrikaStats getTodayStats() throws Exception {
        String date = LocalDate.now().toString();
        String url = "https://api-metrika.yandex.net/stat/v1/data?ids=" + counterId
                + "&metrics=ym:s:visits,ym:s:pageviews,ym:s:users,ym:s:goal" + CALL_GOAL_ID + "reaches"
                + "&date1=" + date
                + "&date2=" + date;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "OAuth " + token)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = new ObjectMapper().readTree(response.body());
        JsonNode totals = root.path("totals");

        if (totals == null || !totals.isArray() || totals.size() < 4) {
            throw new Exception("Нет данных в ответе Яндекс.Метрики: " + response.body());
        }

        return new MetrikaStats(
                (int) Math.round(totals.get(0).asDouble()),
                (int) Math.round(totals.get(1).asDouble()),
                (int) Math.round(totals.get(2).asDouble()),
                (int) Math.round(totals.get(3).asDouble()),
                date
        );
    }
}
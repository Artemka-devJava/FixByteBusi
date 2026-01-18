package ru.fixbyte.monitoring.metrika;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YandexMetrikaService {
    private final String token;
    private final String counterId;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String callGoalId = "503081052";  // <--- сюда ваш ID цели звонка

    public YandexMetrikaService(String token, String counterId) {
        this.token = token;
        this.counterId = counterId;
    }

    public MetrikaStats getTodayStats() throws Exception {
        String date = java.time.LocalDate.now().toString();
        String callGoalId = "503081052";
        String url = "https://api-metrika.yandex.net/stat/v1/data?ids=" + counterId
                + "&metrics=ym:s:visits,ym:s:pageviews,ym:s:users,ym:s:goal" + callGoalId + "reaches"
                + "&date1=" + date
                + "&date2=" + date;

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Authorization", "OAuth " + token)
                .build();
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> resp = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        System.out.println("Yandex API: " + resp.body());

        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.body());
        com.fasterxml.jackson.databind.JsonNode totals = root.path("totals");

        if (totals == null || !totals.isArray() || totals.size() < 4) {
            throw new Exception("Нет всех метрик в totals: " + resp.body());
        }

        int visits   = (int) Math.round(totals.get(0).asDouble());
        int pageviews= (int) Math.round(totals.get(1).asDouble());
        int users    = (int) Math.round(totals.get(2).asDouble());
        int calls    = (int) Math.round(totals.get(3).asDouble());
        return new MetrikaStats(visits, pageviews, users, calls, date);
    }
}
package ru.fixbyte.monitoring.metrika;

public class MetrikaStats {
    public final int visits;
    public final int pageviews;
    public final int users;
    public final String date;

    public MetrikaStats(int visits, int pageviews, int users, String date) {
        this.visits = visits;
        this.pageviews = pageviews;
        this.users = users;
        this.date = date;
    }
}
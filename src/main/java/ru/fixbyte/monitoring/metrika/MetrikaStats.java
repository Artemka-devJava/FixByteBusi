package ru.fixbyte.monitoring.metrika;

public class MetrikaStats {
    public final int visits;
    public final int pageviews;
    public final int users;
    public final int calls;
    public final String date;

    public MetrikaStats(int visits, int pageviews, int users, int calls, String date) {
        this.visits = visits;
        this.pageviews = pageviews;
        this.users = users;
        this.calls = calls;
        this.date = date;
    }
}
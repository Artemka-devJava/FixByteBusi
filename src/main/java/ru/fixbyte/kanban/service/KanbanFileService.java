package ru.fixbyte.kanban.service;

import ru.fixbyte.kanban.model.KanbanCardModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KanbanFileService {

    public List<KanbanCardModel> loadCardsFromFile(String path) {
        List<KanbanCardModel> result = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return result;
        try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String[] sp = sc.nextLine().split("\\|", 6);
                if (sp.length < 6) continue;
                String column = unescape(sp[0]);
                String name = unescape(sp[1]);
                String contacts = unescape(sp[2]);
                String price = unescape(sp[3]);
                String task = unescape(sp[4]);
                boolean paid = "1".equals(sp[5]);
                result.add(new KanbanCardModel(column, name, contacts, price, paid));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void saveCardsToFile(List<KanbanCardModel> cards, String path) {
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (KanbanCardModel c : cards) {
                writer.println(escape(c.column) + "|" + escape(c.name) + "|" + escape(c.contacts) + "|" +
                        escape(c.price) + "|" + escape(c.task) + "|" + (c.paid ? "1" : "0"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void archiveCard(KanbanCardModel card, String archivePath) {
        File file = new File(archivePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file, true), true, StandardCharsets.UTF_8)) {
            writer.println(escape(card.column) + "|" + escape(card.name) + "|" + escape(card.contacts) + "|" +
                    escape(card.price) + "|" + escape(card.task) + "|" + (card.paid ? "1" : "0"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String escape(String s) { return s == null ? "" : s.replace("|", "%7C").replace("\n", "\\n"); }
    private String unescape(String s) { return s.replace("%7C", "|").replace("\\n", "\n"); }
}
package me.rexi.customprefixes;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhook {

    private final String webhookUrl;
    private final String avatarUrl;
    private final String username;
    private final String title;
    private final String description;
    private final String content;
    private final int embedColor;

    public DiscordWebhook(FileConfiguration config) {
        this.webhookUrl = config.getString("discord-webhook.url", "");
        this.avatarUrl = config.getString("discord-webhook.avatar", "");
        this.username = config.getString("discord-webhook.username", "");
        this.title = config.getString("discord-webhook.embed.title", "Tag Changed");
        this.description = config.getString("discord-webhook.embed.description", "{player} has changed his tag to {tag}");
        this.content = config.getString("discord-webhook.content", "");

        int red = config.getInt("discord-webhook.embed.color.red", 0);
        int green = config.getInt("discord-webhook.embed.color.green", 0);
        int blue = config.getInt("discord-webhook.embed.color.blue", 0);

        // Convert RGB to HEX
        this.embedColor = (red << 16) | (green << 8) | blue;
    }

    public void send(Player player, String newTag) {
        if (player == null) {
            return;
        }
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String playerName = player.getName();
            String descriptionWithPlayer = description.replace("{player}", playerName).replace("{tag}", newTag);
            String formattedDescription = PlaceholderAPI.setPlaceholders(player, descriptionWithPlayer);
            String formattedTitle = PlaceholderAPI.setPlaceholders(player, title);
            String formattedContent = PlaceholderAPI.setPlaceholders(player, content);

            // Build JSON payload
            String embedPayload = String.format(
                    "{\"username\": \"%s\", \"avatar_url\": \"%s\", \"content\": \"%s\", \"embeds\": [{\"title\": \"%s\", \"description\": \"%s\", \"color\": %d}]}",
                    escapeJson(username), escapeJson(avatarUrl), escapeJson(formattedContent), escapeJson(formattedTitle), escapeJson(formattedDescription), embedColor
            );

            try (OutputStream os = connection.getOutputStream()) {
                os.write(embedPayload.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                System.out.println("Failed to send webhook. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
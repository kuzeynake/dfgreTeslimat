package me.dfgre.teslimat.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.dfgre.teslimat.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TeslimatExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "teslimat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "dfgre";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("tarim")) {
            return player == null ? "0" : String.valueOf(Main.getInstance().getData().getInt("Players." + player.getUniqueId() + ".tarim_toplam", 0));
        }

        if (params.equalsIgnoreCase("uretim")) {
            return player == null ? "0" : String.valueOf(Main.getInstance().getData().getInt("Players." + player.getUniqueId() + ".uretim_toplam", 0));
        }

        if (params.startsWith("top_tarim_")) {
            try {
                int rank = Integer.parseInt(params.replace("top_tarim_", ""));
                return getRankedStat("tarim", rank);
            } catch (Exception e) {
                return "";
            }
        }

        if (params.startsWith("top_uretim_")) {
            try {
                int rank = Integer.parseInt(params.replace("top_uretim_", ""));
                return getRankedStat("uretim", rank);
            } catch (Exception e) {
                return "";
            }
        }

        return null;
    }

    private String getRankedStat(String type, int rank) {
        Map<String, Integer> dataMap = new HashMap<>();
        if (Main.getInstance().getData().getConfigurationSection("Players") != null) {
            for (String key : Main.getInstance().getData().getConfigurationSection("Players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    int val = Main.getInstance().getData().getInt("Players." + key + "." + type + "_toplam", 0);
                    if (name != null && val > 0) {
                        dataMap.put(name, val);
                    }
                } catch (Exception ignored) {}
            }
        }

        List<Map.Entry<String, Integer>> sorted = dataMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (rank <= 0 || rank > sorted.size()) {
            return "§b" + rank + ". Boş: §f0";
        }

        Map.Entry<String, Integer> entry = sorted.get(rank - 1);
        return "§b" + rank + ". " + entry.getKey() + ": §f" + entry.getValue();
    }
}
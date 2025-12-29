package me.dfgre.teslimat.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.dfgre.teslimat.Main;
import java.util.*;
import java.util.stream.Collectors;

public class TeslimatKomutu implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                Main.getInstance().reloadPluginConfig();
                sender.sendMessage("§a§ldfgreTeslimat §8» §7Konfigürasyon dosyaları yenilendi.");
            }
            return true;
        }

        if (args.length == 0) {
            openAnaMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("teslimat.admin")) {
                player.sendMessage("§cBu komutu kullanmak için yetkiniz yok.");
                return true;
            }
            Main.getInstance().reloadPluginConfig();
            player.sendMessage("§a§ldfgreTeslimat §8» §7Konfigürasyon başarıyla yenilendi.");
            return true;
        }

        openAnaMenu(player);
        return true;
    }

    public static void openAnaMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Menü » Teslimat");
        for (int i = 0; i < 27; i++) gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>()));
        
        String tarimMatStr = Main.getInstance().getConfig().getString("siparisler.tarim.materyal", "CARROT");
        Material tarimIcon = Material.getMaterial(tarimMatStr.toUpperCase());
        if (tarimIcon == null) tarimIcon = Material.WHEAT;

        String uretimMatStr = Main.getInstance().getConfig().getString("siparisler.uretim.materyal", "BOW");
        Material uretimIcon = Material.getMaterial(uretimMatStr.toUpperCase());
        if (uretimIcon == null) uretimIcon = Material.CRAFTING_TABLE;

        gui.setItem(11, createItem(tarimIcon, "§6Tarım Kategorisi", List.of("§7Tıkla ve teslimat yap.")));
        gui.setItem(15, createItem(uretimIcon, "§eÜretim Kategorisi", List.of("§7Tıkla ve teslimat yap.")));
        player.openInventory(gui);
    }

    public static void openTarimMenu(Player player) {
        openKategoriMenu(player, "tarim", "Tarım Kategorisi", Material.ENCHANTED_BOOK);
    }

    public static void openUretimMenu(Player player) {
        openKategoriMenu(player, "uretim", "Üretim Kategorisi", Material.ENCHANTED_BOOK);
    }

    private static void openKategoriMenu(Player player, String cat, String title, Material rewardIcon) {
        Inventory gui = Bukkit.createInventory(null, 45, "Menü » " + title);
        for (int i = 0; i < 45; i++) gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>()));
        
        List<String> odulLore = Main.getInstance().getConfig().getStringList("siparisler." + cat + ".siralama-odulleri").stream()
                .map(s -> s.replace("&", "§")).collect(Collectors.toList());

        gui.setItem(0, createItem(rewardIcon, "§bSıralama Ödülleri", odulLore));
        gui.setItem(21, createItem(Material.CAULDRON, "§aEşyaları Teslim Et", List.of("§7Envanterdeki eşyaları teslim et.")));
        gui.setItem(23, createItem(Material.PAPER, "§6Sıralama", List.of("§7En çok teslim edenleri gör.")));
        
        setupSiparis(gui, player, 30, cat, "birinci", "§e1. Sipariş", Material.YELLOW_CONCRETE);
        setupSiparis(gui, player, 31, cat, "ikinci", "§a2. Sipariş", Material.LIME_CONCRETE);
        setupSiparis(gui, player, 32, cat, "ucuncu", "§c3. Sipariş", Material.RED_CONCRETE);
        player.openInventory(gui);
    }

    private static void setupSiparis(Inventory gui, Player player, int slot, String cat, String key, String name, Material icon) {
        int current = Main.getInstance().getData().getInt("Players." + player.getUniqueId() + "." + cat + "_toplam", 0);
        int target = Main.getInstance().getConfig().getInt("siparisler." + cat + "." + key + ".hedef");
        boolean completed = Main.getInstance().getData().getBoolean("Players." + player.getUniqueId() + "." + cat + "_" + key + "_done", false);
        String status = completed ? "§aTAMAMLANDI" : (current >= target ? "§e§lTIKLA VE ÖDÜLÜ AL" : "§7Hedefe ulaşınca tıkla.");
        gui.setItem(slot, createItem(icon, name, List.of("§fİlerleme: §a" + current + "/" + target, "", status)));
    }

    public static void openSiralamaMenu(Player player, String cat) {
        String menuTitle = cat.equals("tarim") ? "Menü » Tarım Sıralaması" : "Menü » Üretim Sıralaması";
        Inventory gui = Bukkit.createInventory(null, 27, menuTitle);
        for (int i = 0; i < 27; i++) gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>()));
        
        Map<String, Integer> dataMap = new HashMap<>();
        if (Main.getInstance().getData().getConfigurationSection("Players") != null) {
            for (String key : Main.getInstance().getData().getConfigurationSection("Players").getKeys(false)) {
                String name = Bukkit.getOfflinePlayer(UUID.fromString(key)).getName();
                int val = Main.getInstance().getData().getInt("Players." + key + "." + cat + "_toplam", 0);
                dataMap.put(name != null ? name : "Bilinmiyor", val);
            }
        }
        
        List<Map.Entry<String, Integer>> sorted = dataMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10).collect(Collectors.toList());
        
        int slot = 10, rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            gui.setItem(slot++, createItem(Material.PLAYER_HEAD, "§e#" + rank + " " + entry.getKey(), List.of("§fToplam: §a" + entry.getValue())));
            rank++;
        }
        player.openInventory(gui);
    }

    public static void openOnayMenu(Player player, String cat) {
        Inventory gui = Bukkit.createInventory(null, 27, "Menü » Onay");
        for (int i = 0; i < 27; i++) gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>()));
        
        ItemStack evet = createItem(Material.LIME_TERRACOTTA, "§aEVET", List.of("§7Eşyaları teslim et."));
        ItemMeta metaE = evet.getItemMeta();
        if (metaE != null) {
            metaE.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "cat"), PersistentDataType.STRING, cat);
            evet.setItemMeta(metaE);
        }

        gui.setItem(11, evet);
        gui.setItem(15, createItem(Material.RED_TERRACOTTA, "§cHAYIR", List.of("§7Vazgeç.")));
        player.openInventory(gui);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
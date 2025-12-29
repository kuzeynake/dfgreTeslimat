package me.dfgre.teslimat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.dfgre.teslimat.Main;
import me.dfgre.teslimat.commands.TeslimatKomutu;
import me.dfgre.teslimat.utils.UpdateChecker;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("»")) return;
        
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        
        Player player = (Player) event.getWhoClicked();
        Material clicked = event.getCurrentItem().getType();

        if (title.equals("Menü » Teslimat")) {
            String tarimMatStr = Main.getInstance().getConfig().getString("siparisler.tarim.materyal", "CARROT");
            Material tarimIcon = Material.getMaterial(tarimMatStr.toUpperCase());
            String uretimMatStr = Main.getInstance().getConfig().getString("siparisler.uretim.materyal", "BOW");
            Material uretimIcon = Material.getMaterial(uretimMatStr.toUpperCase());

            if (clicked == tarimIcon || (tarimIcon == null && clicked == Material.WHEAT)) {
                TeslimatKomutu.openTarimMenu(player);
            } else if (clicked == uretimIcon || (uretimIcon == null && clicked == Material.CRAFTING_TABLE)) {
                TeslimatKomutu.openUretimMenu(player);
            }
        } 
        else if (title.contains("Kategorisi")) {
            String cat = title.contains("Tarım") ? "tarim" : "uretim";
            
            if (clicked == Material.CAULDRON) {
                TeslimatKomutu.openOnayMenu(player, cat);
            } else if (clicked == Material.PAPER) {
                TeslimatKomutu.openSiralamaMenu(player, cat);
            } else {
                checkAndReward(player, clicked, Material.YELLOW_CONCRETE, cat, "birinci");
                checkAndReward(player, clicked, Material.LIME_CONCRETE, cat, "ikinci");
                checkAndReward(player, clicked, Material.RED_CONCRETE, cat, "ucuncu");
            }
        }
        else if (title.equals("Menü » Onay")) {
            ItemStack item = event.getCurrentItem();
            if (item.getItemMeta() == null) return;
            
            NamespacedKey key = new NamespacedKey(Main.getInstance(), "cat");
            String cat = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            
            if (clicked == Material.LIME_TERRACOTTA) {
                handleDelivery(player, cat);
                player.closeInventory();
            } else if (clicked == Material.RED_TERRACOTTA) {
                if (cat != null && cat.equals("tarim")) {
                    TeslimatKomutu.openTarimMenu(player);
                } else {
                    TeslimatKomutu.openUretimMenu(player);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            new UpdateChecker(Main.getInstance(), 131178).getVersion(version -> {
                if (!Main.getInstance().getDescription().getVersion().equals(version)) {
                    event.getPlayer().sendMessage("§a§ldfgreTeslimat §8» §eYeni bir güncelleme mevcut! §7(v" + version + ")");
                    event.getPlayer().sendMessage("§a§ldfgreTeslimat §8» §fBuradan indirebilirsin: §nhttps://www.spigotmc.org/resources/131178/");
                }
            });
        }
    }

    private void handleDelivery(Player p, String cat) {
        if (cat == null) return;
        String matName = Main.getInstance().getConfig().getString("siparisler." + cat + ".materyal");
        if (matName == null) return;
        
        Material target = Material.getMaterial(matName.toUpperCase());
        if (target == null) return;

        int count = 0;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == target) {
                count += item.getAmount();
                p.getInventory().setItem(i, null);
            }
        }

        if (count > 0) {
            int cur = Main.getInstance().getData().getInt("Players." + p.getUniqueId() + "." + cat + "_toplam", 0);
            Main.getInstance().getData().set("Players." + p.getUniqueId() + "." + cat + "_toplam", cur + count);
            Main.getInstance().saveData();
            p.sendMessage("§a" + count + " adet eşya teslim edildi!");
        } else {
            p.sendMessage("§cEnvanterinde teslim edilecek eşya bulunamadı!");
        }
    }

    private void checkAndReward(Player p, Material clicked, Material icon, String cat, String key) {
        if (clicked == icon) {
            int cur = Main.getInstance().getData().getInt("Players." + p.getUniqueId() + "." + cat + "_toplam", 0);
            int tar = Main.getInstance().getConfig().getInt("siparisler." + cat + "." + key + ".hedef");
            boolean done = Main.getInstance().getData().getBoolean("Players." + p.getUniqueId() + "." + cat + "_" + key + "_done", false);
            
            if (!done && cur >= tar) {
                Main.getInstance().getData().set("Players." + p.getUniqueId() + "." + cat + "_" + key + "_done", true);
                Main.getInstance().saveData();
                
                String cmd = Main.getInstance().getConfig().getString("siparisler." + cat + "." + key + ".odul-komut");
                if (cmd != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));
                }
                
                String msg = Main.getInstance().getConfig().getString("siparisler." + cat + "." + key + ".mesaj");
                if (msg != null) {
                    p.sendMessage(msg.replace("&", "§"));
                }
                p.closeInventory();
            }
        }
    }
}
package me.dfgre.teslimat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import me.dfgre.teslimat.commands.TeslimatKomutu;
import me.dfgre.teslimat.listeners.MenuListener;
import me.dfgre.teslimat.utils.TeslimatExpansion;
import me.dfgre.teslimat.utils.UpdateChecker;
import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin {

    private static Main instance;
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("---------------------------------------");
        getLogger().info("Bu plugin dfgre tarafından ücretsiz olarak yapılmıştır.");
        getLogger().info("Herhangi bir sorunda veya güncellemeler hakkında");
        getLogger().info("bilgi almak için discord: dfgre yazabilirsiniz.");
        getLogger().info("---------------------------------------");
        
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();
        createDataFile();
        
        if (getCommand("teslimat") != null) {
            getCommand("teslimat").setExecutor(new TeslimatKomutu());
        }
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TeslimatExpansion().register();
        }

        new UpdateChecker(this, 131178).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning("[dfgreTeslimat] Yeni bir guncelleme bulundu: v" + version);
                getLogger().warning("[dfgreTeslimat] Indir: https://www.spigotmc.org/resources/131178/");
            }
        });
    }

    public void reloadPluginConfig() {
        reloadConfig();
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public FileConfiguration getData() {
        return dataConfig;
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }
}
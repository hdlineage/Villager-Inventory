package ca.henrychang.villagerinventory;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class VillagerInventory extends JavaPlugin {

    VillagerInvEventHandler eventHandler;

    HashMap<UUID, Villager> invMap;

    Material interactMaterial = Material.STICK;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getConsoleSender().sendMessage("Villager Inventory Plugin Version 1.0");
        getServer().getConsoleSender().sendMessage("Villager Inventory Plugin Starting...");

        String key = "Interactive-Material";
        if (!getConfig().contains(key) || Material.matchMaterial(getConfig().getString(key)) == null)
        {
            getConfig().set(key, Material.STICK.toString());
            saveConfig();
        }else{
            interactMaterial = Material.matchMaterial(getConfig().getString(key));
        }

        eventHandler = new VillagerInvEventHandler(this);
        getServer().getPluginManager().registerEvents(eventHandler, this);
        invMap = new HashMap<UUID, Villager>();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getConsoleSender().sendMessage("Villager Inventory Plugin Stopping...");
        invMap.clear();
    }
}

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
    boolean dropOnDeath = true;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getConsoleSender().sendMessage("Villager Inventory Plugin Version 2.5");
        getServer().getConsoleSender().sendMessage("Villager Inventory Plugin Starting...");

        String key_material = "Interactive-Material";
        String key_dropOnDeath ="Drop-On-Death";
        if (!getConfig().contains(key_material) || Material.matchMaterial(getConfig().getString(key_material)) == null) {
            getConfig().set(key_material, interactMaterial.toString());
            saveConfig();
        }else {
            interactMaterial = Material.matchMaterial(getConfig().getString(key_material));
        }

        if(!getConfig().contains(key_dropOnDeath)) {
            getConfig().set(key_dropOnDeath, dropOnDeath);
            saveConfig();
        }else {
            dropOnDeath = getConfig().getBoolean(key_dropOnDeath);
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

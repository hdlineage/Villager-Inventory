package ca.henrychang.villagerinventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VillagerInvEventHandler implements Listener {
    VillagerInventory plugin;

    public VillagerInvEventHandler(VillagerInventory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();

        if(player.getInventory().getItemInMainHand().getType() != plugin.interactMaterial)
            return;

        if(player == null)
            return;

        Entity villager = e.getRightClicked();
        if(villager == null || villager.getType() != EntityType.VILLAGER)
            return;

        e.setCancelled(true);
        Villager v = (Villager) villager;
        Inventory i = v.getInventory();
        Inventory ni = Bukkit.createInventory(player, 9, "Villager Inventory");
        for (ItemStack item : i.getContents()){
            if (item != null)
                ni.addItem(item);
        }
        //Fill last slot
        ItemStack lastItem = new ItemStack(Material.BARRIER);
        ni.setItem(8, lastItem);
        player.openInventory(ni);
        plugin.invMap.put(player.getUniqueId(), v);
    }

    @EventHandler
    public void onClick(InventoryClickEvent evt) {
        Inventory v = evt.getInventory();

        if(v == null)
            return;

        if(v.getSize() != 9 || v.getType() != InventoryType.CHEST || evt.getView().getTitle() != "Villager Inventory")
            return;

        HumanEntity human = evt.getWhoClicked();
        final int slot = evt.getSlot();

        if(slot > 8 || slot < 0)
            return;

        Villager villager = plugin.invMap.get(human.getUniqueId());
        if(villager == null)
            return;

        if(evt.getCurrentItem() != null)
            if(evt.getCurrentItem().getType() == Material.BARRIER){
                evt.setCancelled(true);
                return;
            }

        Bukkit.getScheduler().runTask(plugin, new InvUpdater(v, villager.getInventory(), (Player) human));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent evt) {
        Inventory v = evt.getInventory();
        if(v == null)
            return;
        if(v.getSize() != 9 || v.getType() != InventoryType.CHEST || evt.getView().getTitle() != "Villager Inventory")
            return;
        HumanEntity human = evt.getWhoClicked();
        Villager villager = plugin.invMap.get(human.getUniqueId());
        if(villager == null)
            return;

        Bukkit.getScheduler().runTask(plugin, new InvUpdater(v, villager.getInventory(), (Player) human));
    }


    @EventHandler
    public void onClose(InventoryCloseEvent evt){
        Inventory v = evt.getInventory();
        if(v == null)
            return;
        if(v.getSize() != 9 || v.getType() != InventoryType.CHEST || evt.getView().getTitle() != "Villager Inventory")
            return;
        HumanEntity human = evt.getPlayer();
        if(evt.getPlayer() == null)
            return;
        Villager villager = plugin.invMap.get(human.getUniqueId());
        if(villager == null)
            return;

        Bukkit.getScheduler().runTask(plugin, new InvUpdater(v, villager.getInventory(), (Player) human));
        plugin.invMap.remove(evt.getPlayer().getUniqueId());
    }

    private class InvUpdater implements Runnable {
        Inventory sourceInv, targetInv;
        boolean needReStack;
        Player player;

        public InvUpdater(Inventory sourceInv, Inventory targetInv, Player player) {
            this(sourceInv, targetInv, player,true);
        }

        public InvUpdater(Inventory sourceInv, Inventory targetInv, Player player, boolean needReStack){
            this.sourceInv = sourceInv;
            this.targetInv = targetInv;
            this.needReStack = needReStack;
            this.player = player;
        }

        @Override
        public void run() {
            for(int i = 0; i < 8; i++)
                targetInv.setItem(i, sourceInv.getItem(i));
            if(needReStack){
                restackInv(targetInv);
                Bukkit.getScheduler().runTask(plugin, new InvUpdater(targetInv, sourceInv, player, false));
            }
        }

        private void restackInv(Inventory inv){
            boolean changed = false;
            loop1: for(int i = 0; i < 8; i++) {
                ItemStack cur = inv.getItem(i);
                //get rid of empty slots between items
                if(cur == null){
                    for(int j = i + 1; j < 8; j++)
                        if(inv.getItem(j) != null) {
                            inv.setItem(i, inv.getItem(j));
                            inv.setItem(j, null);
                            changed = true;
                            continue loop1;
                        }
                    continue loop1;
                }

                //recombine stacks
                if(cur.getAmount() < cur.getMaxStackSize())
                    loop2: for(int j = i + 1; j < 8; j++)
                        if(inv.getItem(j) != null && inv.getItem(j).isSimilar(cur)){
                            int jSize = inv.getItem(j).getAmount();
                            int newSize = cur.getAmount() + jSize;
                            if (newSize > cur.getMaxStackSize()){
                                inv.getItem(j).setAmount(jSize - (cur.getMaxStackSize() - cur.getAmount()));
                                cur.setAmount(cur.getMaxStackSize());
                                changed = true;
                                break loop2;
                            }else {
                                cur.setAmount(cur.getAmount() + jSize);
                                inv.setItem(j,null);
                            }
                            changed = true;
                        }
            }
            if(changed)
                restackInv(inv);
        }
    }

}

package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.BukkitUtils.compareInventories;
import static de.diddiz.util.BukkitUtils.compressInventory;
import static de.diddiz.util.BukkitUtils.getInventoryHolderLocation;
import static de.diddiz.util.BukkitUtils.getInventoryHolderType;
import static de.diddiz.util.BukkitUtils.rawData;

import java.util.HashMap;
import java.util.Map;

import de.diddiz.LogBlock.Logging;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.config.Config;

public class ChestAccessLogging extends LoggingListener
{
	private final Map<HumanEntity, ItemStack[]> containers = new HashMap<HumanEntity, ItemStack[]>();

	public ChestAccessLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {

		if (!isLogging(event.getPlayer().getWorld(), Logging.CHESTACCESS)) return;
		InventoryHolder holder = event.getInventory().getHolder();
		// Nop, screw this line! just log all machines with an inventory :)
		//if (holder instanceof BlockState || holder instanceof DoubleChest) 
		{
			final HumanEntity player = event.getPlayer();
			final ItemStack[] before = containers.get(player);
			if (before != null) {
				final ItemStack[] after = compressInventory(event.getInventory().getContents());
				final ItemStack[] diff = compareInventories(before, after);
				
				// Huge problem here, some machines with inventory may not report their
				// location correctly so instead of fixing every single of these mods
				// we will just add ChestAccess to the block the player is looking at.
				// (HotFix also implemented in BukkitUtils.java)
								
				Location loc = getInventoryHolderLocation(holder);
				int itemTypeId = 0;
				if (!(holder instanceof BlockState || holder instanceof DoubleChest) && Config.customBlockIds.contains(event.getPlayer().getTargetBlock(null,200).getTypeId()))
				{
					loc = event.getPlayer().getTargetBlock(null,200).getLocation();
				}
				
				itemTypeId = loc.getWorld().getBlockTypeIdAt(loc);
				//System.out.println("itemTypeId" + itemTypeId);		
				//System.out.println("data" + loc.getWorld().getBlockAt(loc).getData());		
				// Satefy check
				if(itemTypeId != 0 && loc != null)
				{
					for (ItemStack item : diff) {
						  //System.out.println(player.getName());
						  //System.out.println("Loc:" + loc.getX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
						  //System.out.println("itemTypeId" + itemTypeId);
						  //System.out.println(item.getAmount());
						  //System.out.println(rawData(item));
						  this.consumer.queueChestAccess(player.getName(), loc, itemTypeId, (short)item.getTypeId(), (short)item.getAmount(), rawData(item));
				    }
				}
				containers.remove(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		
		if (!isLogging(event.getPlayer().getWorld(), Logging.CHESTACCESS)) return;
        if (event.getInventory() != null) {
                InventoryHolder holder = event.getInventory().getHolder();
                if (holder instanceof BlockState || holder instanceof DoubleChest || Config.customBlockIds.contains(event.getPlayer().getTargetBlock(null,100).getTypeId())) {
                        if (getInventoryHolderType(holder) != 58) {
                        	// Not a fix, temporary
                        	try{
                                containers.put(event.getPlayer(), compressInventory(event.getInventory().getContents()));
                        	}
                        	finally{}
                        }
                }
        }
	}
}

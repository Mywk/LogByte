package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.BukkitUtils.compareInventories;
import static de.diddiz.util.BukkitUtils.compressInventory;
import static de.diddiz.util.BukkitUtils.getInventoryHolderLocation;
import static de.diddiz.util.BukkitUtils.getInventoryHolderType;
import static de.diddiz.util.BukkitUtils.rawData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.diddiz.LogBlock.Logging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
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

	// This is an attempt to log Interaction with machines, note that it is NOT precise, for example
	// if someone drops an item by that person while he interacts with the machine that WILL be logged as
	// if it was an interaction with the said machine. It's not precise but way better than no logs at all.
	private final Map<HumanEntity, ItemStack[]> specialContainers = new HashMap<HumanEntity, ItemStack[]>();

	public ChestAccessLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {

		if (!isLogging(event.getPlayer().getWorld(), Logging.CHESTACCESS)) return;
		InventoryHolder holder = event.getInventory().getHolder();
		{
			final HumanEntity player = event.getPlayer();
			final ItemStack[] specialBefore = specialContainers.get(player);
			final ItemStack[] before = containers.get(player);
			if (specialBefore != null) {
				final ItemStack[] after = compressInventory(event.getPlayer().getInventory().getContents());
				final ItemStack[] diff = compareInventories(after,specialBefore);

				int itemTypeId = consumer.lastClickedBlock.get(event.getPlayer()).getTypeId();
				Location loc = consumer.lastClickedBlock.get(event.getPlayer()).getLocation();
				if(itemTypeId != 0 && loc != null)
				{
					for (ItemStack item : diff) {
						  this.consumer.queueChestAccess(player.getName(), loc, itemTypeId, (short)item.getTypeId(), (short)item.getAmount(), rawData(item));
				    }
				}
				specialContainers.remove(player);
				
			}
			else if (before != null) {
				final ItemStack[] after = compressInventory(event.getInventory().getContents());
				final ItemStack[] diff = compareInventories(before, after);
						
				Location loc = getInventoryHolderLocation(holder);
				if (!(holder instanceof BlockState || holder instanceof DoubleChest))
				{
					loc = consumer.lastClickedBlock.get(event.getPlayer()).getLocation();
				}
				
				int itemTypeId = consumer.lastClickedBlock.get(event.getPlayer()).getTypeId();
				// Safety check
				if(itemTypeId != 0 && loc != null)
				{
					for (ItemStack item : diff) {
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

		if(event.getPlayer() == null || !(Arrays.asList(Bukkit.getOnlinePlayers()).contains(event.getPlayer())))
		return;
		
        if (event.getInventory() != null) {
                InventoryHolder holder = event.getInventory().getHolder();
                if (holder instanceof BlockState || holder instanceof DoubleChest) {
                        if (getInventoryHolderType(holder) != 58) {
                        	try
                        	{
                                containers.put(event.getPlayer(), compressInventory(event.getInventory().getContents()));
                        	}
                        	catch(java.lang.AbstractMethodError | Exception e)
                        	{        	
                        		if(Config.logAllMachines)
                        			specialContainers.put(event.getPlayer(), compressInventory(event.getPlayer().getInventory().getContents()));
                        	}
                        }
                }
                else if(Config.logAllMachines)
                {
                    specialContainers.put(event.getPlayer(), compressInventory(event.getPlayer().getInventory().getContents()));
                }
        }
	}
}

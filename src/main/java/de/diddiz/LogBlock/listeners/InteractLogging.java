package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;

import java.util.List;

import de.diddiz.util.BukkitUtils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.config.WorldConfig;


public class InteractLogging extends LoggingListener
{
	public InteractLogging(LogBlock lb) {
		super(lb);
	}
	
	// !!!!!! TO ADD, POWER DRILL AREA
	

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final WorldConfig wcfg = getWorldConfig(event.getPlayer().getWorld());
		if (wcfg != null) {
			
			// Mywk wrench and powertool logging, ignores world settings (logs all worlds)
			if (Config.wrenchLog && event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				final Player player = event.getPlayer();
				final Block clicked = event.getClickedBlock();	
				

				if(Config.wrenchIds.contains(player.getItemInHand().getTypeId()))
				{
					final BlockState state = clicked.getState();

					final Location loc = new Location(state.getWorld(), state.getX(), state.getY(), state.getZ());
					final int id = state.getTypeId();
					final byte b = state.getRawData();
					
					LogBlock.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(LogBlock.getInstance(), new Runnable() {
	                    public void run() {
	                        if(clicked.getTypeId() == 0)
	                        {
	                        	consumer.queueBlockBreakDelayed(player.getName(), loc, id,b);
	                        }
	                        	
	                    }
	                  }, 2L); // 2 should be enough, more may cause the block not to be logged
				}
				
				// Log Force Wrench place event
				if(player.getItemInHand().getTypeId() == Config.forceWrenchPlaceId)
				{	
					
					BlockFace face = event.getBlockFace();
					final Location where = clicked.getRelative(face).getLocation();
					
					LogBlock.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(LogBlock.getInstance(), new Runnable() {
	                
						public void run() {
	                    	
	                    	if(where.getBlock().getTypeId() != 0) // Not air
	                    	{
	                    		// Must be sure
	                    		BlockState placedBlock = where.getBlock().getState();
	                    		consumer.queueBlockPlace(player.getName(), placedBlock);
	                    	}
	                        	
	                    }
	                  }, 1L);
                    	
				}
				
				
			}
			
			if(wcfg.isLogging(Logging.SWITCHINTERACT) || wcfg.isLogging(Logging.DOORINTERACT) || wcfg.isLogging(Logging.CAKEEAT) || wcfg.isLogging(Logging.NOTEBLOCKINTERACT) || wcfg.isLogging(Logging.DIODEINTERACT) || wcfg.isLogging(Logging.COMPARATORINTERACT) || wcfg.isLogging(Logging.PRESUREPLATEINTERACT) || wcfg.isLogging(Logging.TRIPWIREINTERACT) || wcfg.isLogging(Logging.CROPTRAMPLE))
			{
			
				final Player player = event.getPlayer();
				final Block clicked = event.getClickedBlock();	
				final Material type = clicked.getType();
				final int typeId = type.getId();
				final byte blockData = clicked.getData();
				final Location loc = clicked.getLocation();
				
				switch (type) {
					case LEVER:
					case WOOD_BUTTON:
					case STONE_BUTTON:
						if (wcfg.isLogging(Logging.SWITCHINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK)
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						break;
					case FENCE_GATE:
					case WOODEN_DOOR:
					case TRAP_DOOR:
						if (wcfg.isLogging(Logging.DOORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK)
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						break;
					case CAKE_BLOCK:
						if (wcfg.isLogging(Logging.CAKEEAT) && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getFoodLevel() < 20)
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						break;
					case NOTE_BLOCK:
						if (wcfg.isLogging(Logging.NOTEBLOCKINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK)
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						break;
					case DIODE_BLOCK_OFF:
					case DIODE_BLOCK_ON:
						if (wcfg.isLogging(Logging.DIODEINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK)
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						break;
					case REDSTONE_COMPARATOR_OFF:
					case REDSTONE_COMPARATOR_ON:
						if (wcfg.isLogging(Logging.COMPARATORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						}
						break;
					case WOOD_PLATE:
					case STONE_PLATE:
					case IRON_PLATE:
					case GOLD_PLATE:
						if (wcfg.isLogging(Logging.PRESUREPLATEINTERACT) && event.getAction() == Action.PHYSICAL) {
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						}
						break;
					case TRIPWIRE:
						if (wcfg.isLogging(Logging.TRIPWIREINTERACT) && event.getAction() == Action.PHYSICAL) {
							consumer.queueBlock(player.getName(), loc, typeId, typeId, blockData);
						}
						break;
					case SOIL:
						if (wcfg.isLogging(Logging.CROPTRAMPLE) && event.getAction() == Action.PHYSICAL) {
							// 3 = Dirt ID
							consumer.queueBlock(player.getName(), loc, typeId, 3, blockData);
							// Log the crop on top as being broken
							Block trampledCrop = clicked.getRelative(BlockFace.UP);
							if (BukkitUtils.getCropBlocks().contains(trampledCrop.getType())) {
								consumer.queueBlockBreak(player.getName(), trampledCrop.getState());
							}
						}
						break;
						
				}
			}
		}
	}
	
	// This is specific for the Lux capacitor
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerInteractAir(PlayerInteractEvent event) {
		
		if (Config.powertoolLog && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR))
		{

			if(event.getPlayer().getItemInHand().getTypeId() == Config.powertoolId)
			{
		
				final Location loc = event.getPlayer().getTargetBlock(null, 110).getLocation();
				final Player player = event.getPlayer();
				
				LogBlock.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(LogBlock.getInstance(), new Runnable() {
		            public void run() {
		
		        		// Lets not overload the server with Math and foreach the BlockFaces          
		        		//(float)Math.toDegrees(Math.atan2(event.getPlayer().getLocation().getBlockX() - theBlock.getX(), theBlock.getZ() - event.getPlayer().getLocation().getBlockZ()));
		        	
		            	Block theBlock = loc.getBlock();

		            	for (BlockFace f : BlockFace.values()) {
		            		if(theBlock.getRelative(f).getTypeId() == Config.luxcapacitorId)
		            		{
		            			consumer.queueBlockPlace(player.getName(), theBlock.getRelative(f).getLocation(), theBlock.getRelative(f).getState().getTypeId(),theBlock.getRelative(f).getState().getRawData());
		            			break;
		            		}
		            	}
		                	
		            }
		          }, 600L); // check 30 seconds later (600 ticks), max time the lux capacitor needs to travel, won't also log if player removes it within the time left
				
			}

	}
	}
	
	
}

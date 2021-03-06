package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.LoggingUtil.smartLogBlockBreak;
import static de.diddiz.util.LoggingUtil.smartLogFallables;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;

public class BlockBreakLogging extends LoggingListener
{
	public BlockBreakLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (isLogging(event.getBlock().getWorld(), Logging.BLOCKBREAK)) {
			WorldConfig wcfg = getWorldConfig(event.getBlock().getWorld());
			if (wcfg == null) return;

			final String playerName = event.getPlayer().getName();
			final Block origin = event.getBlock();
			final int typeId = origin.getTypeId();
			final Material type = origin.getType();

			if (wcfg.isLogging(Logging.SIGNTEXT) && (typeId == 63 || typeId == 68)) {
				consumer.queueSignBreak(playerName, (Sign) origin.getState());
				// Mywk hotfix
			} else if (wcfg.isLogging(Logging.CHESTACCESS) && (BukkitUtils.getContainerBlocks().contains(type))) {
				consumer.queueContainerBreak(playerName, origin.getState());
			} else if (type == Material.ICE) {
				// When in creative mode ice doesn't form water
				if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
					consumer.queueBlockBreak(playerName, origin.getState());
				} else {
					consumer.queueBlockReplace(playerName, origin.getState(), 9, (byte) 0);
				}
			}
			// Force powertool log even on hidden blocks
			else if (Config.forcepowerdrillLog && event.getPlayer().getItemInHand().getTypeId() == Config.powerdrillId)
			{
				// Log the middle block only to avoid block calculation lag and a huge list of logs
				org.bukkit.block.BlockState before = origin.getState();
				consumer.queueBlockForce(playerName, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getTypeId(), 0, before.getRawData(), null, null);

			}
			else {
				smartLogBlockBreak(consumer, playerName, origin);
			}
			smartLogFallables(consumer, playerName, origin);
			
			// Debug
			//System.out.println("Material type:" + type.toString() + " | " + "TypeId:" + typeId);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (isLogging(event.getBlockClicked().getWorld(), Logging.BLOCKBREAK)) {
			consumer.queueBlockBreak(event.getPlayer().getName(), event.getBlockClicked().getState());
		}
	}
}

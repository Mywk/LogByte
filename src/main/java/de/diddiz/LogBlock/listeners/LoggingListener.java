package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Consumer;

import org.bukkit.event.Listener;

import de.diddiz.LogBlock.LogBlock;

public class LoggingListener implements Listener
{
	protected final Consumer consumer;

	public LoggingListener(LogBlock lb) {
		consumer = lb.getConsumer();
	}
}

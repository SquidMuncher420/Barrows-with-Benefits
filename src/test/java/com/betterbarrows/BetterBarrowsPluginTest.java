package com.betterbarrows;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterBarrowsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BetterBarrowsPlugin.class);
		RuneLite.main(args);
	}
}
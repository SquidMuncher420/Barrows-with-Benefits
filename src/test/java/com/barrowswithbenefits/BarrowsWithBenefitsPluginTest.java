package com.barrowswithbenefits;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BarrowsWithBenefitsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BarrowsWithBenefitsPlugin.class);
		RuneLite.main(args);
	}
}
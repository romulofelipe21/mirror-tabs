package com.mirrortabs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MirrorTabsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MirrorTabsPlugin.class);
		RuneLite.main(args);
	}
}

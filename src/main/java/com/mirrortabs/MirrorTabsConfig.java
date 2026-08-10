package com.mirrortabs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(MirrorTabsConfig.GROUP)
public interface MirrorTabsConfig extends Config
{
	String GROUP = "mirror-tabs";

	@ConfigItem(
		keyName = "enableMirrorTabs",
		name = "Enable Mirror Tabs",
		description = "Shows or hides the Mirror Tabs overlay",
		position = 0
	)
	default boolean enableMirrorTabs()
	{
		return true;
	}

	@Range(
		min = 0,
		max = 100
	)
	@ConfigItem(
		keyName = "overlayOpacity",
		name = "Overlay opacity",
		description = "Controls the overlay opacity as a percentage",
		position = 1
	)
	default int overlayOpacity()
	{
		return 85;
	}

	@Range(
		min = 50,
		max = 200
	)
	@ConfigItem(
		keyName = "overlayScale",
		name = "Overlay scale",
		description = "Controls the initial overlay size before manual resizing",
		position = 2
	)
	default int overlayScale()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "showLabels",
		name = "Show labels",
		description = "Shows text labels inside the overlay",
		position = 3
	)
	default boolean showLabels()
	{
		return true;
	}
}

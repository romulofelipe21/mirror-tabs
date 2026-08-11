package com.mirrortabs;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

@Singleton
class MirrorItemChargeResolver
{
	static final int NO_CHARGES = Integer.MIN_VALUE;
	static final int UNKNOWN_CHARGES = -1;

	private static final String ITEM_CHARGE_GROUP = "itemCharge";
	private static final String KEY_AMULET_OF_BOUNTY = "amuletOfBounty";
	private static final String KEY_AMULET_OF_CHEMISTRY = "amuletOfChemistry";
	private static final String KEY_BINDING_NECKLACE = "bindingNecklace";
	private static final String KEY_BLOOD_ESSENCE = "bloodEssence";
	private static final String KEY_BRACELET_OF_CLAY = "braceletOfClay";
	private static final String KEY_BRACELET_OF_SLAUGHTER = "braceletOfSlaughter";
	private static final String KEY_CHRONICLE = "chronicle";
	private static final String KEY_DODGY_NECKLACE = "dodgyNecklace";
	private static final String KEY_EXPEDITIOUS_BRACELET = "expeditiousBracelet";
	private static final String KEY_EXPLORERS_RING = "explorerRing";
	private static final String KEY_RING_OF_FORGING = "ringOfForging";

	private static final Pattern TRAILING_CHARGES = Pattern.compile("\\((\\d+)\\)$");

	private final ItemManager itemManager;
	private final ConfigManager configManager;

	@Inject
	MirrorItemChargeResolver(ItemManager itemManager, ConfigManager configManager)
	{
		this.itemManager = itemManager;
		this.configManager = configManager;
	}

	int[] resolve(Item[] items)
	{
		int[] charges = new int[items.length];
		Arrays.fill(charges, NO_CHARGES);

		for (int slot = 0; slot < items.length; slot++)
		{
			Item item = items[slot];
			if (item != null && item.getId() >= 0)
			{
				charges[slot] = resolve(item.getId());
			}
		}

		return charges;
	}

	private int resolve(int itemId)
	{
		String configKey = findConfigKey(itemId);
		if (configKey != null)
		{
			Integer charges = configManager.getRSProfileConfiguration(
				ITEM_CHARGE_GROUP,
				configKey,
				Integer.class
			);
			if (charges == null)
			{
				charges = configManager.getConfiguration(ITEM_CHARGE_GROUP, configKey, Integer.class);
			}

			return charges == null ? UNKNOWN_CHARGES : Math.max(0, charges);
		}

		ItemComposition composition = itemManager.getItemComposition(itemId);
		return composition == null ? NO_CHARGES : parseTrailingCharges(composition.getName());
	}

	static int parseTrailingCharges(String itemName)
	{
		if (itemName == null)
		{
			return NO_CHARGES;
		}

		Matcher matcher = TRAILING_CHARGES.matcher(itemName);
		if (!matcher.find())
		{
			return NO_CHARGES;
		}

		try
		{
			return Integer.parseInt(matcher.group(1));
		}
		catch (NumberFormatException ignored)
		{
			return NO_CHARGES;
		}
	}

	private static String findConfigKey(int itemId)
	{
		switch (itemId)
		{
			case ItemID.DODGY_NECKLACE:
				return KEY_DODGY_NECKLACE;
			case ItemID.MAGIC_EMERALD_NECKLACE:
				return KEY_BINDING_NECKLACE;
			case ItemID.LUMBRIDGE_RING_EASY:
			case ItemID.LUMBRIDGE_RING_MEDIUM:
			case ItemID.LUMBRIDGE_RING_HARD:
			case ItemID.LUMBRIDGE_RING_ELITE:
				return KEY_EXPLORERS_RING;
			case ItemID.RING_OF_FORGING:
				return KEY_RING_OF_FORGING;
			case ItemID.AMULET_OF_CHEMISTRY:
				return KEY_AMULET_OF_CHEMISTRY;
			case ItemID.AMULET_OF_BOUNTY:
				return KEY_AMULET_OF_BOUNTY;
			case ItemID.BRACELET_OF_SLAUGHTER:
				return KEY_BRACELET_OF_SLAUGHTER;
			case ItemID.EXPEDITIOUS_BRACELET:
				return KEY_EXPEDITIOUS_BRACELET;
			case ItemID.JEWL_BRACELET_OF_CLAY:
				return KEY_BRACELET_OF_CLAY;
			case ItemID.CHRONICLE:
				return KEY_CHRONICLE;
			case ItemID.BLOOD_ESSENCE_ACTIVE:
				return KEY_BLOOD_ESSENCE;
			default:
				return null;
		}
	}
}

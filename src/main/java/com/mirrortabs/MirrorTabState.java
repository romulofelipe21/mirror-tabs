package com.mirrortabs;

import net.runelite.api.gameval.InventoryID;

public enum MirrorTabState
{
	INVENTORY(InventoryID.INV, "Inventory"),
	EQUIPMENT(InventoryID.WORN, "Equipment");

	private final int containerId;
	private final String label;

	MirrorTabState(int containerId, String label)
	{
		this.containerId = containerId;
		this.label = label;
	}

	int getContainerId()
	{
		return containerId;
	}

	String getLabel()
	{
		return label;
	}
}

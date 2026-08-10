package com.mirrortabs;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Mirror Tabs",
	description = "Displays inventory and equipment together",
	tags = {"inventory", "equipment", "interface", "tabs"}
)
public class MirrorTabsPlugin extends Plugin
{
	private static final EquipmentInventorySlot[] DISPLAYED_EQUIPMENT_SLOTS =
	{
		EquipmentInventorySlot.HEAD,
		EquipmentInventorySlot.CAPE,
		EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.AMMO,
		EquipmentInventorySlot.WEAPON,
		EquipmentInventorySlot.BODY,
		EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS,
		EquipmentInventorySlot.GLOVES,
		EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING
	};

	private boolean inventoryInitialized;
	private boolean equipmentInitialized;
	private boolean spritesInitialized;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MirrorTabsOverlay overlay;

	@Inject
	private SpriteManager spriteManager;

	@Override
	protected void startUp()
	{
		inventoryInitialized = false;
		equipmentInitialized = false;
		spritesInitialized = false;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlay.reset();
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		boolean loggedIn = client.getGameState() == GameState.LOGGED_IN;
		overlay.setLoggedIn(loggedIn);
		if (!loggedIn)
		{
			inventoryInitialized = false;
			equipmentInitialized = false;
			spritesInitialized = false;
			return;
		}

		overlay.ensureDefaultLocation(client.getCanvasWidth(), client.getCanvasHeight());

		if (!spritesInitialized)
		{
			spritesInitialized = cacheSprites();
		}

		if (!inventoryInitialized)
		{
			inventoryInitialized = cacheContainer(MirrorTabState.INVENTORY, client.getItemContainer(InventoryID.INV));
		}

		if (!equipmentInitialized)
		{
			equipmentInitialized = cacheContainer(MirrorTabState.EQUIPMENT, client.getItemContainer(InventoryID.WORN));
		}

		int selectedPanel = client.getVarcIntValue(VarClientID.TOPLEVEL_PANEL);
		int inventoryPanel = WidgetUtil.componentToInterface(InterfaceID.Inventory.ITEMS);
		int equipmentPanel = WidgetUtil.componentToInterface(InterfaceID.Wornitems.UNIVERSE);

		if (selectedPanel == inventoryPanel)
		{
			overlay.setAutomaticMirroredState(MirrorTabState.EQUIPMENT);
		}
		else if (selectedPanel == equipmentPanel)
		{
			overlay.setAutomaticMirroredState(MirrorTabState.INVENTORY);
		}
		else if (isVisible(client.getWidget(InterfaceID.Inventory.ITEMS)))
		{
			overlay.setAutomaticMirroredState(MirrorTabState.EQUIPMENT);
		}
		else if (isVisible(client.getWidget(InterfaceID.Wornitems.UNIVERSE)))
		{
			overlay.setAutomaticMirroredState(MirrorTabState.INVENTORY);
		}
	}

	private boolean cacheSprites()
	{
		BufferedImage inventoryIcon = spriteManager.getSprite(SpriteID.SideIcons.INVENTORY, 0);
		BufferedImage equipmentIcon = spriteManager.getSprite(SpriteID.SideIcons.EQUIPMENT, 0);
		BufferedImage[] equipmentPlaceholders = new BufferedImage[14];

		equipmentPlaceholders[EquipmentInventorySlot.HEAD.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.HEAD, 0);
		equipmentPlaceholders[EquipmentInventorySlot.CAPE.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.CAPE, 0);
		equipmentPlaceholders[EquipmentInventorySlot.AMULET.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.NECK, 0);
		equipmentPlaceholders[EquipmentInventorySlot.WEAPON.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.WEAPON, 0);
		equipmentPlaceholders[EquipmentInventorySlot.BODY.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.TORSO, 0);
		equipmentPlaceholders[EquipmentInventorySlot.SHIELD.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.SHIELD, 0);
		equipmentPlaceholders[EquipmentInventorySlot.LEGS.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.LEGS, 0);
		equipmentPlaceholders[EquipmentInventorySlot.GLOVES.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.HANDS, 0);
		equipmentPlaceholders[EquipmentInventorySlot.BOOTS.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.FEET, 0);
		equipmentPlaceholders[EquipmentInventorySlot.RING.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.RING, 0);
		equipmentPlaceholders[EquipmentInventorySlot.AMMO.getSlotIdx()] = spriteManager.getSprite(SpriteID.Wornicons.AMMUNITION, 0);

		overlay.setInterfaceSprites(inventoryIcon, equipmentIcon, equipmentPlaceholders);

		if (inventoryIcon == null || equipmentIcon == null)
		{
			return false;
		}

		for (EquipmentInventorySlot slot : DISPLAYED_EQUIPMENT_SLOTS)
		{
			if (equipmentPlaceholders[slot.getSlotIdx()] == null)
			{
				return false;
			}
		}

		return true;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INV)
		{
			inventoryInitialized = cacheContainer(MirrorTabState.INVENTORY, event.getItemContainer());
		}
		else if (event.getContainerId() == InventoryID.WORN)
		{
			equipmentInitialized = cacheContainer(MirrorTabState.EQUIPMENT, event.getItemContainer());
		}
	}

	private boolean cacheContainer(MirrorTabState state, ItemContainer itemContainer)
	{
		if (itemContainer == null)
		{
			return false;
		}

		Item[] items = itemContainer.getItems();
		overlay.setItems(state, Arrays.copyOf(items, items.length));
		return true;
	}

	private static boolean isVisible(Widget widget)
	{
		return widget != null && !widget.isHidden();
	}

	@Provides
	MirrorTabsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MirrorTabsConfig.class);
	}
}

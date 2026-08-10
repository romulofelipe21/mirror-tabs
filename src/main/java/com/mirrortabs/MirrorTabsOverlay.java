package com.mirrortabs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MirrorTabsOverlay extends Overlay
{
	private static final int BASE_WIDTH = 180;
	private static final int PANEL_HEIGHT = 254;
	private static final int HEADER_HEIGHT = 32;
	private static final int EDGE_MARGIN = 10;
	private static final int BOTTOM_INTERFACE_RESERVE = 220;
	private static final int MINIMUM_SIZE = 100;

	private static final int INVENTORY_COLUMNS = 4;
	private static final int INVENTORY_ROWS = 7;
	private static final int INVENTORY_HORIZONTAL_GAP = 6;
	private static final int INVENTORY_VERTICAL_GAP = 3;

	private static final int EQUIPMENT_HORIZONTAL_GAP = 6;
	private static final int EQUIPMENT_VERTICAL_GAP = 5;

	private static final Color BACKGROUND_COLOR = new Color(35, 43, 47, 225);
	private static final Color HEADER_COLOR = new Color(28, 32, 34, 238);
	private static final Color SLOT_COLOR = new Color(55, 55, 50, 205);
	private static final Color SLOT_BORDER_DARK = new Color(35, 30, 24, 230);
	private static final Color SLOT_BORDER_LIGHT = new Color(110, 96, 72, 220);
	private static final Color OUTER_BORDER = new Color(39, 31, 22);
	private static final Color INNER_BORDER = new Color(145, 122, 78);
	private static final Color LABEL_COLOR = new Color(238, 224, 184);
	private static final Item[] EMPTY_ITEMS = new Item[0];

	private static final EquipmentSlotLayout[] EQUIPMENT_LAYOUT =
	{
		new EquipmentSlotLayout(EquipmentInventorySlot.HEAD, 1, 0),
		new EquipmentSlotLayout(EquipmentInventorySlot.CAPE, 0, 1),
		new EquipmentSlotLayout(EquipmentInventorySlot.AMULET, 1, 1),
		new EquipmentSlotLayout(EquipmentInventorySlot.AMMO, 2, 1),
		new EquipmentSlotLayout(EquipmentInventorySlot.WEAPON, 0, 2),
		new EquipmentSlotLayout(EquipmentInventorySlot.BODY, 1, 2),
		new EquipmentSlotLayout(EquipmentInventorySlot.SHIELD, 2, 2),
		new EquipmentSlotLayout(EquipmentInventorySlot.LEGS, 1, 3),
		new EquipmentSlotLayout(EquipmentInventorySlot.GLOVES, 0, 4),
		new EquipmentSlotLayout(EquipmentInventorySlot.BOOTS, 1, 4),
		new EquipmentSlotLayout(EquipmentInventorySlot.RING, 2, 4)
	};

	private final ItemManager itemManager;
	private final MirrorTabsConfig config;
	private volatile boolean loggedIn;
	private volatile MirrorTabState mirroredState = MirrorTabState.EQUIPMENT;
	private volatile Item[] inventoryItems = EMPTY_ITEMS;
	private volatile Item[] equipmentItems = EMPTY_ITEMS;
	private volatile BufferedImage inventoryIcon;
	private volatile BufferedImage equipmentIcon;
	private volatile BufferedImage[] equipmentPlaceholders = new BufferedImage[14];

	@Inject
	MirrorTabsOverlay(ItemManager itemManager, MirrorTabsConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(true);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setResizable(true);
		setMinimumSize(MINIMUM_SIZE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableMirrorTabs() || !loggedIn)
		{
			return null;
		}

		Item[] items = mirroredState == MirrorTabState.INVENTORY ? inventoryItems : equipmentItems;
		Dimension size = getRenderDimension();
		int headerHeight = getHeaderHeight(size);
		float opacity = Math.max(0, Math.min(100, config.overlayOpacity())) / 100.0f;

		Composite originalComposite = graphics.getComposite();
		Color originalColor = graphics.getColor();
		Font originalFont = graphics.getFont();
		Stroke originalStroke = graphics.getStroke();

		try
		{
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			drawPanel(graphics, size.width, size.height, headerHeight);

			if (mirroredState == MirrorTabState.INVENTORY)
			{
				drawInventory(graphics, items, size.width, size.height, headerHeight);
			}
			else
			{
				drawEquipment(graphics, items, size.width, size.height, headerHeight);
			}
		}
		finally
		{
			graphics.setComposite(originalComposite);
			graphics.setColor(originalColor);
			graphics.setFont(originalFont);
			graphics.setStroke(originalStroke);
		}

		return size;
	}

	void ensureDefaultLocation(int canvasWidth, int canvasHeight)
	{
		if (getPreferredLocation() != null)
		{
			return;
		}

		Dimension size = getRenderDimension();
		int x = Math.max(EDGE_MARGIN, canvasWidth - size.width - EDGE_MARGIN);
		int y = Math.max(EDGE_MARGIN, canvasHeight - size.height - BOTTOM_INTERFACE_RESERVE);
		setPreferredLocation(new Point(x, y));
	}

	void setAutomaticMirroredState(MirrorTabState mirroredState)
	{
		this.mirroredState = mirroredState;
	}

	void setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}

	void setItems(MirrorTabState state, Item[] items)
	{
		if (state == MirrorTabState.INVENTORY)
		{
			inventoryItems = items;
		}
		else
		{
			equipmentItems = items;
		}
	}

	void setInterfaceSprites(BufferedImage inventoryIcon, BufferedImage equipmentIcon, BufferedImage[] placeholders)
	{
		this.inventoryIcon = inventoryIcon;
		this.equipmentIcon = equipmentIcon;
		this.equipmentPlaceholders = Arrays.copyOf(placeholders, placeholders.length);
	}

	void reset()
	{
		loggedIn = false;
		mirroredState = MirrorTabState.EQUIPMENT;
		inventoryItems = EMPTY_ITEMS;
		equipmentItems = EMPTY_ITEMS;
		inventoryIcon = null;
		equipmentIcon = null;
		equipmentPlaceholders = new BufferedImage[14];
	}

	private Dimension getRenderDimension()
	{
		Dimension preferredSize = getPreferredSize();
		if (preferredSize != null)
		{
			return new Dimension(
				Math.max(MINIMUM_SIZE, preferredSize.width),
				Math.max(MINIMUM_SIZE, preferredSize.height)
			);
		}

		double scale = getScale();
		return new Dimension((int) Math.round(BASE_WIDTH * scale), (int) Math.round(PANEL_HEIGHT * scale));
	}

	private double getScale()
	{
		return Math.max(50, Math.min(200, config.overlayScale())) / 100.0;
	}

	private static int getHeaderHeight(Dimension size)
	{
		double scale = Math.min((double) size.width / BASE_WIDTH, (double) size.height / PANEL_HEIGHT);
		return Math.max(22, Math.min(size.height / 3, (int) Math.round(HEADER_HEIGHT * scale)));
	}

	private void drawPanel(Graphics2D graphics, int width, int height, int headerHeight)
	{
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(HEADER_COLOR);
		graphics.fillRect(3, 3, width - 6, headerHeight - 3);

		graphics.setColor(OUTER_BORDER);
		graphics.drawRect(0, 0, width - 1, height - 1);
		graphics.setColor(INNER_BORDER);
		graphics.drawRect(1, 1, width - 3, height - 3);
		graphics.setColor(OUTER_BORDER);
		graphics.drawRect(2, 2, width - 5, height - 5);

		double scale = Math.min((double) width / BASE_WIDTH, (double) height / PANEL_HEIGHT);
		float fontSize = (float) Math.max(9.0, Math.min(20.0, 12.0 * scale));
		int contentPadding = Math.max(5, (int) Math.round(8 * scale));
		int iconSize = Math.max(16, headerHeight - 6);
		BufferedImage icon = mirroredState == MirrorTabState.INVENTORY ? inventoryIcon : equipmentIcon;

		if (config.showLabels())
		{
			graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, fontSize));
			graphics.setColor(LABEL_COLOR);
			FontMetrics metrics = graphics.getFontMetrics();
			String label = mirroredState.getLabel();
			int labelY = (headerHeight - metrics.getHeight()) / 2 + metrics.getAscent();
			graphics.drawString(label, contentPadding, labelY);
		}

		if (icon != null)
		{
			drawContainedImage(graphics, icon, width - iconSize - 4, 3, iconSize, iconSize);
		}
	}

	private void drawInventory(Graphics2D graphics, Item[] items, int width, int height, int headerHeight)
	{
		int horizontalPadding = Math.max(4, width / 30);
		int verticalPadding = Math.max(4, height / 50);
		int horizontalGap = Math.max(1, (int) Math.round(INVENTORY_HORIZONTAL_GAP * (double) width / BASE_WIDTH));
		int verticalGap = Math.max(1, (int) Math.round(INVENTORY_VERTICAL_GAP * (double) height / PANEL_HEIGHT));
		int slotWidth = Math.max(1, (width - 2 * horizontalPadding - (INVENTORY_COLUMNS - 1) * horizontalGap) / INVENTORY_COLUMNS);
		int slotHeight = Math.max(1, (height - headerHeight - 2 * verticalPadding - (INVENTORY_ROWS - 1) * verticalGap) / INVENTORY_ROWS);
		int gridWidth = INVENTORY_COLUMNS * slotWidth + (INVENTORY_COLUMNS - 1) * horizontalGap;
		int gridHeight = INVENTORY_ROWS * slotHeight + (INVENTORY_ROWS - 1) * verticalGap;
		int startX = (width - gridWidth) / 2;
		int startY = headerHeight + Math.max(1, (height - headerHeight - gridHeight) / 2);

		for (int slot = 0; slot < INVENTORY_COLUMNS * INVENTORY_ROWS; slot++)
		{
			int column = slot % INVENTORY_COLUMNS;
			int row = slot / INVENTORY_COLUMNS;
			int x = startX + column * (slotWidth + horizontalGap);
			int y = startY + row * (slotHeight + verticalGap);
			drawItem(graphics, getItem(items, slot), x, y, slotWidth, slotHeight);
		}
	}

	private void drawEquipment(Graphics2D graphics, Item[] items, int width, int height, int headerHeight)
	{
		int horizontalPadding = Math.max(8, width / 10);
		int verticalPadding = Math.max(5, height / 30);
		int horizontalGap = Math.max(2, (int) Math.round(EQUIPMENT_HORIZONTAL_GAP * (double) width / BASE_WIDTH));
		int verticalGap = Math.max(2, (int) Math.round(EQUIPMENT_VERTICAL_GAP * (double) height / PANEL_HEIGHT));
		int slotWidth = Math.max(1, (width - 2 * horizontalPadding - 2 * horizontalGap) / 3);
		int slotHeight = Math.max(1, (height - headerHeight - 2 * verticalPadding - 4 * verticalGap) / 5);
		int gridWidth = 3 * slotWidth + 2 * horizontalGap;
		int gridHeight = 5 * slotHeight + 4 * verticalGap;
		int startX = (width - gridWidth) / 2;
		int startY = headerHeight + Math.max(1, (height - headerHeight - gridHeight) / 2);

		for (EquipmentSlotLayout layout : EQUIPMENT_LAYOUT)
		{
			int slotIndex = layout.slot.getSlotIdx();
			int x = startX + layout.column * (slotWidth + horizontalGap);
			int y = startY + layout.row * (slotHeight + verticalGap);
			Item item = getItem(items, slotIndex);
			drawEquipmentSlot(graphics, x, y, slotWidth, slotHeight);

			if (item == null || item.getId() < 0)
			{
				BufferedImage placeholder = slotIndex < equipmentPlaceholders.length
					? equipmentPlaceholders[slotIndex]
					: null;
				if (placeholder != null)
				{
					drawContainedImage(graphics, placeholder, x + 2, y + 2, Math.max(1, slotWidth - 4), Math.max(1, slotHeight - 4));
				}
			}
			else
			{
				drawItem(graphics, item, x, y, slotWidth, slotHeight);
			}
		}
	}

	private static void drawEquipmentSlot(Graphics2D graphics, int x, int y, int width, int height)
	{
		graphics.setColor(SLOT_COLOR);
		graphics.fillRect(x, y, width, height);
		graphics.setColor(SLOT_BORDER_DARK);
		graphics.drawRect(x, y, width - 1, height - 1);
		graphics.setColor(SLOT_BORDER_LIGHT);
		graphics.drawLine(x + 1, y + 1, x + width - 3, y + 1);
		graphics.drawLine(x + 1, y + 1, x + 1, y + height - 3);
	}

	private void drawItem(Graphics2D graphics, Item item, int x, int y, int slotWidth, int slotHeight)
	{
		if (item == null || item.getId() < 0)
		{
			return;
		}

		int quantity = item.getQuantity();
		BufferedImage image = itemManager.getImage(item.getId(), quantity, quantity > 1);
		if (image != null)
		{
			drawContainedImage(graphics, image, x, y, slotWidth, slotHeight);
		}
	}

	private static void drawContainedImage(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height)
	{
		double imageScale = Math.min(
			(double) width / image.getWidth(),
			(double) height / image.getHeight()
		);
		int drawWidth = Math.max(1, (int) Math.round(image.getWidth() * imageScale));
		int drawHeight = Math.max(1, (int) Math.round(image.getHeight() * imageScale));
		int imageX = x + (width - drawWidth) / 2;
		int imageY = y + (height - drawHeight) / 2;
		graphics.drawImage(image, imageX, imageY, drawWidth, drawHeight, null);
	}

	private static Item getItem(Item[] items, int slot)
	{
		return slot >= 0 && slot < items.length ? items[slot] : null;
	}

	private static final class EquipmentSlotLayout
	{
		private final EquipmentInventorySlot slot;
		private final int column;
		private final int row;

		private EquipmentSlotLayout(EquipmentInventorySlot slot, int column, int row)
		{
			this.slot = slot;
			this.column = column;
			this.row = row;
		}
	}
}

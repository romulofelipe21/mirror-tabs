package com.mirrortabs;

import java.awt.Point;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MirrorTabsOverlayTest
{
	private final MirrorTabsConfig config = new MirrorTabsConfig()
	{
	};

	@Test
	public void usesRuneLiteOverlayMovementAndResizing()
	{
		MirrorTabsOverlay overlay = new MirrorTabsOverlay(null, config);

		assertEquals(OverlayPosition.DYNAMIC, overlay.getPosition());
		assertTrue(overlay.isMovable());
		assertTrue(overlay.isResizable());
		assertEquals(100, overlay.getMinimumSize());
	}

	@Test
	public void defaultLocationDoesNotOverwriteUserLocation()
	{
		MirrorTabsOverlay overlay = new MirrorTabsOverlay(null, config);
		overlay.ensureDefaultLocation(1000, 800);
		assertEquals(new Point(810, 326), overlay.getPreferredLocation());

		Point userLocation = new Point(125, 75);
		overlay.setPreferredLocation(userLocation);
		overlay.ensureDefaultLocation(1000, 800);
		assertEquals(userLocation, overlay.getPreferredLocation());
	}
}

package com.mirrortabs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MirrorItemChargeResolverTest
{
	@Test
	public void parsesChargesEncodedInItemNames()
	{
		assertEquals(6, MirrorItemChargeResolver.parseTrailingCharges("Amulet of glory(6)"));
		assertEquals(4, MirrorItemChargeResolver.parseTrailingCharges("Prayer potion(4)"));
		assertEquals(0, MirrorItemChargeResolver.parseTrailingCharges("Waterskin(0)"));
	}

	@Test
	public void ignoresParenthesesThatAreNotCharges()
	{
		assertEquals(
			MirrorItemChargeResolver.NO_CHARGES,
			MirrorItemChargeResolver.parseTrailingCharges("Berserker ring (i)")
		);
		assertEquals(
			MirrorItemChargeResolver.NO_CHARGES,
			MirrorItemChargeResolver.parseTrailingCharges("Dragon platebody (g)")
		);
		assertEquals(
			MirrorItemChargeResolver.NO_CHARGES,
			MirrorItemChargeResolver.parseTrailingCharges(null)
		);
	}
}

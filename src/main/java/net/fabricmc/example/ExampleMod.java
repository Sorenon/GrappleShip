package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.items.GrappleHookItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {

	public static final Identifier START_GRAPPLE = new Identifier("modid", "grapple_start");
	public static final Identifier END_GRAPPLE = new Identifier("modid", "grapple_end");

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("modid", "grapple"), new GrappleHookItem(new FabricItemSettings().maxCount(1)));
	}
}

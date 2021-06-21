package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.items.GrappleHookItem;
import net.fabricmc.example.items.MarketGardenerItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {

	public static final Identifier START_GRAPPLE = new Identifier("modid", "grapple_start");
	public static final Identifier END_GRAPPLE = new Identifier("modid", "grapple_end");

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("modid", "grapple"), new GrappleHookItem(new FabricItemSettings().maxCount(1)));
		Registry.register(Registry.ITEM, new Identifier("modid", "market_gardener"), new MarketGardenerItem(ToolMaterials.IRON, 1.5F, -3.0F, (new Item.Settings()).group(ItemGroup.TOOLS)));
	}
}

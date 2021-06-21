package net.fabricmc.example.items;

import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.movement.AirStrafeMovement;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;

public class MarketGardenerItem extends ShovelItem {

    public MarketGardenerItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof LivingEntityExt ext && ext.getMovement() instanceof AirStrafeMovement) {
            target.damage(new EntityDamageSource("modid.marketgarden", attacker), 99);
        }

        stack.damage(1, attacker, entity -> entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }
}

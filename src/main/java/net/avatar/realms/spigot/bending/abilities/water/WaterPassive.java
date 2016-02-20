package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = "WaterPassive", element = BendingElement.WATER)
public class WaterPassive extends BendingPassiveAbility {
	public final static String NAME = "WaterPassive";

	public WaterPassive(RegisteredAbility register, Player player) {
		super(register, player);
	}

	public static Vector handle(Player player, Vector velocity) {
		Vector vec = velocity.clone();
		return vec;
	}

	@Override
	public boolean start() {
		Block block = this.player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);

		if (fallblock.getType() == Material.AIR) {
			return true;
		}

		if (BlockTools.isWaterbendable(block, this.player) && !BlockTools.isPlant(block)) {
			return true;
		}

		if ((BlockTools.isWaterbendable(fallblock, this.player) && !BlockTools.isPlant(fallblock)) || (fallblock.getType() == Material.SNOW_BLOCK)) {
			return true;
		}

		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!this.bender.isBender(BendingElement.WATER)) {
			return false;
		}

		if (!EntityTools.canBendPassive(this.player, BendingElement.WATER)) {
			return false;
		}

		return true;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public void stop() {
		
	}

}

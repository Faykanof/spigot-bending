package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@BendingAbility(name="Dash", element=BendingType.ChiBlocker)
public class Dash extends Ability {

	@ConfigurationParameter("Length")
	private static double LENGTH = 1.9;
	
	@ConfigurationParameter("Height")
	private static double HEIGHT = 0.7;
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 6000;
	
	private Vector direction;

	public Dash(Player player) {
		super(player, null);
	}
	
	@Override
	public boolean sneak() {
		if (state.isBefore(AbilityState.CanStart)) {
			return true;
		}
		
		if (state == AbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Started);
		}
		
		return false;
	}

	public static boolean isDashing(Player player) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.Dash);
		if (instances == null || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}

	public static Dash getDash(Player pl) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.Dash);
		return (Dash) instances.get(pl);
	}
	@Override
	public boolean progress() {
		System.out.println(player.getLocation().toString());
		if (!super.progress()) {
			return false;
		}
		
		if (state == AbilityState.Ended) {
			return false;
		}
		
		if (state != AbilityState.Progressing) {
			return true;
		}
		System.out.println(player.getLocation().toString());
		dash();
		return false;
	}

	public void dash() {
		Vector dir = new Vector(direction.getX() * LENGTH, HEIGHT, direction.getZ() * LENGTH);
		player.setVelocity(dir);
		setState(AbilityState.Ended);
	}

	// This should be called in OnMoveEvent to set the direction dash the same as the player
	public void setDirection(Vector d) {
		if (state != AbilityState.Started) {
			return;
		}
		if (Double.isNaN(d.getX()) 
				|| Double.isNaN(d.getY())
				|| Double.isNaN(d.getZ())
				|| ((d.getX() < 0.005 && d.getX() > -0.005)
				&& (d.getZ() < 0.005 && d.getZ() > -0.005))) {
			this.direction = player.getLocation().getDirection().clone().normalize();
		} else {
			this.direction = d.normalize();
		}
		System.out.println(player.getLocation().toString());
		setState(AbilityState.Progressing);
	}
	
	@Override
	public void remove() {
		bender.cooldown(Abilities.Dash, COOLDOWN);
		AbilityManager.getManager().getInstances(Abilities.Dash).remove(player);
		super.remove();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.Dash;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}
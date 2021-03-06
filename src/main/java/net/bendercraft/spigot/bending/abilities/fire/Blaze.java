package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

@ABendingAbility(name = Blaze.NAME, element = BendingElement.FIRE)
public class Blaze extends BendingActiveAbility {
	public final static String NAME = "Blaze";
	
	@ConfigurationParameter("Arc")
	private static int DEFAULT_ARC = 10;

	@ConfigurationParameter("Range-Arc")
	private static int RANGE_ARC = 25;

	@ConfigurationParameter("Range-Ring")
	private static int RANGE_RING = 13;
	
	@ConfigurationParameter("Power")
	public static int POWER = 4;
	
	@ConfigurationParameter("Dissipate")
	public static long DISSIPATE = 5000;

	private static int stepsize = 2;

	private List<FireStream> firestreams = new LinkedList<FireStream>();
	
	private int power;
	private long dissipate;

	public Blaze(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.power = POWER;
		if(bender.hasPerk(BendingPerk.FIRE_BLAZE_ENERGY)) {
			this.power -= 1;
		}
		
		this.dissipate = DISSIPATE;
		if(bender.hasPerk(BendingPerk.FIRE_BLAZE_PERMANENT)) {
			this.dissipate += 1000;
		}
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, power)) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean swing() {
		if(getState() != BendingAbilityState.START) {
			return false;
		}
		
		Location location = this.player.getLocation();

		int arc = DEFAULT_ARC;

		for (int i = -arc; i <= arc; i += stepsize) {
			double angle = Math.toRadians(i);
			Vector direction = this.player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
			vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

			direction.setX(vx);
			direction.setZ(vz);

			int range = RANGE_ARC;
			if (AvatarState.isAvatarState(this.player)) {
				range = AvatarState.getValue(range);
			}

			firestreams.add(new FireStream(location, direction, this.player, range, dissipate));
		}
		bender.fire.consume(NAME, power);
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() != BendingAbilityState.START) {
			return false;
		}
		Location location = this.player.getLocation();

		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
			Vector direction = this.player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
			vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

			direction.setX(vx);
			direction.setZ(vz);

			int range = RANGE_RING;
			if (AvatarState.isAvatarState(this.player)) {
				range = AvatarState.getValue(range);
			}

			firestreams.add(new FireStream(location, direction, this.player, range, dissipate));
		}

		bender.fire.consume(NAME, power);
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PROGRESSING) {
			List<FireStream> test = new LinkedList<FireStream>(firestreams);
			for(FireStream stream : test) {
				if(!stream.progress()) {
					firestreams.remove(stream);
				}
			}
			if(firestreams.isEmpty()) {
				remove();
			}
		}
	}
	
	@Override
	public void stop() {
		
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	public List<FireStream> getFirestreams() {
		return firestreams;
	}
}

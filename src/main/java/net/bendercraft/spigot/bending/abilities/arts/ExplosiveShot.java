package net.bendercraft.spigot.bending.abilities.arts;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.fire.FireStream;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = ExplosiveShot.NAME, affinity = BendingAffinity.BOW)
public class ExplosiveShot extends BendingActiveAbility {
	public final static String NAME = "ExplosiveShot";
	
	@ConfigurationParameter("Range-Damage")
	private static int RANGE_DAMAGE = 3;
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 7;
	@ConfigurationParameter("Sound-Radius")
	private static int SOUND_RADIUS = 35;
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;
	
	private static final Particle EXPLODE = Particle.SPELL;
	
	private Arrow arrow;
	private List<FireStream> firestreams = new LinkedList<FireStream>();

	private int rangeDamage;
	private int range;
	private long cooldown;

	private int ticks = 0; // For display only

	private Location location;

	public ExplosiveShot(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.rangeDamage = RANGE_DAMAGE;
		if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD)) {
			this.rangeDamage += 2;
		}
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTFIRE_POISONNEDDARTDAMAGE_CONCUSSIONDURATION)) {
			this.range += 1;
		}
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTCD_POISONNEDDARTCD_SLICEDURATION)) {
			this.cooldown -= 500;
		}
	}
	
	/**
	 * Entry point for this ability, gets constructed when a player shot an arrow
	 */
	public void shot(Arrow arrow) {
		this.arrow = arrow;
		setState(BendingAbilityState.PREPARED);
		bender.cooldown(NAME, cooldown);
	}
	
	/**
	 * When projectile hits, it EXPLODES §§§§
	 */
	public void explode() {
		if(arrow == null) {
			return;
		}
		location = arrow.getLocation();
		
		for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, rangeDamage)) {
			affect(entity);
		}

		Vector direction = arrow.getLocation().getDirection().clone();
		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
			vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

			direction.setX(vx);
			direction.setZ(vz);

			firestreams.add(new FireStream(location, direction, player, range, 1200));
			firestreams.add(new FireStream(location.add(0, 1, 0), direction, player, range, 1200));
			firestreams.add(new FireStream(location.add(0, -1, 0), direction, player, range, 1200));
			if(bender.hasPerk(BendingPerk.MASTER_SMOKE_HIDE_SHIELD)) {
				for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, range)) {
					entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*6, 1));
				}
			}
			setState(BendingAbilityState.PROGRESSING);
		}
		location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, SOUND_RADIUS / 16.0f, 1);
		location.getWorld().spawnParticle(EXPLODE, location, 1, 0, 0, 0);
	}

	@Override
	public boolean swing() {
		
		return false;
	}

	@Override
	public boolean sneak() {
		
		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PROGRESSING) {
			if(bender.hasPerk(BendingPerk.MASTER_SMOKE_HIDE_SHIELD)) {
				this.ticks++;
				if ((this.ticks  % 8) == 0 && location != null) {
					for (Block block : BlockTools.getBlocksAroundPoint(location, range)) {
						block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1, 15);
					}
				}
			}
			
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
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		DamageTools.damageEntity(bender, entity, this, DAMAGE);
	}
}

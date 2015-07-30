package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Lavatrain", element=BendingType.Earth, specialization=BendingSpecializationType.Lavabend)
public class LavaTrain implements IAbility {
	private static Map<Player, LavaTrain> instances = new HashMap<Player, LavaTrain>();
	
	//public static double speed = ConfigManager.lavaTrainSpeed;
	public static double speed = 5;
	private static long interval = (long) (1000. / speed);
	
	@ConfigurationParameter("Range")
	public static int RANGE = 7;
	
	@ConfigurationParameter("Train-Width")
	public static int TRAIN_WIDTH = 1;
	
	@ConfigurationParameter("Random-Width")
	public static int RANDOM_WIDTH = 2;
	
	@ConfigurationParameter("Random-Chance")
	public static double RANDOM_CHANCE = 0.25;
	
	@ConfigurationParameter("Reach-Width")
	public static int REACH_WIDTH = 3;
	
	@ConfigurationParameter("Max-Duration")
	public static long DURATION = 20000; //ms
	
	@ConfigurationParameter("Cooldown-Factor")
	public static int COOLDOWN_FACTOR = 2;
	
	
	private static final byte full = 0x0;
	
	private IAbility parent;
	private Location origin;
	private Block safePoint;
	private Location current;
	private Vector direction;
	private BendingPlayer bPlayer;
	private Player player;
	private boolean reached = false;
	
	private List<Block> affecteds = new LinkedList<Block>();

	private long time;
	
	public LavaTrain(Player player, IAbility parent) {
		if(instances.containsKey(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if(bPlayer == null) {
			return;
		}
		
		if (bPlayer.isOnCooldown(Abilities.LavaTrain))
			return;
		if(!EntityTools.canBend(player, Abilities.LavaTrain)) {
			return;
		}
		
		this.parent = parent;
		this.player = player;
		this.bPlayer = bPlayer;
		this.safePoint = this.bPlayer.getPlayer().getLocation().getBlock();
		
		this.direction = player.getEyeLocation().getDirection().clone();
		this.direction.setY(0);
		this.direction = this.direction.normalize();
		origin = player.getLocation().clone().add(direction.clone().multiply(TRAIN_WIDTH+1+RANDOM_WIDTH));
		origin.setY(origin.getY()-1);
		current = origin.clone();
		
		time = System.currentTimeMillis();
		bPlayer.cooldown(Abilities.LavaTrain, DURATION * COOLDOWN_FACTOR); //TODO : Real duration * COOLDOWN_FACTOR
		instances.put(player, this);
	}
	
	public boolean progress() {
		if(bPlayer == null || bPlayer.getPlayer() == null) {
			return false;
		}
		
		if (bPlayer.getPlayer().isDead() || !bPlayer.getPlayer().isOnline()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(bPlayer.getPlayer(), Abilities.LavaTrain, current)) {
			return false;
		}
		if(this.direction.getX() == 0 && this.direction.getZ() == 0) {
			if(!reached) {
				this.affectBlocks(current, REACH_WIDTH);
				reached = true;
			} else {
				if (System.currentTimeMillis() - time > DURATION) {
					return false;
				}
				return true;
			}
		}
		if (System.currentTimeMillis() - time >= interval) {
			if(origin.distance(current) >= RANGE) {
				if(!reached) {
					this.affectBlocks(current, REACH_WIDTH);
					reached = true;
				} else {
					if (System.currentTimeMillis() - time > DURATION) {
						return false;
					}
					return true;
				}
			} else {
				this.affectBlocks(current, TRAIN_WIDTH);
			}
			
			if(affecteds.isEmpty()) {
				return false;
			}
			
			time = System.currentTimeMillis();
			current = current.clone().add(direction);
		}
		
		return true;
	}
	
	private void affectBlocks(Location current, int width) {
		List<Block> safe = BlockTools.getBlocksOnPlane(this.safePoint.getLocation(), 1);
		safe.add(safePoint);
		
		for(int i=-1; i <= 2 ; i++) {
			Location tmp = current.clone();
			tmp.setY(current.getY()+i);
			List<Block> potentialsBlocks = BlockTools.getBlocksOnPlane(tmp, width);
			//Add small random in generation
			List<Block> potentialsAddsBlocks = BlockTools.getBlocksOnPlane(tmp, width+RANDOM_WIDTH);
			for(Block potentialsBlock : potentialsAddsBlocks) {
				if(Math.random() < RANDOM_CHANCE) {
					potentialsBlocks.add(potentialsBlock);
				}
			}
			
			for(Block potentialsBlock : potentialsBlocks) {
				if(BlockTools.isEarthbendable(bPlayer.getPlayer(),Abilities.LavaTrain, potentialsBlock) && !TempBlock.isTempBlock(potentialsBlock)) {
					//Do not let block behind bender to be bend, this whill be stupid
					if(!safe.contains(potentialsBlock)) {
						new TempBlock(potentialsBlock, Material.LAVA, full);
						affecteds.add(potentialsBlock);
					}
				}
			}
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void remove() {
		for(Block affected : affecteds) {
			TempBlock temp = TempBlock.get(affected);
			if(temp != null) {
				temp.revertBlock();
			}
		}
		affecteds.clear();
		instances.remove(this.bPlayer.getPlayer());
	}
	
	public static void progressAll() {
		List<LavaTrain> toRemove = new LinkedList<LavaTrain>();
		for(LavaTrain train : instances.values()) {
			if (!train.progress()) {
				toRemove.add(train);
			}
		}
		
		for(LavaTrain train : toRemove) {
			train.remove();
		}
	}
	
	public static void removeAll() {
		List<LavaTrain> toRemove = new LinkedList<LavaTrain>();
		toRemove.addAll(instances.values());
		
		for(LavaTrain train : toRemove) {
			train.remove();
		}
	}
	
	public static boolean isLavaPart(Block block) {
		for(LavaTrain train : instances.values()) {
			if(train.affecteds.contains(block)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}
	
	public static LavaTrain getLavaTrain(Block b) {
		for (LavaTrain train : instances.values()){
			if (train.affecteds.contains(b)) {
				return train;
			}
		}	
		return null;
	}

}
package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Raise Earth", element=BendingType.Earth)
public class EarthColumn implements IAbility {
	private static Map<Integer, EarthColumn> instances = new HashMap<Integer, EarthColumn>();
	
	@ConfigurationParameter("Height")
	public static int HEIGHT = 6;
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 1000;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 8;
	
	private static final Vector direction = new Vector(0, 1, 0);

	private static long interval = (long) (1000. / SPEED);
	
	private static int ID = Integer.MIN_VALUE;
	
	private static Map<Block, Block> alreadydoneblocks = new HashMap<Block, Block>();
	private static Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();

	private Location origin;
	private Location location;
	private Block block;
	private int distance;
	private Player player;
	private int id;
	private long time;
	private int height = HEIGHT;
	private List<Block> affectedBlocks = new ArrayList<Block>();
	private EarthGrab earthGrab = null;
	private IAbility parent;

	public EarthColumn(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.RaiseEarth)) {
			return;
		}
			
		try {
			block = BlockTools.getEarthSourceBlock(player, Abilities.RaiseEarth, RANGE);
			if (block == null)
				return;
			origin = block.getLocation();
			location = origin.clone();
			distance = BlockTools.getEarthbendableBlocksLength(player, block,
					direction.clone().multiply(-1), height);
		} catch (IllegalStateException e) {
			return;
		}

		this.player = player;

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				bPlayer.cooldown(Abilities.RaiseEarth, COOLDOWN);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	public EarthColumn(Player player, Location origin, IAbility parent) {
		this.parent = parent;
		this.origin = origin;
		location = origin.clone();
		block = location.getBlock();
		this.player = player;
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	public EarthColumn(Player player, Location origin, int height, IAbility parent) {
		this.parent = parent;
		this.height = height;
		this.origin = origin;
		location = origin.clone();
		block = location.getBlock();
		this.player = player;
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}
	
	public EarthColumn(Player player, Location origin,int height, EarthGrab grab, IAbility parent) {
		this(player,origin, height, parent);
		this.earthGrab = grab;
	}
	
	public EarthGrab getEarthGrab() {
		return earthGrab;
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisblock;
		for (int i = 0; i <= distance; i++) {
			thisblock = block.getWorld().getBlockAt(
					location.clone().add(direction.clone().multiply(-i)));
			if (thisblock.getType() != Material.ANVIL) {
				affectedBlocks.add(thisblock);
			}	
			if (CompactColumn.blockInAllAffectedBlocks(thisblock))
				CompactColumn.revertBlock(thisblock);
		}
	}

	public boolean blockInAffectedBlocks(Block block) {
		if (affectedBlocks.contains(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (EarthColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (EarthColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block)) {
				column.affectedBlocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		for (Block block : affectedBlocks) {
			if (blockInAllAffectedBlocks(block)
					|| alreadydoneblocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!moveEarth()) {
				for (Block block : affectedBlocks) {
					alreadydoneblocks.put(block, block);
				}
				baseblocks.put(
						location.clone()
								.add(direction.clone().multiply(
										-1 * (distance - 1))).getBlock(),
						(distance - 1));

				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		BlockTools.moveEarth(player, block, direction, distance);
		loadAffectedBlocks();

		if (location.distance(origin) >= distance) {
			return false;
		}

		return true;
	}

	public static void progressAll() {
		List<EarthColumn> toRemove = new LinkedList<EarthColumn>();
		for(EarthColumn column : instances.values()) {
			boolean keep = column.progress();
			if(!keep) {
				toRemove.add(column);
			}
		}
		for(EarthColumn column : toRemove) {
			column.remove();
		}
	}

	private void remove() {
		instances.remove(id);
	}

	public static boolean blockIsBase(Block block) {
		if (baseblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static void removeBlockBase(Block block) {
		if (baseblocks.containsKey(block)) {
			baseblocks.remove(block);
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	public static void resetBlock(Block block) {
		if (alreadydoneblocks.containsKey(block)) {
			alreadydoneblocks.remove(block);
		}
	}
	
	public  List<Block> getAffectedBlocks() {
		return affectedBlocks;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
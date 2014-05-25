package net.avatarrealms.minecraft.bending.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.business.Tools;
import net.avatarrealms.minecraft.bending.data.BendingPlayers;
import net.avatarrealms.minecraft.bending.data.ConfigManager;
import net.avatarrealms.minecraft.bending.data.CustomSerializable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("BendingPlayer")
public class BendingPlayer implements CustomSerializable {

	private static ConcurrentHashMap<String, BendingPlayer> players = new ConcurrentHashMap<String, BendingPlayer>();

	private static Map<Abilities, Long> abilityCooldowns = new HashMap<Abilities, Long>();
	private static long globalCooldown = 250;
	private static BendingPlayers config = Tools.config;

	private String playername;
	private String language;

	private Map<Integer, Abilities> slotAbilities = new HashMap<Integer, Abilities>();
	private Map<Material, Abilities> itemAbilities = new HashMap<Material, Abilities>();

	private Map<BendingType, BendingLevel> bendings = new HashMap<BendingType,BendingLevel>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private boolean bendToItem = ConfigManager.bendToItem;

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lasttime = 0;

	private boolean permaremoved = false;

	private boolean tremorsense = true;

	public BendingPlayer(String player) {
		if (players.containsKey(player)) {
			players.remove(player);
		}

		language = Tools.getDefaultLanguage();

		playername = player;

		lasttime = System.currentTimeMillis();

		players.put(player, this);

		// Tools.verbose(playername + " slot size: " + slotAbilities.size());
		// Tools.verbose(playername + " item size: " + itemAbilities.size());
	}

	public static List<BendingPlayer> getBendingPlayers() {
		List<BendingPlayer> bPlayers = new ArrayList<BendingPlayer>(
				players.values());
		return bPlayers;
	}

	public static BendingPlayer getBendingPlayer(OfflinePlayer player) {
		return getBendingPlayer(player.getName());
	}

	public static BendingPlayer getBendingPlayer(String playername) {
		if (players.containsKey(playername)) {
			return players.get(playername);
		}

		if (config == null) {
			config = Tools.config;
		}

		BendingPlayer player = config.getBendingPlayer(playername);
		if (player != null) {
			players.put(playername, player);
			return player;
		} else {
			return new BendingPlayer(playername);
		}
	}

	public static void initializeCooldowns() {
		if (abilityCooldowns.isEmpty()) {
			for (Abilities ability : Abilities.values()) {
				long cd = 0;
				switch (ability) {
					case WaterManipulation:
						cd = 1000;
						break;
					case EarthBlast:
						cd = 1000;
						break;
					case AirSwipe:
						cd = ConfigManager.airSwipeCooldown;
						break;
					case HighJump:
						cd = ConfigManager.highJumpCooldown;
						break;
					case RapidPunch:
						cd = ConfigManager.rapidPunchCooldown;
						break;
					case Tremorsense:
						cd = ConfigManager.tremorsenseCooldown;
						break;
					case FireBlast:
						cd = ConfigManager.fireBlastCooldown;
						break;
					case FireJet:
						cd = ConfigManager.fireJetCooldown;
						break;
					case IceSpike:
						cd = ConfigManager.icespikecooldown;
						break;
					default:
						//TODO Throw exception here
						cd = 0;
						break;
				}
				abilityCooldowns.put(ability, cd);
			}
		}
	}

	public boolean isOnGlobalCooldown() {
		return (System.currentTimeMillis() <= lasttime + globalCooldown);
	}

	public boolean isOnCooldown(Abilities ability) {
		if (ability == Abilities.AvatarState)
			return false;
		if (isOnGlobalCooldown()) {
			return true;
		}

		if (cooldowns.containsKey(ability)) {
			double time = System.currentTimeMillis() - cooldowns.get(ability);
			// Tools.verbose(time);
			// Tools.verbose(ability + ": " + abilityCooldowns.get(ability));
			return (time <= abilityCooldowns.get(ability));

		} else {
			return false;
		}
	}

	public void toggleTremorsense() {
		tremorsense = !tremorsense;
	}

	public boolean isTremorsensing() {
		return tremorsense;
	}

	public void cooldown() {
		cooldown(null);
	}

	public void cooldown(Abilities ability) {
		long time = System.currentTimeMillis();
		if (ability != null)
			cooldowns.put(ability, time);
		lasttime = time;
	}

	public String getName() {
		return playername;
	}

	public boolean isBender() {
		return !bendings.isEmpty();
	}

	public boolean isBender(BendingType type) {
		// lasttime = System.currentTimeMillis();
		return bendings.containsKey(type);
	}
	
	public boolean hasLevel(String ability) {
		
		if (bendings == null) {
			return false;
		}
		
		if (ability.equalsIgnoreCase("plantbending")) {
			if (!bendings.containsKey(BendingType.Water)) {
				return false;
			}
			if (bendings.get(BendingType.Water).getLevel() < ConfigManager.plantbendingLevelRequired){
				return false;
			}
			
			return true;
		}
		
		return true;
	}
	
	public boolean hasLevel(Abilities ability) {
		if (bendings == null) {
			return false;
		}
		
		if (Abilities.isAirbending(ability)) {
			if (!bendings.containsKey(BendingType.Air)) {
				return false;
			}
			
			if (bendings.get(BendingType.Air).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		if (Abilities.isEarthbending(ability)) {
			if (!bendings.containsKey(BendingType.Earth)) {
				return false;
			}
			if (bendings.get(BendingType.Earth).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		if (Abilities.isFirebending(ability)) {
			if (!bendings.containsKey(BendingType.Fire)) {
				return false;
			}
			if (bendings.get(BendingType.Fire).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			
			return true;
		}
		
		if (Abilities.isWaterbending(ability)){
			if (!bendings.containsKey(BendingType.Water)) {
				return false;
			}
			if (bendings.get(BendingType.Water).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			
			return true;
		}
		
		if (Abilities.isChiBlocking(ability)) {
			if (!bendings.containsKey(BendingType.ChiBlocker)) {
				return false;
			}
			if (bendings.get(BendingType.ChiBlocker).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		
		return true;
	}

	public void setBender(BendingType type) {
		removeBender();
		bendings.put(type, new BendingLevel (type, this));
	}

	public void addBender(BendingType type) {
		permaremoved = false;
		if (!bendings.containsKey(type))
			bendings.put(type, new BendingLevel(type,this));
	}

	public void clearAbilities() {
		slotAbilities = new HashMap<Integer, Abilities>();
		itemAbilities = new HashMap<Material, Abilities>();
	}

	public void removeBender() {
		bendings.clear();
		clearAbilities();
	}

	public void permaremoveBender() {
		permaremoved = true;
		removeBender();
	}

	public boolean isPermaRemoved() {
		return permaremoved;
	}

	public void setPermaRemoved(boolean value) {
		permaremoved = value;
	}

	public Abilities getAbility() {
		Player player = Bending.plugin.getServer().getPlayerExact(playername);
		if (player == null)
			return null;
		if (!player.isOnline() || player.isDead())
			return null;
		if (bendToItem) {
			Material item = player.getItemInHand().getType();
			return getAbility(item);
		} else {
			int slot = player.getInventory().getHeldItemSlot();
			return getAbility(slot);
		}
	}

	public Abilities getAbility(int slot) {
		return slotAbilities.get(slot);
	}

	public Abilities getAbility(Material item) {
		return itemAbilities.get(item);
	}

	public void setAbility(int slot, Abilities ability) {
		slotAbilities.put(slot, ability);
	}

	public void setAbility(Material item, Abilities ability) {
		itemAbilities.put(item, ability);
	}

	public void removeSelectedAbility() {
		Player player = Bending.plugin.getServer().getPlayerExact(playername);
		if (player == null)
			return;
		if (!player.isOnline() || player.isDead())
			return;
		if (bendToItem) {
			Material item = player.getItemInHand().getType();
			removeAbility(item);
		} else {
			int slot = player.getInventory().getHeldItemSlot();
			removeAbility(slot);
		}
	}

	public void removeAbility(int slot) {
		setAbility(slot, null);
	}

	public void removeAbility(Material item) {
		setAbility(item, null);
	}

	public Player getPlayer() {
		return Bending.plugin.getServer().getPlayerExact(playername);
	}

	// public static ArrayList<BendingPlayer> getBendingPlayers() {
	// ArrayList<BendingPlayer> list = new ArrayList<BendingPlayer>();
	// for (String player : players.keySet()) {
	// list.add(players.get(player));
	// }
	// return list;
	// }

	public List<BendingType> getBendingTypes() {
		List<BendingType> list = new ArrayList<BendingType>();
		for (BendingType index : bendings.keySet()) {
			list.add(index);
		}
		return list;
	}
	
	public String bendingsToString() {
		String str = "";
		for (BendingType type : bendings.keySet()) {
			str+=bendings.get(type).toString()+"\n";
		}
		return str;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public void setBendToItem(boolean value) {
		bendToItem = value;
	}

	public boolean getBendToItem() {
		return bendToItem;
	}

	public boolean canBeParalyzed() {
		return (System.currentTimeMillis() > paralyzeTime);
	}

	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	public void paralyze(long cooldown) {
		paralyzeTime = System.currentTimeMillis() + cooldown;
	}

	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	public long getLastTime() {
		return lasttime;
	}

	public void delete() {
		players.remove(playername);
	}

	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + playername;
		string += ", ";
		string += "Bendings=" + bendings;
		string += ", ";
		string += "Language=" + language;
		string += ", ";
		if (ConfigManager.bendToItem) {
			string += "Binds=" + itemAbilities;
		} else {
			string += "Binds=" + slotAbilities;
		}
		string += "}";
		return string;
	}

	@SuppressWarnings("unchecked")
	public BendingPlayer(Map<String, Object> map) {
		List<BendingLevel> bending;
		playername = (String) map.get("PlayerName");

		if (players.containsKey(playername)) {
			players.remove(playername);
		}
		
		bending = (List<BendingLevel>) map.get("Bendings");
		
		for (BendingLevel bend: bending) {
			bendings.put(bend.getBendingType(), bend);
			bend.setBendingPlayer(this);
		}
		language = (String) map.get("Language");
		bendToItem = (Boolean) map.get("BendToItem");
		itemAbilities = (Map<Material, Abilities>) map.get("ItemAbilities");
		slotAbilities = (Map<Integer, Abilities>) map.get("SlotAbilities");

		permaremoved = (Boolean) map.get("Permaremove");

		lasttime = (Long) map.get("LastTime");

		players.put(playername, this);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		List<BendingLevel> bending = new ArrayList<BendingLevel>();
		
		for (BendingType bLev : bendings.keySet()) {
			bending.add(bendings.get(bLev));
		}
		map.put("PlayerName", playername);
		map.put("Bendings", bending);
		map.put("Language", language);
		map.put("BendToItem", bendToItem);
		map.put("ItemAbilities", itemAbilities);
		map.put("SlotAbilities", slotAbilities);
		map.put("Permaremove", permaremoved);
		map.put("LastTime", lasttime);
		return map;
	}

	public static BendingPlayer deserialize(Map<String, Object> map) {
		return new BendingPlayer(map);
	}

	public static BendingPlayer valueOf(Map<String, Object> map) {
		return deserialize(map);
	}
	
	public void resetXP() {	
		for (BendingType type : bendings.keySet()) {
			bendings.get(type).setXP(bendings.get(type).getXP()*0.95);
		}
	}
	
	public double getCriticalHit(BendingType type, double damage){
		double newDamage = damage;
		int level = bendings.get(type).getLevel();
		double prc = ((level)/(level+2))*0.4;
		
		Random rand = new Random();
		
		if (rand.nextDouble() < prc) {
			newDamage += 1;
			
			if (level >= 25) {
				prc = ((level)/(level+2))*0.2;
				if (rand.nextDouble() < prc) {
					newDamage += 1;
				}
			}		
		}
		
		return newDamage;
	}
	
	public void increaseAllBendingCpt() {
		for (BendingType type : bendings.keySet()) {
			bendings.get(type).increaseCpt();
		}
	}
	
	public void resetBendingCpt(BendingType type) {
		if (bendings.get(type) != null)
			bendings.get(type).resetCpt();
	}
	
	public void earnXP(BendingType type) {
		bendings.get(type).earnXP();
	}
	
	public Integer getLevel (BendingType type) {
		BendingLevel bLvl = bendings.get(type);
		if (bLvl == null) {
			return 0;
		}
		else {
			return bLvl.getLevel();
		}
	}
	
	public void receiveXP(BendingType type, Integer amount) {
		bendings.get(type).giveXP(amount);
	}
	
	public void setBendingLevel(BendingType type, Integer level) {
		bendings.get(type).setLevel(level);
	}
	
	public int getMaxLevel() {
		int max = 0;
		for (BendingType type : bendings.keySet()) {
			if (bendings.get(type).getLevel() > max) {
				max = bendings.get(type).getLevel();
			}
		}	
		return max;
	}
}
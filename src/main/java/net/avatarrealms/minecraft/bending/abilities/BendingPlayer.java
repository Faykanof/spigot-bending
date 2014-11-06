package net.avatarrealms.minecraft.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.event.AbilityCooldownEvent;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BendingPlayer {
	private static Map<Abilities, Long> abilityCooldowns = new HashMap<Abilities, Long>();
	private static long globalCooldown = 250;

	private UUID player;
	private String language;

	private Map<Integer, Abilities> slotAbilities = new HashMap<Integer, Abilities>();
	private Map<Material, Abilities> itemAbilities = new HashMap<Material, Abilities>();

	private List<BendingType> bendings = new LinkedList<BendingType>();
	private List<BendingSpecializationType> specializations = new LinkedList<BendingSpecializationType>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private boolean bendToItem = ConfigManager.bendToItem;

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime = 0;

	private boolean tremorsense = true;

	public BendingPlayer(UUID id) {
		this.player = id;
		language = PluginTools.getDefaultLanguage();
		lastTime = System.currentTimeMillis();
	}
	
	public BendingPlayer(BendingPlayerData data) {
		this.player = data.getPlayer();
		
		bendings = data.getBendings();
		language = data.getLanguage();
		bendToItem = data.isBendToItem();
		itemAbilities = data.getItemAbilities();
		slotAbilities = data.getSlotAbilities();
		
		specializations = data.getSpecialization();

		lastTime = data.getLastTime();
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return Bending.database.get(player.getUniqueId());
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
					case EarthGrab :
						cd = ConfigManager.earthGrabCooldown;
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
						cd = ConfigManager.icespikeCooldown;
						break;
					case SmokeBomb:
						cd = ConfigManager.smokeBombCooldown;
						break;
					case PoisonnedDart:
						cd = ConfigManager.poisonnedDartCooldown;
						break;
					case Dash :
						cd = ConfigManager.dashCooldown;
						break;
					case FireBlade :
						cd = ConfigManager.fireBladeCooldown;
						break;
					case AstralProjection :
						cd = ConfigManager.astralProjectionCooldown;
						break;
					case Bloodbending:
						cd = ConfigManager.bloodbendingCooldown;
						break;
					case PlasticBomb:
						cd = ConfigManager.plasticCooldown;
						break;
					default:
						cd = 0;
						break;
				}
				abilityCooldowns.put(ability, cd);
			}
		}
	}

	public boolean isOnGlobalCooldown() {
		return (System.currentTimeMillis() <= lastTime + globalCooldown);
	}

	public boolean isOnCooldown(Abilities ability) {
		if (ability == Abilities.AvatarState){
			return false;
		}
			
		if (isOnGlobalCooldown()) {
			return true;
		}

		if (cooldowns.containsKey(ability)) {
			double time = System.currentTimeMillis() - cooldowns.get(ability);
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
		lastTime = time;
		if(ability != null) {
			Bending.callEvent(new AbilityCooldownEvent(this, ability));
		}
	}

	public UUID getPlayerID() {
		return player;
	}

	public boolean isBender() {
		return !bendings.isEmpty();
	}

	public boolean isBender(BendingType type) {
		return bendings.contains(type);
	}
	
	public boolean isSpecialized(BendingSpecializationType specialization) {
		return specializations.contains(specialization);
	}

	public void setBender(BendingType type) {
		removeBender();
		bendings.add(type);
	}

	public void addBender(BendingType type) {
		if (!bendings.contains(type)) {
			bendings.add(type);
		}
	}
	
	public void setSpecialization(BendingSpecializationType specialization) {
		this.clearSpecialization(specialization.getElement());
		specializations.add(specialization);
	}
	public void addSpecialization(BendingSpecializationType specialization) {
		if (!specializations.contains(specialization)) {
			specializations.add(specialization);
		}		
	}
	public void removeSpecialization(BendingSpecializationType specialization) {
		specializations.remove(specialization);
		this.clearAbilities();
	}
	public void clearSpecialization(BendingType element) {
		List<BendingSpecializationType> toRemove = new LinkedList<BendingSpecializationType>();
		for(BendingSpecializationType spe : specializations) {
			if(spe.getElement().equals(element)) {
				toRemove.add(spe);
			}
		}
		for(BendingSpecializationType spe : toRemove) {
			this.removeSpecialization(spe);
		}
		this.clearAbilities();
	}
	public void clearSpecialization() {
		specializations.clear();
	}

	public void clearAbilities() {
		slotAbilities = new HashMap<Integer, Abilities>();
		itemAbilities = new HashMap<Material, Abilities>();
	}

	public void removeBender() {
		clearAbilities();
		specializations.clear();
		bendings.clear();
	}

	public Abilities getAbility() {
		Player player = this.getPlayer();
		if (player == null) {
			return null;
		}
		if (!player.isOnline() || player.isDead()) {
			return null;
		}
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
		Player player = this.getPlayer();
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
		return Bukkit.getServer().getPlayer(player);
	}

	public List<BendingType> getBendingTypes() {
		List<BendingType> list = new ArrayList<BendingType>();
		for (BendingType index : bendings) {
			list.add(index);
		}
		return list;
	}
	
	public String bendingsToString() {
		
		Player pl = getPlayer();
		if (pl != null) {
			String str = pl.getName() + " : \n";
			for (BendingType type : bendings) {
				str+=type.toString() + "\n";
			}
			return str;
		}
		return "This player seems not to exist.";
		
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
		return lastTime;
	}

	public void delete() {
		Bending.database.remove(player);
	}

	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + this.player.toString();
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
	

	public List<BendingSpecializationType> getSpecializations() {
		return specializations;
	}
	
	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(bendings);
		result.setBendToItem(bendToItem);
		result.setItemAbilities(itemAbilities);
		result.setLanguage(language);
		result.setLastTime(lastTime);
		result.setSpecialization(specializations);
		result.setPlayer(this.player);
		result.setSlotAbilities(slotAbilities);

		return result;
	}

}

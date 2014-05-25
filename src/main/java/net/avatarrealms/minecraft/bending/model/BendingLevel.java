package net.avatarrealms.minecraft.bending.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.lang.Math;

import net.avatarrealms.minecraft.bending.data.ConfigManager;
import net.avatarrealms.minecraft.bending.data.CustomSerializable;

import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("BendingLevel")
public class BendingLevel implements CustomSerializable{

		private BendingPlayer bPlayer;
		private BendingType bendingType;
		private Integer level = 1;
		private Integer experience = 0;
		private int cpt = 0;
		
		public BendingLevel(BendingType type, BendingPlayer player) {
			this.bPlayer = player;
			this.bendingType = type;
			this.level = 1;
			this.experience = 0;
			this.cpt = 0;
		}
		
		public Integer getLevel() {
			return level;
		}
		
		public BendingType getBendingType() {
			return bendingType;
		}
		
		public void resetCpt() {
			this.cpt = 0;
		}
		
		public void increaseCpt() {
			cpt++;
			if (cpt >= ConfigManager.ticksBeforeLoseXP) {
				experience -= ConfigManager.xpLost;
				cpt = 0;
				if (experience <= 0) {
					if (level > 1) {
						level --;
						experience = getExperienceNeeded() + experience;
					}
					else {
						experience = 0;
					}
				}
			}
		}

		public String toString() {
			String str = "";
			if (bendingType == BendingType.Air) {
				str+="Air : ";
			}
			else if (bendingType == BendingType.Earth) {
				str+="Earth : ";
			}
			else if (bendingType == BendingType.Fire) {
				str+="Fire : ";
			}
			else if (bendingType == BendingType.Water) {
				str+="Water : ";
			}
			else if (bendingType == BendingType.ChiBlocker) {
				str+= "ChiBlocker : ";
			}
			
			str+= " Level " + level;
			
			if (level < ConfigManager.maxlevel) {
				str+= " with " + experience + "/" + getExperienceNeeded() + " experience";
			}
			return str;
		}
		
		public Integer getExperienceNeeded() {
			double xp;
			Integer xpArr = (int) 0;
			xp = 50*((level+1) * 40 + 1.20*Math.exp((level)/4.5));
			xpArr = (int)(((int)(xp/5))*5/9)*10;
			
			return xpArr;
		}
		
		public BendingLevel(Map<String, Object> map) {
			bendingType = BendingType.getType((Integer) map.get("BendingType"));
			level = (Integer) map.get("Level");
			experience = (Integer) map.get("Experience");
		}

		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("BendingType", BendingType.getIndex(bendingType));
			map.put("Level", level);
			map.put("Experience", experience);
			return map;
		}

		public static BendingLevel deserialize(Map<String, Object> map) {
			return new BendingLevel(map);
		}

		public static BendingLevel valueOf(Map<String, Object> map) {
			return deserialize(map);
		}
		
		public void setLevel (Integer level) {
			if (level >= ConfigManager.maxlevel) {
				level = ConfigManager.maxlevel;
			}
			if (level < 1) {
				level = 1;
			}
			experience = 0;
			this.level = level;
		}
		
		public void giveXP (Integer xpAmount) {
			if (level< ConfigManager.maxlevel) {
				experience+=xpAmount;
				while (experience >= getExperienceNeeded()) {
					experience-=getExperienceNeeded();
					if (level < ConfigManager.maxlevel) {
						String str = "Vous venez de passer au niveau ";
						level++;
						str+= level;
						if (bendingType == BendingType.Air){
							str+=" (Air)";
						}
						else if (bendingType == BendingType.Earth){
							str+=" (Terre)";
						}
						else if (bendingType == BendingType.ChiBlocker){
							str+=" (ChiBlocker)";
						}
						else if (bendingType == BendingType.Fire){
							str+=" (Feu)";
						}
						else if (bendingType == BendingType.Water){
							str+=" (Eau)";
						}
						
						bPlayer.getPlayer().sendMessage(str);
					}	
					
				}
			}
		}
		
		public void earnXP() {
			Random rand = new Random();
			Integer xpReceived = 0;
			
			if (bendingType == BendingType.Fire) {	
				xpReceived = 2 + (int)(level/(rand.nextInt(8)+1));
			}
			else if (bendingType == BendingType.Air){
				xpReceived = 1 + (int)(level/(rand.nextInt(8)+1));
			}
			else if (bendingType == BendingType.ChiBlocker) {
				xpReceived = 8 + (int)(level/(rand.nextInt(8)+1));
			}
			else {
				xpReceived = 5 + (int)(level/(rand.nextInt(8)+1));
			}
			giveXP(xpReceived);
		}
		
		public void setXP(double d) {
			experience = (int)d;
		}
		
		public Integer getXP() {
			return experience;
		}
		public void setBendingPlayer (BendingPlayer player) {
			this.bPlayer = player;
		}
}
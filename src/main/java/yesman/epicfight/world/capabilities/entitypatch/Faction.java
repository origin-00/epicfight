package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.resources.Identifier;
import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface Faction extends ExtensibleEnum {
	ExtensibleEnumManager<Faction> ENUM_MANAGER = new ExtensibleEnumManager<> ("faction");
	
	public Identifier healthBarTexture();
	
	public int healthBarIndex();
	
	public int damageColor();
}
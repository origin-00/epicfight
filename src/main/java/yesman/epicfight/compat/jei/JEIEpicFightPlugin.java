package yesman.epicfight.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import yesman.epicfight.main.EpicFightMod;

@JeiPlugin
public class JEIEpicFightPlugin implements IModPlugin{
	@Override
	public Identifier getPluginUid() {
        return EpicFightMod.identifier("jei_plugin");
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IModPlugin.super.registerCategories(registration);
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		IModPlugin.super.registerRecipes(registration);
	}
	
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		IModPlugin.super.registerGuiHandlers(registration);
	}
}
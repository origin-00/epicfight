package yesman.epicfight.epicskins.client.screen;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;

public class AwaitIconMessageScreen extends MessageScreen<Object> {
	int tickCount;
	
	public AwaitIconMessageScreen(String title, String message, Screen parentScreen, int width, int height) {
		super(title, message, parentScreen, null, width, height);
	}
	
	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(0F, 0F);
		
		guiGraphics.pose().translate((float)(this.width / 2), (float)(this.height / 2 + 8));
		
		for (int i = 0; i < 12; i++) {
			int mod = (i * 21 + this.tickCount) % 256;
			int color = ((mod << 16) | (mod << 8) | (mod << 0)) | 254 << 24;
			
			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().rotate((float)Math.toRadians(-30.0F * i));
			guiGraphics.pose().translate(-1F, 0F);
			guiGraphics.fill(0, 11, 2, 18, color);
			guiGraphics.pose().popMatrix();
		}
		
		guiGraphics.pose().popMatrix();
		this.tickCount+=3;
	}
}

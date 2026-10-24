package yesman.epicfight.api.asset;

import net.minecraft.resources.Identifier;

public record SelfAccessor<A>(Identifier registryName, A asset) implements AssetAccessor<A> {
	public static <A> SelfAccessor<A> create(Identifier registryName, A asset) {
		return new SelfAccessor<> (registryName, asset);
	}
	
	@Override
	public A get() {
		return this.asset;
	}
	
	@Override
	public boolean inRegistry() {
		return false;
	}
}

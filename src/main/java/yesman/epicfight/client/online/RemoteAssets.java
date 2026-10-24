package yesman.epicfight.client.online;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.client.online.texture.RemoteTexture;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RemoteAssets {
	private static final RemoteAssets INSTANCE = new RemoteAssets();
	
	public static RemoteAssets getInstance() {
		return INSTANCE;
	}
	
	private final Map<Integer, RemoteMeshAccessor> cachedMeshes = Maps.newConcurrentMap();
	
	private final Set<Identifier> registeredTextures = new HashSet<> ();
	
	public synchronized AssetAccessor<Mesh> getRemoteMesh(int seq, String path, @Nullable Consumer<Mesh> callback) {
		if (this.cachedMeshes.containsKey(seq)) {
			RemoteMeshAccessor cachedMesh = this.cachedMeshes.get(seq);
			
			if (callback != null) {
				if (cachedMesh.get() == null) {
					cachedMesh.addWork(callback);
				} else {
					callback.accept(cachedMesh.get());
				}
			}
			
			return cachedMesh;
		}
		
		RemoteMeshAccessor remoteMeshAccessor = new RemoteMeshAccessor();
		remoteMeshAccessor.addWork(callback);
		this.cachedMeshes.put(seq, remoteMeshAccessor);
		
		CompletableFuture.runAsync(() -> {
			EpicFightServerConnectionHelper.loadRemoteMesh(EpicFightSharedConstants.webServerDomain(), path, (mesh, exception) -> {
				if (exception != null) {
					EpicFightMod.LOGGER.error("Failed at loading remote mesh " + seq + ": " + exception.getMessage());
					exception.printStackTrace();
				} else {
					remoteMeshAccessor.load(mesh);
				}
			});
		});

		return this.cachedMeshes.get(seq);
	}
	
	public synchronized Identifier getRemoteTexture(String fileName) {
		Identifier textureLocation = Identifier.fromNamespaceAndPath(EpicFightMod.EPICSKINS_MODID, "textures/remote/" + fileName);
		// TextureManager#getTexture would load (and cache) a missing resource texture for an unknown id.
		if (this.registeredTextures.add(textureLocation)) {
			Minecraft.getInstance().getTextureManager().register(textureLocation, new RemoteTexture(EpicFightSharedConstants.webServerDomain() + "/textures/" + fileName));
		}
		
		return textureLocation;
	}

    private static class RemoteMeshAccessor implements AssetAccessor<Mesh> {
		private Queue<Consumer<Mesh>> callback = Queues.newArrayDeque();
		private Mesh mesh;
		
		public void addWork(Consumer<Mesh> callback) {
			this.callback.add(callback);
		}
		
		public void load(Mesh mesh) {
			this.mesh = mesh;
			
			Minecraft.getInstance().execute(() -> {
				this.callback.forEach((callback) -> callback.accept(mesh));
				this.callback.clear();
				this.callback = null;
			});
		}
		
		@Override
		public Mesh get() {
			return this.mesh;
		}
		
		@Override
		public Identifier registryName() {
			return null;
		}
		
		@Override
		public boolean isPresent() {
			return this.mesh != null;
		}

		@Override
		public boolean inRegistry() {
			return false;
		}
	}
}

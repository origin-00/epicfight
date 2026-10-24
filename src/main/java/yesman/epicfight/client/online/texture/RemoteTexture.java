package yesman.epicfight.client.online.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/// A texture downloaded from the Epic Skins web server.
///
/// Up to 1.21.4 this was a `SimpleTexture` whose GL texture was re-uploaded once the download
/// finished. Textures are GPU objects with a fixed size since 1.21.5, so it starts as a transparent
/// 1x1 texture and is replaced by a texture of the downloaded image's size on the render thread.
public class RemoteTexture extends AbstractTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final String urlString;
	@Nullable
	private CompletableFuture<?> future;

	public RemoteTexture(String urlString) {
		this.urlString = urlString;
		NativeImage placeholder = new NativeImage(1, 1, true);
		this.replaceTexture(placeholder);
		placeholder.close();
		this.download();
	}

	private void replaceTexture(NativeImage image) {
		GpuDevice device = RenderSystem.getDevice();
		GpuTexture texture = device.createTexture(() -> "Remote texture " + this.urlString, GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, 1);
		device.createCommandEncoder().writeToTexture(texture, image);

		this.close();
		this.texture = texture;
		this.textureView = device.createTextureView(texture);
		this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
	}

	private void download() {
		if (this.future != null) {
			return;
		}

		this.future = CompletableFuture.runAsync(() -> {
			LOGGER.debug("Downloading http texture from {}", this.urlString);

			try {
				HttpURLConnection httpurlconnection = (HttpURLConnection)(URI.create(this.urlString).toURL()).openConnection(Minecraft.getInstance().getProxy());
				httpurlconnection.setDoInput(true);
				httpurlconnection.setDoOutput(false);
				httpurlconnection.connect();

				try {
					if (httpurlconnection.getResponseCode() / 100 == 2) {
						NativeImage image = this.load(httpurlconnection.getInputStream());

						if (image != null) {
							Minecraft.getInstance().execute(() -> {
								this.replaceTexture(image);
								image.close();
							});
						}
					}
				} finally {
					httpurlconnection.disconnect();
				}
			} catch (Exception exception) {
				LOGGER.error("Couldn't download http texture", (Throwable) exception);
			}
		}, Util.backgroundExecutor());
	}

	@Nullable
	private NativeImage load(InputStream pStream) {
		NativeImage nativeimage = null;

		try {
			nativeimage = NativeImage.read(pStream);
		} catch (Exception exception) {
			LOGGER.warn("Error while loading the skin texture", (Throwable)exception);
		}

		return nativeimage;
	}
}

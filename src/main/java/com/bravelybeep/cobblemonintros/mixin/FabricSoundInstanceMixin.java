package com.bravelybeep.cobblemonintros.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bravelybeep.cobblemonintros.AudioStreamWithIntro;
import com.bravelybeep.cobblemonintros.CobblemonIntros;
import com.cobblemon.mod.common.client.sound.instances.BattleMusicInstance;

import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;

@Mixin(FabricSoundInstance.class)
public interface FabricSoundInstanceMixin {
	@Inject(method = "getAudioStream(Lnet/minecraft/client/sounds/SoundBufferLibrary;Lnet/minecraft/resources/ResourceLocation;Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
	public default void getAudioStream(
			SoundBufferLibrary loader,
			ResourceLocation id,
			boolean repeatInstantly,
			CallbackInfoReturnable<CompletableFuture<AudioStream>> info) {
		if ((Object) this instanceof BattleMusicInstance) {
			var introPath = id.getPath().replaceAll("^(.*?)(\\.[^.]+)?$", "$1_intro$2");
			var introId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), introPath);

			var first = loader.getStream(introId, false).exceptionally(ex -> {
				CobblemonIntros.LOGGER.warn("Failed to find: " + introId);
				return null;
			});
			var second = loader.getStream(id, repeatInstantly);

			var combined = first.thenCombine(second, (x, y) -> (x == null) ? y : new AudioStreamWithIntro(x, y));
			info.setReturnValue(combined);
		}
	}
}
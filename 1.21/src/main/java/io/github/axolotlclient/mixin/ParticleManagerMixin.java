/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.mixin;

import java.util.Collection;
import java.util.Queue;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	@Unique
	private ParticleType<?> cachedType;

	@Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At(value = "HEAD"), cancellable = true)
	private void axolotlclient$afterCreation(ParticleEffect parameters, double x, double y, double z, double velocityX,
											 double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
		cachedType = parameters.getType();

		if (!Particles.getInstance().getShowParticle(cachedType)) {
			cir.setReturnValue(null);
			cir.cancel();
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At(value = "HEAD"))
	private void axolotlclient$afterCreation(Particle particle, CallbackInfo ci) {
		if (cachedType != null) {
			Particles.getInstance().particleMap.put(particle, cachedType);
			cachedType = null;
		}
	}

	@Redirect(method = "tickParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;tickParticle(Lnet/minecraft/client/particle/Particle;)V"))
	private void axolotlclient$removeParticlesWhenRemoved(ParticleManager instance, Particle particle) {
		if (!particle.isAlive()) {
			Particles.getInstance().particleMap.remove(particle);
		}
		tickParticle(particle);
	}

	@Shadow
	protected abstract void tickParticle(Particle particle);

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Queue;removeAll(Ljava/util/Collection;)Z"))
	private boolean axolotlclient$removeEmitterParticlesWhenRemoved(Queue<Particle> instance, Collection<Particle> collection) {
		collection.forEach(particle -> Particles.getInstance().particleMap.remove(particle));

		return instance.removeAll(collection);
	}

	@Inject(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void axolotlclient$applyOptions(LightmapTextureManager lightmap,
											Camera camera,
											float tickDelta,
											CallbackInfo ci,
											@Local Particle particle) {
		if (Particles.getInstance().particleMap.containsKey(particle)) {
			Particles.getInstance().applyOptions(particle);
		}
	}
}
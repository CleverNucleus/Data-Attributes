package com.github.clevernucleus.dataattributes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.clevernucleus.dataattributes.DataAttributes;
import com.github.clevernucleus.dataattributes.mutable.MutableAttributeContainer;
import com.github.clevernucleus.dataattributes.mutable.MutableIntFlag;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 999)
abstract class LivingEntityMixin {
	
	@Unique
	private AttributeContainer data_attributes;
	
	@Unique
	private int data_updateFlag;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo info) {
		this.data_attributes = new AttributeContainer(DataAttributes.MANAGER.getContainer(entityType));
		this.data_updateFlag = ((MutableIntFlag)world.getLevelProperties()).getUpdateFlag();
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		((MutableAttributeContainer)this.data_attributes).setLivingEntity(livingEntity);
		livingEntity.setHealth(livingEntity.getMaxHealth());
	}
	
	@Inject(method = "getAttributes", at = @At("HEAD"), cancellable = true)
	private void data_getAttributes(CallbackInfoReturnable<AttributeContainer> info) {
		if(this.data_attributes != null) {
			info.setReturnValue(this.data_attributes);
		}
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickActiveItemStack()V"))
	private void data_tick(CallbackInfo info) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		final int updateFlag = ((MutableIntFlag)livingEntity.world.getLevelProperties()).getUpdateFlag();
		
		if(this.data_updateFlag != updateFlag) {
			AttributeContainer container = livingEntity.getAttributes();
			
			@SuppressWarnings("unchecked")
			AttributeContainer container2 = new AttributeContainer(DataAttributes.MANAGER.getContainer((EntityType<? extends LivingEntity>)livingEntity.getType()));
			((MutableAttributeContainer)container2).setLivingEntity(livingEntity);
			container2.setFrom(container);
			this.data_attributes = container2;
			this.data_updateFlag = updateFlag;
		}
	}
}

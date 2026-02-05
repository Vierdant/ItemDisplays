package me.arkon.itemdisplays.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.arkon.itemdisplays.ItemDisplays;
import me.arkon.itemdisplays.component.DisplayAnchorComponent;
import me.arkon.itemdisplays.component.DisplayedItemComponent;
import me.arkon.itemdisplays.util.ItemUtils;
import org.jetbrains.annotations.NotNull;

public class DisplayItemInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<DisplayItemInteraction> CODEC = BuilderCodec.builder(
                    DisplayItemInteraction.class, DisplayItemInteraction::new, SimpleInstantInteraction.CODEC
            )
            .documentation("Handles an item inside display entity behaviour.")
            .build();

    @Override
    protected void firstRun(@NotNull InteractionType type, @NotNull InteractionContext context, @NotNull CooldownHandler handler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        assert commandBuffer != null;

        World world = commandBuffer.getExternalData().getWorld();
        Ref<EntityStore> targetRef = context.getTargetEntity();
        MovementStatesComponent movementStates = context.getEntity().getStore().getComponent(context.getEntity(), MovementStatesComponent.getComponentType());

        if (targetRef == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        DisplayedItemComponent displayComponent = commandBuffer.getComponent(targetRef, DisplayedItemComponent.getComponentType());
        if (displayComponent == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        if (movementStates != null && movementStates.getMovementStates().crouching) {
            Vector3i pos = displayComponent.getDisplayPosition();

            commandBuffer.run(store -> {
                if (displayComponent.getItemStack() == null) return;

                ItemUtils.spawnItem(store, displayComponent, targetRef);
                store.removeEntity(targetRef, RemoveReason.REMOVE);

                WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
                if (chunk != null) {
                    Ref<ChunkStore> chunkRef = chunk.getBlockComponentEntity(pos.x, pos.y, pos.z);

                    if (chunkRef != null) {
                        DisplayAnchorComponent anchor = world.getChunkStore().getStore().getComponent(chunkRef, DisplayAnchorComponent.getComponentType());

                        if (anchor != null) {
                            anchor.setAnchoredEntity(null);
                        }
                    }
                }

                world.performBlockUpdate(pos.x, pos.y, pos.z);
            });
        }
    }
}

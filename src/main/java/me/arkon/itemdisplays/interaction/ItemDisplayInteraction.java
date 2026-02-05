package me.arkon.itemdisplays.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.itemdisplays.ItemDisplays;
import me.arkon.itemdisplays.component.DisplayAnchorComponent;
import me.arkon.itemdisplays.component.DisplayedItemComponent;
import me.arkon.itemdisplays.display.*;
import me.arkon.itemdisplays.util.ItemUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ItemDisplayInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<ItemDisplayInteraction> CODEC = BuilderCodec.builder(
                    ItemDisplayInteraction.class, ItemDisplayInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Handles an item display entity behaviour.")
            .build();


    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull Vector3i pos,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        long indexChunk = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk worldchunk = world.getChunk(indexChunk);
        BlockType blockType = world.getBlockType(pos);
        MovementStatesComponent movementStates = context.getEntity().getStore().getComponent(context.getEntity(), MovementStatesComponent.getComponentType());
        if (blockType == null || worldchunk == null) {
            context.getState().state = InteractionState.Failed;
            return;
        };


        int rotationIndex = world.getBlockRotationIndex(pos.x, pos.y, pos.z);

        Ref<ChunkStore> chunkRef = worldchunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
        if (chunkRef == null) {
            chunkRef = BlockModule.ensureBlockEntity(worldchunk, pos.x, pos.y, pos.z);
            if (chunkRef == null) {
                ItemDisplays.LOGGER.atSevere().log("Failed to interact with item frame due to null chunk ref.");
                context.getState().state = InteractionState.Failed;
                return;
            }
        }


        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        DisplayAnchorComponent displayAnchorComponent = chunkStore.getComponent(chunkRef, DisplayAnchorComponent.getComponentType());

        if (displayAnchorComponent == null) {
            displayAnchorComponent = chunkStore.addComponent(chunkRef, DisplayAnchorComponent.getComponentType());
        }

        if (displayAnchorComponent.getAnchoredEntity() != null) {
            Ref<EntityStore> ref  = world.getEntityStore().getRefFromUUID(displayAnchorComponent.getAnchoredEntity());
            if (ref == null) {
                displayAnchorComponent.setAnchoredEntity(null);
            } else {
                if (movementStates != null && movementStates.getMovementStates().crouching) {
                    commandBuffer.run(store -> {
                        DisplayedItemComponent component = store.getComponent(ref, DisplayedItemComponent.getComponentType());
                        if (component != null) {
                            ItemStack item = component.getItemStack();
                            if (item != null) {
                                ItemUtils.spawnItem(store, component, ref);
                            }
                        }
                        store.removeEntity(ref, RemoveReason.REMOVE);
                    });

                    displayAnchorComponent.setAnchoredEntity(null);
                    return;
                }

                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        if (itemInHand == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ItemStack stackClone = itemInHand.withQuantity(1);
        if (context.getHeldItemContainer() != null) {
            ItemStackSlotTransaction itemstackslottransaction = context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), itemInHand, 1);
            if (!itemstackslottransaction.succeeded()) {
                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        DisplayContext ctx = new DisplayContext(
                world,
                commandBuffer,
                pos,
                blockType,
                rotationIndex,
                stackClone,
                displayAnchorComponent
        );

        spawnDisplay(ctx);
    }

    @Override
    protected void simulateInteractWithBlock(
            @Nonnull InteractionType type, @Nonnull InteractionContext context, @javax.annotation.Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
    ) {
        // intentionally empty
    }


    private void spawnDisplay(DisplayContext ctx) {
        Item item = Item.getAssetMap().getAsset(ctx.itemStack().getItemId());
        if (item == null) return;

        DisplayTransform transform = computeTransform(ctx, item);
        DisplayVisual visual = resolveVisual(item);

        ctx.commandBuffer().run(store -> {
            Vector3i blockPos = ctx.blockPos();
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            buildEntity(holder, store, ctx, transform, visual);
            UUID uuid = UUID.randomUUID();
            holder.putComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
            holder.putComponent(DisplayedItemComponent.getComponentType(), new DisplayedItemComponent(ctx.itemStack(), blockPos));
            store.addEntity(holder, AddReason.SPAWN);

            ctx.displayAnchor().setAnchoredEntity(uuid);
            ctx.world().performBlockUpdate(blockPos.x, blockPos.y, blockPos.z);
        });
    }


    private DisplayTransform computeTransform(DisplayContext ctx, Item item) {
        Vector3d blockCenter = new Vector3d();
        ctx.blockType().getBlockCenter(ctx.rotationIndex(), blockCenter);

        Vector3d worldPos = new Vector3d(
                ctx.blockPos().x,
                ctx.blockPos().y,
                ctx.blockPos().z
        ).add(blockCenter);

        worldPos.add(computeDisplayOffset(ctx.rotationIndex(), item.getIconProperties(), item.getId()));

        Vector3f rotation = computeDisplayRotation(ctx.rotationIndex());

        return new DisplayTransform(worldPos, rotation);
    }

    private Vector3d computeDisplayOffset(int rotationIndex, AssetIconProperties properties, String itemId) {
        double iconY = 0.0;
        if (properties != null && properties.getTranslation() != null) {
            iconY = properties.getTranslation().y;
        }

        double compressed = Math.tanh(iconY / 12.0);
        OffsetBounds bounds = DisplayOffsetProfile.getBounds(itemId);
        double limited = compressed * bounds.limit();
        double clamped = Math.clamp(limited, bounds.min(), bounds.max());
        double worldOffset = properties != null ? clamped : -0.1;

        if (rotationIndex == 0 || rotationIndex == 8) {
            if (itemId.toLowerCase().contains("weapon")
                    || itemId.toLowerCase().contains("tool")
                    || itemId.toLowerCase().contains("glider")
                    || itemId.toLowerCase().contains("armor")) {
                return new Vector3d(worldOffset, 0, 0.0);
            } else {
                return new Vector3d(worldOffset + 0.5, -0.5, 0.0);
            }
        } else {
            return new Vector3d(0, worldOffset, 0);
        }
    }


    private Vector3f computeDisplayRotation(int rotationIndex) {
        Vector3f rotation = new Vector3f();

        switch (rotationIndex) {
            // Wall-mounted
            case 4 -> rotation.addRotationOnAxis(Axis.Y, -90); // front
            case 7 -> rotation.addRotationOnAxis(Axis.Y, 180); // right
            case 6 -> rotation.addRotationOnAxis(Axis.Y, 90);  // back

            // Ground
            case 0 -> {
                rotation.addRotationOnAxis(Axis.Z, -90);
            }

            // Ceiling
            case 8 -> {
                rotation.addRotationOnAxis(Axis.Z, -90);
            }

            default -> rotation.add(0f, 0f, 0f);
        }

        return rotation;
    }


    private DisplayVisual resolveVisual(@Nonnull Item item) {
        Model model = getItemModel(item);
        float scale = item.getIconProperties() != null
                ? item.getIconProperties().getScale()
                : 0.4f;
        DisplayKind displayKind =
                model != null ? DisplayKind.MODEL
                        : item.hasBlockType() ? DisplayKind.BLOCK
                        : DisplayKind.ITEM;

        return new DisplayVisual(null, scale, displayKind);
    }

    private void buildEntity(
            Holder<EntityStore> holder,
            Store<EntityStore> store,
            DisplayContext ctx,
            DisplayTransform transform,
            DisplayVisual visual
    ) {
        holder.addComponent(
                NetworkId.getComponentType(),
                new NetworkId(store.getExternalData().takeNextNetworkId())
        );

        holder.addComponent(
                TransformComponent.getComponentType(),
                new TransformComponent(transform.position(), transform.rotation())
        );

        holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
        holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(transform.rotation()));
        holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
        holder.ensureComponent(UUIDComponent.getComponentType());


        holder.ensureComponent(Interactable.getComponentType());
        holder.ensureComponent(PrefabCopyableComponent.getComponentType());
        Interactions interactions = new Interactions();
        interactions.setInteractionId(InteractionType.Use, "Item_In_Display");
        interactions.setInteractionHint("server.interactionHints.useItemInDisplay");
        holder.addComponent(Interactions.getComponentType(), interactions);

        applyVisual(holder, ctx.itemStack(), visual);
    }

    private void applyVisual(
            Holder<EntityStore> holder,
            ItemStack itemStack,
            DisplayVisual visual
    ) {
        ItemStack displayStack = new ItemStack(itemStack.getItemId(), 1);
        displayStack.setOverrideDroppedItemAnimation(true);


        switch (visual.kind()) {
            case MODEL -> {
                holder.addComponent(
                        ModelComponent.getComponentType(),
                        new ModelComponent(visual.model())
                );
                Item item = Item.getAssetMap().getAsset(itemStack.getItemId());
                if (item == null) return;
                holder.addComponent(
                        PersistentModel.getComponentType(),
                        new PersistentModel(
                                new Model.ModelReference(
                                        getItemModelId(item),
                                        visual.scale(),
                                        null,
                                        true
                                )
                        )
                );
                holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(displayStack));
            }

            case BLOCK -> {
                holder.addComponent(BlockEntity.getComponentType(),
                        new BlockEntity(itemStack.getItemId()));
                holder.addComponent(EntityScaleComponent.getComponentType(),
                        new EntityScaleComponent(visual.scale()));
                holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(displayStack));
            }

            case ITEM -> {
                holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(displayStack));
                holder.addComponent(EntityScaleComponent.getComponentType(),
                        new EntityScaleComponent(visual.scale()));
            }
        }
    }


    @Nullable
    private String getItemModelId(@Nonnull Item item) {
        String modelId = item.getModel();
        if (modelId == null && item.hasBlockType()) {
            BlockType blockType = BlockType.getAssetMap().getAsset(item.getId());
            if (blockType != null && blockType.getCustomModel() != null) {
                modelId = blockType.getCustomModel();
            }
        }
        return modelId;
    }

    @Nullable
    private Model getItemModel(@Nonnull Item item) {
        String modelId = getItemModelId(item);
        if (modelId == null) return null;

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
        return modelAsset != null
                ? Model.createStaticScaledModel(
                modelAsset,
                item.getIconProperties().getScale()
        )
                : null;
    }
}

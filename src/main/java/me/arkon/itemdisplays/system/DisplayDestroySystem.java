package me.arkon.itemdisplays.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.itemdisplays.component.DisplayedItemComponent;
import me.arkon.itemdisplays.util.ItemUtils;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

public class DisplayDestroySystem extends EntityTickingSystem<EntityStore> {
    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        ++this.tickCounter;
        if (this.tickCounter >= 10) {
            this.tickCounter = 0;
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            DisplayedItemComponent component = store.getComponent(ref, DisplayedItemComponent.getComponentType());
            if (component != null) {
                ItemStack item = component.getItemStack();
                if (component.getDisplayPosition() == null) {
                    commandBuffer.run(entityStore -> {
                        if (item != null) {
                            ItemUtils.spawnItem(entityStore, component, ref);
                        }

                        store.removeEntity(ref, RemoveReason.REMOVE);
                    });
                } else {
                    World world = store.getExternalData().getWorld();
                    BlockType blockType = world.getBlockType(component.getDisplayPosition());
                    if (blockType == null || !blockType.getId().contains("ItemDisplay")) {
                        commandBuffer.run((entityStore) -> {
                            if (item != null) {
                                ItemUtils.spawnItem(entityStore, component, ref);
                            }

                            store.removeEntity(ref, RemoveReason.REMOVE);
                        });
                    }
                }
            }

        }
    }

    @NullableDecl
    public Query<EntityStore> getQuery() {
        return DisplayedItemComponent.getComponentType();
    }
}

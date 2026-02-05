package me.arkon.itemdisplays.util;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.itemdisplays.component.DisplayedItemComponent;

// Credit to ShyNieke
public class ItemUtils {
    public static void spawnItem(ComponentAccessor<EntityStore> store, DisplayedItemComponent component, Ref<EntityStore> ref) {
        Vector3d position = null;
        if (component.getDisplayPosition() != null) {
            position = component.getDisplayPosition().toVector3d().add(0.5F, 0.25F, 0.5F);
        }

        if (position == null) {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) {
                return;
            }

            position = transformComponent.getPosition();
        }

        Holder<EntityStore> holder = ItemComponent.generateItemDrop(store, component.getItemStack(), position, Vector3f.ZERO, 0.0F, 0.0F, 0.0F);
        if (holder != null) {
            ItemComponent itemcomponent = holder.getComponent(ItemComponent.getComponentType());
            if (itemcomponent != null) {
                itemcomponent.setPickupDelay(1.5F);
            }

            store.addEntity(holder, AddReason.SPAWN);
        }

    }
}

package me.arkon.itemdisplays.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

public class DisplayedItemComponent implements Component<EntityStore> {
    public static final BuilderCodec<DisplayedItemComponent> CODEC;
    public static ComponentType<EntityStore, DisplayedItemComponent> TYPE;

    private Vector3i displayPosition;
    private ItemStack itemStack;

    public DisplayedItemComponent() {}

    public DisplayedItemComponent(ItemStack item, Vector3i displayPosition) {
        this.itemStack = item;
        this.displayPosition = displayPosition;
    }

    public DisplayedItemComponent(Vector3i displayPosition) {
        this.itemStack = null;
        this.displayPosition = displayPosition;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack item) {
        this.itemStack = item;
    }

    public Vector3i getDisplayPosition() {
        return this.displayPosition;
    }

    public void setDisplayPosition(Vector3i pos) {
        this.displayPosition = pos;
    }

    public static ComponentType<EntityStore, DisplayedItemComponent> getComponentType() {
        return DisplayedItemComponent.TYPE;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return new DisplayedItemComponent(this.itemStack, this.displayPosition);
    }

    static {
        CODEC = BuilderCodec.builder(DisplayedItemComponent.class, DisplayedItemComponent::new)
                .append(new KeyedCodec<>("DisplayPosition", Vector3i.CODEC),
                        (component, displayPosition) -> component.displayPosition = displayPosition,
                        (component) -> component.displayPosition).add()
                .append(new KeyedCodec<>("ItemStack", ItemStack.CODEC),
                        (component, itemStack) -> component.itemStack = itemStack,
                        (component) -> component.itemStack).add()
                .build();
    }
}

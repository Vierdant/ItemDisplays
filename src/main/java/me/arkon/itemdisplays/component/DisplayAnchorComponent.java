package me.arkon.itemdisplays.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DisplayAnchorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<DisplayAnchorComponent> CODEC;
    public static ComponentType<ChunkStore, DisplayAnchorComponent> TYPE;

    private UUID anchoredEntity;

    public DisplayAnchorComponent(){}

    public DisplayAnchorComponent(UUID attachedEntity) {
        this.anchoredEntity = attachedEntity;
    }

    public void setAnchoredEntity(UUID attachedEntity) {
        this.anchoredEntity = attachedEntity;
    }

    public UUID getAnchoredEntity() {
        return this.anchoredEntity;
    }

    public static ComponentType<ChunkStore, DisplayAnchorComponent> getComponentType() {
        return DisplayAnchorComponent.TYPE;
    }

    @Override
    public @Nullable Component<ChunkStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return new DisplayAnchorComponent(this.anchoredEntity);
    }


    static {
        CODEC = BuilderCodec.builder(DisplayAnchorComponent.class, DisplayAnchorComponent::new)
                .append(new KeyedCodec<>("AnchoredEntity", Codec.UUID_BINARY),
                        (component, anchoredEntity) -> component.anchoredEntity = anchoredEntity,
                        (component) -> component.anchoredEntity).add().build();
    }
}

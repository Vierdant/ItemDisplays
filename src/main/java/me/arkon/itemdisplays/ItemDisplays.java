package me.arkon.itemdisplays;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.arkon.itemdisplays.component.DisplayAnchorComponent;
import me.arkon.itemdisplays.component.DisplayedItemComponent;
import me.arkon.itemdisplays.interaction.DisplayItemInteraction;
import me.arkon.itemdisplays.interaction.ItemDisplayInteraction;
import me.arkon.itemdisplays.system.DisplayDestroySystem;

public class ItemDisplays extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ItemDisplays(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC)
                .register("UseItemDisplay", ItemDisplayInteraction.class, ItemDisplayInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
                .register("UseItemInDisplay", DisplayItemInteraction.class, DisplayItemInteraction.CODEC);
        DisplayAnchorComponent.TYPE = this.getChunkStoreRegistry().registerComponent(DisplayAnchorComponent.class, "ItemDisplays_DisplayAnchor", DisplayAnchorComponent.CODEC);
        DisplayedItemComponent.TYPE = this.getEntityStoreRegistry().registerComponent(DisplayedItemComponent.class, "ItemDisplays_DisplayedItem", DisplayedItemComponent.CODEC);

    }

    @Override
    protected void start() {
        super.start();
        this.getEntityStoreRegistry().registerSystem(new DisplayDestroySystem());
    }
}
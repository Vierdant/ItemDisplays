package me.arkon.itemdisplays.display;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.itemdisplays.component.DisplayAnchorComponent;

public record DisplayContext(
        World world,
        CommandBuffer<EntityStore> commandBuffer,
        Vector3i blockPos,
        BlockType blockType,
        int rotationIndex,
        ItemStack itemStack,
        DisplayAnchorComponent displayAnchor
) {}

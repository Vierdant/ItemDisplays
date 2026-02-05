package me.arkon.itemdisplays.display;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;

import javax.annotation.Nullable;

public record DisplayVisual(
        @Nullable Model model,
        float scale,
        DisplayKind kind
) {}

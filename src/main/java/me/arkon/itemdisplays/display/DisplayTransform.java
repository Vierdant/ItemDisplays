package me.arkon.itemdisplays.display;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

public record DisplayTransform(
        Vector3d position,
        Vector3f rotation
) {}

package me.arkon.itemdisplays.display;

import java.util.Map;

public class DisplayOffsetProfile {

    private DisplayOffsetProfile() {
    }

    private static final Map<String, OffsetBounds> OFFSET_MAX = Map.of(
            "furniture", new OffsetBounds(-0.2, -0.1, 0.18),
            "block", new OffsetBounds(-0.1, -0.1, 0.1),
            "default", new OffsetBounds(-0.3, -0.08, 0.3)
    );

    public static OffsetBounds getBounds(String itemId) {
        String id = itemId.toLowerCase();

        for (Map.Entry<String, OffsetBounds> entry: OFFSET_MAX.entrySet()) {
            if (!entry.getKey().equals("default") && id.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return OFFSET_MAX.get("default");
    }

}

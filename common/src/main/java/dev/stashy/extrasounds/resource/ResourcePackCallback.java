package dev.stashy.extrasounds.resource;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.stashy.extrasounds.resource.util.IrremovableList;
import net.minecraft.resource.ResourcePack;

import java.util.List;

public interface ResourcePackCallback {
    Event<ResourcePackCallback> BEFORE_VANILLA = EventFactory.of(r -> rs -> {
        IrremovableList<ResourcePack> packs = new IrremovableList<>(rs, $ -> {});
        for (ResourcePackCallback callback : r) {
            callback.insert(packs);
        }
    });

    void insert(List<ResourcePack> resources);
}

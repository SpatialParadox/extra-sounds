package dev.stashy.extrasounds;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.Registries;
import dev.stashy.extrasounds.debug.DebugUtils;
import dev.stashy.extrasounds.mapping.SoundPackLoader;
import dev.stashy.extrasounds.sound.SoundType;
import dev.stashy.extrasounds.sound.Sounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ExtraSounds {
    public static final String MOD_ID = "extrasounds";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));

    public static void init() {
        DebugUtils.init();
        SoundPackLoader.init();
    }

    public static void playHotbarSound(int i) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
        if (stack.getItem() == Items.AIR) {
            SoundManager.playSound(Sounds.HOTBAR_SCROLL, SoundType.HOTBAR);
        } else {
            SoundManager.playSound(stack, SoundType.HOTBAR);
        }
    }

    public static void playInventoryClick(Slot slot, ItemStack cursor, SlotActionType actionType) {
        ItemStack clicked = slot.getStack();
        boolean hasCursor = !cursor.isEmpty();
        boolean hasSlot = !clicked.isEmpty();

        switch (actionType) {
            case PICKUP_ALL:
                if (hasCursor) {
                    SoundManager.playSound(Sounds.ITEM_PICK_ALL, SoundType.PICKUP);
                }

                break;
            case CLONE:
                SoundManager.playSound(Sounds.ITEM_CLONE, SoundType.PLACE);
                break;
            case QUICK_MOVE:
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || client.player.currentScreenHandler instanceof PlayerScreenHandler ||
                        client.player.currentScreenHandler == null) {
                    break;
                }

                if (client.player.currentScreenHandler.slots
                        .parallelStream()
                        .filter((s) -> s.inventory != slot.inventory)
                        .filter((s) -> !(s.inventory instanceof CraftingInventory
                                || s.inventory instanceof CraftingResultInventory))
                        .noneMatch(
                                (s) -> !s.hasStack() || s.getStack().getItem()
                                        .equals(slot.getStack().getItem()) && s
                                        .getStack().getCount() < s.getStack().getMaxCount())) {
                    break;
                }
            default:
                if (hasCursor) {
                    SoundManager.playSound(cursor, SoundType.PLACE);
                } else if (hasSlot) {
                    SoundManager.playSound(clicked, SoundType.PICKUP);
                }
        }
    }

    public static String getClickId(Identifier id) {
        return getClickId(id, true);
    }

    public static String getClickId(Identifier id, boolean includeNamespace) {
        return (includeNamespace ? "extrasounds:" : "") + "item.click." + id.getNamespace() + "." + id.getPath();
    }
}

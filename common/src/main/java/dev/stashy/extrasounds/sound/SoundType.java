package dev.stashy.extrasounds.sound;

public enum SoundType
{
    PICKUP(1f, SoundTypeCategory.INVENTORY),
    PLACE(0.9f, SoundTypeCategory.INVENTORY),
    HOTBAR(1f, SoundTypeCategory.SCROLL),
    EFFECT(1f, SoundTypeCategory.EFFECTS),
    CHAT(1f, SoundTypeCategory.CHAT);

    public final float pitch;
    public final SoundTypeCategory category;

    SoundType(float pitch, SoundTypeCategory category) {
        this.pitch = pitch;
        this.category = category;
    }
}

package dev.gacbl.logicore.api.compat.ae2;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IGridNodeService {
    void serverTick();

    void onRemove();

    void save(CompoundTag tag, HolderLookup.Provider registries);

    void load(CompoundTag tag, HolderLookup.Provider registries);
}

package dev.gacbl.logicore.api.compat.ae2;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface IGridNodeService {
    void serverTick();

    void onRemove();

    void save(ValueOutput output);

    void load(ValueInput input);
}

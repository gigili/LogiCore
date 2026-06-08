package dev.gacbl.logicore.api.compat;

import guideme.Guide;
import net.minecraft.resources.Identifier;

public class GuideMeSupport {
    public static void setupGuide() {
        Guide.builder(Identifier.parse("logicore:guide")).build();
    }
}

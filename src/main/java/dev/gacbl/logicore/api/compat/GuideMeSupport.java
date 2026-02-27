package dev.gacbl.logicore.api.compat;

import guideme.Guide;
import net.minecraft.resources.ResourceLocation;

public class GuideMeSupport {
    public static void setupGuide() {
        Guide.builder(ResourceLocation.parse("logicore:guide")).build();
    }
}

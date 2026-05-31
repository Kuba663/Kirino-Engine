package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.simpletext.SimpleTextProducer;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleTextDebugHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        ICS.instance().text()
                .begin()
                .append("ABCabcijk", 0, 0, 12f, Color.GRAY.getRGB())
                .endDraw();

        SimpleTextProducer.LineInfo lineInfo = ICS.instance().text().simulate("ABCabcijk", 0, 0, 12f);
        ICS.instance().text()
                .begin()
                .append("min xy max xy: " + lineInfo.getMinX() + ", " + lineInfo.getMinY() + ", " + lineInfo.getMaxX() + ", " + lineInfo.getMaxY(), 0, 14)
                .append("LineTopToBaseline: " + lineInfo.getLineTopToBaseline(), 0, 24)
                .endDraw();

        hud.drawRectOutline(
                lineInfo.getMinX(),
                lineInfo.getMinY(),
                lineInfo.getMaxX() - lineInfo.getMinX(),
                lineInfo.getMaxY() - lineInfo.getMinY(),
                0.5f,
                Color.GREEN.getRGB());
        hud.drawRect(
                lineInfo.getMinX(),
                lineInfo.getMinY() + lineInfo.getLineTopToBaseline(),
                lineInfo.getLineWidth() + 1f,
                0.5f,
                Color.RED.getRGB());
    }
}

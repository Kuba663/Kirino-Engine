package com.cleanroommc.test.kirino.gl.ksml;

import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.utils.KsmlcUtils;
import com.cleanroommc.ksmlc.KSMLCompiler;
import com.cleanroommc.ksmlc.SourceFile;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GLTestExtension.class)
public class KsmlcTest {

    private static final Logger LOGGER = LogManager.getLogger();

    private void testSubmitToGL(String shaderSource, ShaderType shaderType) {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            int shaderID = GL20.glCreateShader(shaderType.glValue);
            GL20.glShaderSource(shaderID, shaderSource);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);
        });
    }

    @Test
    public void testSimpleCompile() {
        SourceFile glsl = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std.glsl"));
        SourceFile ksml = KsmlcUtils.buildKsmlcSourceFile(new ResourceLocation(
                "forge:testdata/test_kirino_std.ksml"));

        KSMLCompiler compiler = new KSMLCompiler(glsl, new SourceFile[]{ksml}, null);

        String shaderSource = compiler.compile();

        LOGGER.info("debug:\n{}", shaderSource);

        testSubmitToGL(shaderSource, ShaderType.VERTEX);
    }
}

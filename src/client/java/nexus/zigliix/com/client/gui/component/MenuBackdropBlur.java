package nexus.zigliix.com.client.gui.component;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

public final class MenuBackdropBlur {
    private static final Identifier BACKDROP_BLUR_PIPELINE_ID = Identifier.fromNamespaceAndPath("nexus-client", "pipeline/menu_backdrop_blur");
    private static final Identifier BACKDROP_BLUR_FRAGMENT_ID = Identifier.fromNamespaceAndPath("nexus-client", "core/menu_backdrop_blur");
    private static final RenderPipeline BACKDROP_BLUR_PIPELINE = RenderPipeline.builder()
        .withLocation(BACKDROP_BLUR_PIPELINE_ID)
        .withVertexShader(Identifier.withDefaultNamespace("core/position_tex_color"))
        .withFragmentShader(BACKDROP_BLUR_FRAGMENT_ID)
        .withSampler("Sampler0")
        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
        .withUniform("Projection", UniformType.UNIFORM_BUFFER)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .build();

    private static final int QUARTER_RES_DIVISOR = 3;
    private static final long UPDATE_INTERVAL_NANOS = 33_000_000L;

    private static TextureTarget blurQuarterTarget;
    private static long lastBlurUpdateNanos;

    private MenuBackdropBlur() {}

    public static void ensureTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        var mainTarget = minecraft.getMainRenderTarget();
        markDirtyOnResize(blurQuarterTarget, scaledSize(mainTarget.width, QUARTER_RES_DIVISOR), scaledSize(mainTarget.height, QUARTER_RES_DIVISOR));
        blurQuarterTarget = resizeOrCreate(blurQuarterTarget, "nexus_menu_blur_quarter", scaledSize(mainTarget.width, QUARTER_RES_DIVISOR), scaledSize(mainTarget.height, QUARTER_RES_DIVISOR));
    }

    public static void captureMainTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        ensureTarget();
        if (blurQuarterTarget == null) {
            return;
        }

        long now = System.nanoTime();
        if (!shouldRefresh(now)) {
            return;
        }

        minecraft.getMainRenderTarget().blitAndBlendToTexture(blurQuarterTarget.getColorTextureView());
        lastBlurUpdateNanos = System.nanoTime();
    }

    public static boolean available() {
        return blurQuarterTarget != null;
    }

    public static boolean addBlurredRoundRect(GuiGraphicsExtractor g, int x, int y, int width, int height, int radius) {
        if (!available() || width <= 0 || height <= 0) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }

        GpuTextureView textureView = blurQuarterTarget.getColorTextureView();
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        TextureSetup textureSetup = TextureSetup.singleTexture(textureView, sampler);
        Matrix3x2f pose = new Matrix3x2f(g.pose());
        float u0 = x / (float) g.guiWidth();
        float u1 = (x + width) / (float) g.guiWidth();
        float v0 = y / (float) g.guiHeight();
        float v1 = (y + height) / (float) g.guiHeight();
        float radiusX = radius / (float) width;
        float radiusY = radius / (float) height;

        minecraft.gameRenderer.getGameRenderState().guiRenderState.addGuiElement(
            new BackdropBlurElement(textureSetup, pose, x, y, width, height, u0, u1, v0, v1, radiusX, radiusY)
        );
        return true;
    }

    private static boolean shouldRefresh(long now) {
        return lastBlurUpdateNanos == 0L || now - lastBlurUpdateNanos >= UPDATE_INTERVAL_NANOS;
    }

    private static int scaledSize(int size, int divisor) {
        return Math.max(1, size / divisor);
    }

    private static TextureTarget resizeOrCreate(TextureTarget target, String label, int width, int height) {
        if (target == null) {
            return new TextureTarget(label, width, height, false);
        }
        if (target.width != width || target.height != height) {
            target.resize(width, height);
            lastBlurUpdateNanos = 0L;
        }
        return target;
    }

    private static void markDirtyOnResize(TextureTarget target, int width, int height) {
        if (target == null || target.width != width || target.height != height) {
            lastBlurUpdateNanos = 0L;
        }
    }

    private static int packControlData(float localU, float localV, float radiusX, float radiusY) {
        int red = clampChannel(localU);
        int green = clampChannel(localV);
        int blue = clampChannel(radiusX);
        int alpha = clampChannel(radiusY);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int clampChannel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0f)));
    }

    private record BackdropBlurElement(
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x,
        int y,
        int width,
        int height,
        float u0,
        float u1,
        float v0,
        float v1,
        float radiusX,
        float radiusY
    ) implements GuiElementRenderState {
        @Override
        public void buildVertices(VertexConsumer consumer) {
            consumer.addVertexWith2DPose(pose, x, y)
                .setUv(u0, v0)
                .setColor(packControlData(0.0f, 0.0f, radiusX, radiusY));
            consumer.addVertexWith2DPose(pose, x, y + height)
                .setUv(u0, v1)
                .setColor(packControlData(0.0f, 1.0f, radiusX, radiusY));
            consumer.addVertexWith2DPose(pose, x + width, y + height)
                .setUv(u1, v1)
                .setColor(packControlData(1.0f, 1.0f, radiusX, radiusY));
            consumer.addVertexWith2DPose(pose, x + width, y)
                .setUv(u1, v0)
                .setColor(packControlData(1.0f, 0.0f, radiusX, radiusY));
        }

        @Override
        public RenderPipeline pipeline() {
            return BACKDROP_BLUR_PIPELINE;
        }

        @Override
        public ScreenRectangle scissorArea() {
            return null;
        }

        @Override
        public ScreenRectangle bounds() {
            return new ScreenRectangle(x, y, width, height).transformAxisAligned(pose);
        }
    }
}

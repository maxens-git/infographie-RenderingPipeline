package renderer.core.shader;

import java.awt.Color;

import renderer.algebra.MathUtils;
import renderer.controller.ImageWrapper;
import renderer.controller.Renderer;
import renderer.core.mesh.Texture;

/**
 * Simple shader that just copy the interpolated color to the screen,
 * taking the depth of the fragment into account.
 *
 * @author cdehais
 */
public class TextureShader extends Shader {

    /**
     * The start index of the texture attribute.
     */
    private static final int START_TEXTURE_ATTRIBUTE = 7;

    /**
     * The number of attribute about the texture.
     */
    private static final int NUMBER_TEXTURE_ATTRIBUTE = 2;

    /** The depth buffer. */
    private DepthBuffer depth;
    /** The texture to apply. */
    private Texture texture;
    /**
     * If we have to combine the texture color and
     * the original color of the fragment.
     */
    private boolean combineWithBaseColor;

    /**
     * Creates a PainterShader.
     */
    public TextureShader() {
        super();
        texture = null;
    }

    /**
     * Set the texture to use for shading.
     *
     * @param path the path to the texture image
     * @return whether the operation is a success
     */
    public boolean setTexture(String path) {
        try {
            texture = new Texture(path);
            return true;
        } catch (Exception e) {
            System.out.println("Could not load texture " + path);
            e.printStackTrace();
            texture = null;
            return false;
        }
    }

    /**
     * Set whether the texture should be combined with the base color.
     *
     * @param combineWithBaseColor true if the texture should be combined
     *                             with the base color
     */
    public void setCombineWithBaseColor(boolean combineWithBaseColor) {
        this.combineWithBaseColor = combineWithBaseColor;
    }

    /**
     * Shade the fragment, taking the depth of the fragment into account.
     *
     * @param fragment the fragment to shade
     */
    @Override
    public void shade(Fragment fragment) {
        if (!depth.testFragment(fragment)) {
            return;
        }
        // The Fragment may not have texture coordinates
        try {
            double u = fragment.getAttribute(7);
            double v = fragment.getAttribute(8);

            Color tt = texture.sample(u, v);

            if (this.combineWithBaseColor) {
                Color fc = fragment.getColor();
                int r = MathUtils.clamp((tt.getRed() * fc.getRed()) / 255, 0, 255);
                int g = MathUtils.clamp((tt.getGreen() * fc.getGreen()) / 255, 0, 255);
                int b = MathUtils.clamp((tt.getBlue() * fc.getBlue()) / 255, 0, 255);
                screen.setPixel(fragment.getX(), fragment.getY(), new Color(r, g, b));
            } else {
                screen.setPixel(fragment.getX(), fragment.getY(), tt);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            screen.setPixel(fragment.getX(), fragment.getY(), fragment.getColor());
        }
        depth.writeFragment(fragment);
    }

    /**
     * Reset the shader.
     */
    @Override
    public void reset() {
        depth.clear();
    }


    /**
     * Gets whether the color has to be combined with the base color.
     * @return whether the color has to be combined with the base color
     */
    public boolean getCombineWithBaseColor() {
        return combineWithBaseColor;
    }

    @Override
    public void init(final Renderer renderer, final ImageWrapper screen) {
        super.init(renderer, screen);
        if (depth == null) {
            depth = new DepthBuffer(screen.getWidth(), screen.getHeight());
        } else {
            depth.resize(screen.getWidth(), screen.getHeight());
        }
    }
}

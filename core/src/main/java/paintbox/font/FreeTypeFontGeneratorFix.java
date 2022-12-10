package paintbox.font;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Stroker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FreeTypeFontGeneratorFix extends FreeTypeFontGenerator {

    public FreeTypeFontGeneratorFix(FileHandle fontFile) {
        super(fontFile);
    }

    public FreeTypeFontGeneratorFix(FileHandle fontFile, int faceIndex) {
        super(fontFile, faceIndex);
    }

    @Override
    protected Glyph createGlyph(char c, FreeTypeBitmapFontData data, FreeTypeFontParameter parameter, Stroker stroker,
                                float baseLine, PixmapPacker packer) {
        // baseLine can differ due to floating point error even among glyphs. This is most common
        // when setting the default characters or fixed-width glyphs.
        // However, in super.createGlyph, baseLine is always cast to an int. This can cause
        // off-by-one issues (example: 26.0 vs 25.999998). Here, it is rounded to the nearest integer
        // since it will be used like an integer anyway in the super implementation.
        return super.createGlyph(c, data, parameter, stroker, (float) Math.round(baseLine), packer);
    }
}

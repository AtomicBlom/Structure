package mod.steamnsteel.world.structure.remnantruins;

import org.lwjgl.util.Point;
import org.lwjgl.util.ReadableRectangle;
import org.lwjgl.util.Rectangle;

public class Ruin
{
    public final RuinLevel ruinLevel;
    public final RuinSchematic schematic;
    public final Point location;
    public Integer height;

    public Ruin(RuinLevel ruinLevel, int ruinX, int ruinY, RuinSchematic ruinSchematic)
    {
        this.ruinLevel = ruinLevel;
        this.location = new Point(ruinX - ruinLevel.getMaxRuinSize().getWidth() / 2, ruinY - ruinLevel.getMaxRuinSize().getHeight() / 2);
        this.schematic = ruinSchematic;
    }

    public ReadableRectangle getBoundingRectangle()
    {
        return new Rectangle(
                location,
                ruinLevel.getMaxRuinSize()
        );
    }

    public boolean IntersectsChunk(ReadableRectangle chunkRect)
    {
        ReadableRectangle bounds = getBoundingRectangle();

        return !(chunkRect.getX() > (bounds.getX() + bounds.getWidth()) ||
                (chunkRect.getX() + chunkRect.getWidth()) < bounds.getX()||
                chunkRect.getY() > (bounds.getY() + bounds.getHeight()) ||
                (chunkRect.getY() + chunkRect.getHeight()) < bounds.getY()
        );
    }
}

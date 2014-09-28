package mod.steamnsteel.texturing;

import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class ProceduralConnectedTexture
{
    public static final int NO_FEATURE = -1;

    protected final int DEFAULT = 0;
    public static long TOP;
    public static long BOTTOM;
    public static long LEFT;
    public static long RIGHT;

    public static long FEATURE_EDGE_TOP;
    public static long FEATURE_EDGE_BOTTOM;
    public static long FEATURE_EDGE_LEFT;
    public static long FEATURE_EDGE_RIGHT;
    final int MISSING_TEXTURE = Integer.MAX_VALUE;
    private FeatureRegistry featureRegistry;

    //private HashMap<Integer, IProceduralWallFeature> featureRegistry;

    private ProceduralTextureRegistry textures;

    protected ProceduralConnectedTexture()
    {
        registerFeatures();
    }

    public void registerIcons(IIconRegister iconRegister)
    {
        registerFeatures();

        textures = new ProceduralTextureRegistry(iconRegister);
        registerIcons(textures);
    }

    private void registerFeatures() {
        featureRegistry = new FeatureRegistry();

        registerInternalFeatureProperties(featureRegistry);
        registerFeatures(featureRegistry);
    }

    private final void registerInternalFeatureProperties(IFeatureRegistry features)
    {
        TOP = features.registerFeatureProperty("T");
        LEFT = features.registerFeatureProperty("L");
        BOTTOM = features.registerFeatureProperty("B");
        RIGHT = features.registerFeatureProperty("R");

        FEATURE_EDGE_TOP = features.registerFeatureProperty("FE_T");
        FEATURE_EDGE_LEFT = features.registerFeatureProperty("FE_L");
        FEATURE_EDGE_BOTTOM = features.registerFeatureProperty("FE_B");
        FEATURE_EDGE_RIGHT = features.registerFeatureProperty("FE_R");
    }

    protected abstract void registerFeatures(IFeatureRegistry features);
    protected abstract void registerIcons(ITextureConditionSet textures);

    public IIcon getIconForSide(IBlockAccess blockAccess, WorldBlockCoord worldBlockCoord, int side)
    {
        TextureContext context = new TextureContext(blockAccess, worldBlockCoord, side);

        int blockProperties = getTexturePropertiesForSide2(context);

        IIcon icon = textures.getTextureFor(blockProperties);

        if (icon == null)
        {
            String blockPropertiesDescription = featureRegistry.describeSide(blockProperties);

            Logger.warning("Unknown texture: %d (%s) - %s @ (%s) - %d", blockProperties, Integer.toBinaryString(blockProperties), blockPropertiesDescription, worldBlockCoord, side);
        }
        return icon;
    }

    private int getTexturePropertiesForSide2(TextureContext context)
    {
        int blockProperties = 0;
        ForgeDirection orientation = context.getOrientation();
        if (orientation == ForgeDirection.UP || orientation == ForgeDirection.DOWN)
        {
            return DEFAULT;
        }

        boolean leftIsRuinWallAndNotObscured = isBlockPartOfWallAndUnobstructed(context, TextureDirection.LEFT);
        boolean rightIsRuinWallAndNotObscured = isBlockPartOfWallAndUnobstructed(context, TextureDirection.RIGHT);
        boolean aboveIsRuinWallAndNotObscured = isBlockPartOfWallAndUnobstructed(context, TextureDirection.ABOVE);
        boolean belowIsRuinWallAndNotObscured = isBlockPartOfWallAndUnobstructed(context, TextureDirection.BELOW);

        if (!aboveIsRuinWallAndNotObscured)
        {
            blockProperties |= TOP;
        }
        if (!belowIsRuinWallAndNotObscured)
        {
            blockProperties |= BOTTOM;
        }
        if (!leftIsRuinWallAndNotObscured)
        {
            blockProperties |= LEFT;
        }
        if (!rightIsRuinWallAndNotObscured)
        {
            blockProperties |= RIGHT;
        }

        blockProperties &= featureRegistry.getFeatureBits(context);

        /*
        IProceduralWallFeature feature = getValidFeature(blockAccess, worldBlockCoord, orientation);
        if (feature != null)
        {
            long subProperties = feature.getSubProperties(blockAccess, worldBlockCoord, orientation);

            if ((subProperties & featureRegistry.getFeatureMask()) != 0)
            {
                blockProperties |= subProperties;
            }
        }*/
        return blockProperties;
    }

    public final String describeTextureAt(IBlockAccess blockAccess, WorldBlockCoord worldBlockCoord, int side)
    {
        TextureContext context = new TextureContext(blockAccess, worldBlockCoord, side);

        int blockProperties = getTexturePropertiesForSide2(context);
        return featureRegistry.describeSide(blockProperties);

    }

    public final boolean isBlockPartOfWallAndUnobstructed(TextureContext context, TextureDirection direction)
    {
        if (!isCompatibleBlock(context, context.getNearbyBlock(direction)))
        {
            return false;
        }

        if (context.getNearbyBlock(TextureDirection.BACKWARDS).getMaterial().isOpaque())
        {
            return false;
        }
        return true;

    }

    protected abstract boolean isCompatibleBlock(TextureContext context, Block block);

    //protected abstract int getTexturePropertiesForSide(IBlockAccess blockAccess, WorldBlockCoord worldBlockCoord, int side);

    /*private List<FeatureInstance> getChunkFeatures(ChunkCoord chunkCoord)
    {
        List<FeatureInstance> featureRegistry = cachedFeatures.get(chunkCoord);
        if (featureRegistry != null)
        {
            return featureRegistry;
        }
        featureRegistry = new ArrayList<FeatureInstance>();

        for (IProceduralWallFeature wallFeature : this.featureRegistry.values())
        {

            for (FeatureInstance feature : wallFeature.getFeatureAreasFor(chunkCoord))
            {
                final IProceduralWallFeature ruinWallFeature = this.featureRegistry.get(feature.featureId);
                final WorldBlockCoord blockCoord = feature.getBlockCoord();
                boolean addFeature = true;
                for (FeatureInstance otherFeature : featureRegistry)
                {

                    final WorldBlockCoord otherFeatureBlockCoord = otherFeature.getBlockCoord();
                    if (blockCoord.getX() + feature.getWidth() >= otherFeatureBlockCoord.getX() &&
                            blockCoord.getX() <= otherFeatureBlockCoord.getX() + otherFeature.getWidth() &&
                            blockCoord.getY() + feature.getHeight() >= otherFeatureBlockCoord.getY() &&
                            blockCoord.getY() <= otherFeatureBlockCoord.getY() + otherFeature.getHeight() &&
                            blockCoord.getZ() + feature.getDepth() >= otherFeatureBlockCoord.getZ() &&
                            blockCoord.getZ() <= otherFeatureBlockCoord.getZ() + otherFeature.getDepth()
                            )
                    {
                        if (!feature.getFeature().canIntersect(otherFeature.getFeature()))
                        {
                            addFeature = false;
                            break;
                        }
                    }
                }
                if (addFeature)
                {
                    featureRegistry.add(feature);
                }
            }

            featureRegistry.addAll(wallFeature.getFeatureAreasFor(chunkCoord));
        }

        cachedFeatures.put(chunkCoord, featureRegistry);

        return featureRegistry;
    }*/

    public IProceduralWallFeature getValidFeature(IBlockAccess blockAccess, WorldBlockCoord worldBlockCoord, ForgeDirection orientation, int layer)
    {
        IProceduralWallFeature desiredFeature = featureRegistry.getFeatureAt(worldBlockCoord, layer);
        if (desiredFeature == null)
        {
            return null;
        }
        if (desiredFeature.isFeatureValid(blockAccess, worldBlockCoord, orientation))
        {
            return desiredFeature;
        }
        return null;
    }
}

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.block.StructureShapeBlock;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.foudroyantfactotum.tool.structure.registry.StructureTransformation;
import com.foudroyantfactotum.tool.structure.utility.StructureDefinitionBuilder;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("Duplicates")
public class SimpleStructureTests
{
	@Test
	public void LStructureAlongZRotatedNorth() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.NORTH, false);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 1));
		Assert.assertFalse(structureTransform.isBlockPresent(0,1, 1));
	}

	@Test
	public void LStructureAlongZRotatedEast() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.EAST, false);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(1,0, 0));
		Assert.assertFalse(structureTransform.isBlockPresent(1,1, 0));
	}

	@Test
	public void LStructureAlongZRotatedSouth() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.SOUTH, false);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,0, -1));
		Assert.assertFalse(structureTransform.isBlockPresent(0,1, -1));
	}

	@Test
	public void LStructureAlongZRotatedWest() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.WEST, false);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(-1,0, 0));
		Assert.assertFalse(structureTransform.isBlockPresent(-1,1, 0));
	}

	@Test
	public void LStructureAlongZRotatedNorthMirrored() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.NORTH, true);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,0, -1));
		Assert.assertFalse(structureTransform.isBlockPresent(0,1, -1));
	}

	@Test
	public void LStructureAlongZRotatedEastMirrored() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.EAST, true);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(-1,0, 0));
		Assert.assertFalse(structureTransform.isBlockPresent(-1,1, 0));
	}

	@Test
	public void LStructureAlongZRotatedSouthMirrored() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.SOUTH, true);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 1));
		Assert.assertFalse(structureTransform.isBlockPresent(0,1, 1));
	}

	@Test
	public void LStructureAlongZRotatedWestMirrored() {
		final StructureDefinition structureDefinition = getTestStructureDefinition();

		final StructureTransformation structureTransform = structureDefinition.getTransformsFor(EnumFacing.WEST, true);

		Assert.assertTrue(structureTransform.isBlockPresent(0,0, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(0,1, 0));
		Assert.assertTrue(structureTransform.isBlockPresent(1,0, 0));
		Assert.assertFalse(structureTransform.isBlockPresent(1,1, 0));
	}

	private static StructureDefinition getTestStructureDefinition()
	{
		final StructureDefinitionBuilder builder = new StructureDefinitionBuilder();

		final IBlockState dummyBlockStateA = Mockito.mock(IBlockState.class);
		final IBlockState dummyBlockStateB = Mockito.mock(IBlockState.class);
		final StructureBlock masterBlock = Mockito.mock(StructureBlock.class);
		final StructureShapeBlock slaveBlock = Mockito.mock(StructureShapeBlock.class);

		builder.setMasterBlock(masterBlock);
		builder.setShapeBlock(slaveBlock);

		builder.assignConstructionDefWithStates(ImmutableMap.of(
				'a', dummyBlockStateA,
				'b', dummyBlockStateB
		));

		builder.assignConstructionBlocks(
				new String[] {"a", "a"},
				new String[] {"b", "-"}
		);

		builder.assignToolFormPosition(BlockPos.ORIGIN);

		builder.setConfiguration(BlockPos.ORIGIN,
				new String[] {"M", "-"},
				new String[] {"-", " "}
		);

		return builder.build();
	}
}

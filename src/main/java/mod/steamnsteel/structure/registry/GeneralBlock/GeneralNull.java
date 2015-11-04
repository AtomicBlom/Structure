/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package mod.steamnsteel.structure.registry.GeneralBlock;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class GeneralNull extends Block implements IGeneralBlock
{
    public GeneralNull()
    {
        super(Material.air);
    }

    @Override
    public boolean canBlockBeUsed(Block b, int meta, TripleCoord pos)
    {
        return true;
    }

    @Override
    public void resetGeneralBlock()
    {
        //noop
    }
}

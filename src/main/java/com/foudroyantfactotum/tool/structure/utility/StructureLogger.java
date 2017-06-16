/*
 * Copyright (c) 2016 Foudroyant Factotum
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
package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.StructureException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class StructureLogger
{
    private static org.apache.logging.log4j.Logger logger;

    public static void info(final String format, final Object... args)
    {
        if (logger == null) {
            throw new StructureException("Structure attempted to log before the Mod ID has been set.");
        }
        logger.log(Level.INFO, format, args);
    }

    public static void setModId(String modId)
    {
        logger = LogManager.getLogger(modId + ".Structure");
    }
}
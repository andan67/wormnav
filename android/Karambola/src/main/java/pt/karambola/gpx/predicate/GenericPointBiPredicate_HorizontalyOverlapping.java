/*
 * GenericPointBiPredicate_HorizontalyOverlapping.java
 *
 * Copyright (c) 2016 Karambola. All rights reserved.
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3.0
 * of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file; if not, write to the
 * Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */


package pt.karambola.gpx.predicate;

import pt.karambola.commons.collections.BiPredicate;
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.util.GpxUtils;

public class
GenericPointBiPredicate_HorizontalyOverlapping
	implements BiPredicate<GenericPoint,GenericPoint>
{
	private final double proximityToleranceMtrs ;

	public 
	GenericPointBiPredicate_HorizontalyOverlapping( final double proximityToleranceMtrs )
	{
		this.proximityToleranceMtrs = proximityToleranceMtrs ;
	}

	@Override
	public boolean
	test( final GenericPoint gpt1, final GenericPoint gpt2 )
	{
		return gpt1 != gpt2
			&& GpxUtils.horizontalDistance( gpt1, gpt2 ) < this.proximityToleranceMtrs
			;
	}
}

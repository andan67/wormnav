/*
 * GenericPointPredicate_HorizontalDistanceRange.java
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

import pt.karambola.commons.collections.Predicate;
import pt.karambola.geo.Geo;
import pt.karambola.gpx.beans.GenericPoint;

public class
GenericPointPredicate_HorizontalDistanceRange
	implements Predicate<GenericPoint>
{
	private final double	refLat ;
	private final double	refLon ;
	private final Double	distanceMin ;
	private final Double	distanceMax ;

	public
	GenericPointPredicate_HorizontalDistanceRange( final double refLat, final double refLon, final Double distanceMin, final Double distanceMax )
	{
		this.refLat 		= refLat ;
		this.refLon 		= refLon ;
		this.distanceMin 	= distanceMin ;
		this.distanceMax 	= distanceMax ;
	}

	@Override
	public
	boolean
	evaluate( final GenericPoint gpt )
	{
		if (this.distanceMin != null  ||  this.distanceMax != null)
		{
			final double distance = Geo.horizontalDistance( gpt.getLatitude( ), gpt.getLongitude( ), refLat, refLon ) ;

			if (this.distanceMin != null  &&  distance < this.distanceMin)
				return false ;

			if (this.distanceMax != null  &&  distance > this.distanceMax)
				return false ;
		}

		return true ;
	}
}

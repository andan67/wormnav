/*
 * GenericPointPredicate_DistanceRange.java
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
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.util.GpxUtils;

public class
GenericPointPredicate_DistanceRange
	implements Predicate<GenericPoint>
{
	private final Point		ref	= new Point( ) ;
	private final Double	distanceMin ;
	private final Double	distanceMax ;

	public
	GenericPointPredicate_DistanceRange( final double refLat, final double refLon, final double refEle, final Double distanceMin, final Double distanceMax )
	{
		ref.setLatitude( refLat ) ;
		ref.setLongitude( refLon ) ;
		ref.setElevation( refEle ) ;
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
			final double distance = GpxUtils.distance( gpt, ref ) ;

			if (this.distanceMin != null  &&  distance < this.distanceMin)  return false ;
			if (this.distanceMax != null  &&  distance > this.distanceMax)  return false ;
		}

		return true ;
	}
}

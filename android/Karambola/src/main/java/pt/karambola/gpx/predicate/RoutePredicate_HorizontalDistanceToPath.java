/*
 * RoutePredicate_HorizontalDistanceToPath.java
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

import pt.karambola.R3.R3;
import pt.karambola.commons.collections.Predicate;
import pt.karambola.geo.Geo;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.util.GpxUtils;

public class
RoutePredicate_HorizontalDistanceToPath
	implements Predicate<Route>
{
	private final R3  		ref ;
	private final Double	distanceMin ;
	private final Double	distanceMax ;

	public
	RoutePredicate_HorizontalDistanceToPath( final double refLat, final double refLon, final Double distanceMin, final Double distanceMax )
	{
		ref  = Geo.horizontalCartesian( refLat, refLon ) ;
		this.distanceMin = distanceMin ;
		this.distanceMax = distanceMax ;
	}

	public
	boolean
	evaluate( Route rte )
	{
		if (this.distanceMin != null  ||  this.distanceMax != null)
		{
			final double distance = GpxUtils.horizontalDistanceToPath( ref, rte.getRoutePoints( ) )[0] ;
			
			if (this.distanceMin != null  &&  distance < this.distanceMin)
				return false ;

			if (this.distanceMax != null  &&  distance > this.distanceMax)
				return false ;
		}

		return true ;
	}
}

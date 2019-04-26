/*
 * RouteComparator_HorizontalDistanceToPath.java
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


package pt.karambola.gpx.comparator;

import java.util.Comparator;

import pt.karambola.R3.R3;
import pt.karambola.geo.Geo;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.util.GpxUtils;

public class
RouteComparator_HorizontalDistanceToPath
	implements Comparator<Route>
{
	private final R3  ref ;

	public
	RouteComparator_HorizontalDistanceToPath( double refLat, double refLon )
	{
		ref  = Geo.horizontalCartesian( refLat, refLon ) ;
	}


	@Override
	public int
	compare( final Route r1, final Route r2 )
	{
		if (r1 == r2)	return  0 ;
		if (r1 == null)	return  1 ;
		if (r2 == null) return -1 ;

		final double[] 	distanceResults1 = GpxUtils.horizontalDistanceToPath( ref, r1.getRoutePoints( ) ) ;
		final double 	d1 				 = distanceResults1[0] ;

		final double[] 	distanceResults2 = GpxUtils.horizontalDistanceToPath( ref, r2.getRoutePoints( ) ) ;
		final double 	d2 				 = distanceResults2[0] ;

		return Double.compare( d1, d2 ) ;
	}
}

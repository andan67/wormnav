/*
 * GenericPointComparator_Distance.java
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

import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.util.GpxUtils;

public class
GenericPointComparator_Distance
	implements Comparator<GenericPoint>
{
	private final 	Point 	ref = new Point() ;

	public
	GenericPointComparator_Distance( final double refLat, final double refLon, final double refEle )
	{
		ref.setLatitude( refLat ) ;
		ref.setLongitude( refLon ) ;
		ref.setElevation( refEle ) ;
	}


	/**
	 * Compare the geodesic distance (to a reference point) of two geographic points.
	 *
	 * @param p1 	The first Point point to compare
	 * @param p2 	The second Point point to compare
	 * @return 		a negative value if the first point distance (to the reference point)
	 *              is less than the second point distance (to the reference point), zero if the same and a positive value if greater.
	 */
	@Override
	public int
	compare( GenericPoint p1, GenericPoint p2 )
	{
		if (p1 == p2)	return  0 ;
		if (p1 == null)	return  1 ;
		if (p2 == null) return -1 ;

		final double d1 = GpxUtils.distance( ref, p1 ) ;
		final double d2 = GpxUtils.distance( ref, p2 ) ;

		return Double.compare( d1, d2 ) ;
	}
}

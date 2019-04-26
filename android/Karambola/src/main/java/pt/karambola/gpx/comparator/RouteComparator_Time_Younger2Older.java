/*
 * RouteComparator_Time_Younger2Older.java
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

import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;

public class
RouteComparator_Time_Younger2Older
	implements Comparator<Route>
{
	public
	int
	compare( Route r1, Route r2 )
	{
		if (r1 == r2)	return  0 ;
		if (r1 == null)	return  1 ;
		if (r2 == null) return -1 ;

		RoutePoint start1 = r1.getRoutePoints().get(0) ;
		RoutePoint start2 = r2.getRoutePoints().get(0) ;

		return GenericPointComparator.TIME_YOUNGER2OLDER.compare( start1, start2 ) ;
	}
}

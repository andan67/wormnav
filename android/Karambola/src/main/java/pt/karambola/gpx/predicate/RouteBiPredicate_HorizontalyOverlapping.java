/*
 * RouteBiPredicate_Overlapping.java
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

import java.util.List;

import pt.karambola.commons.collections.BiPredicate;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;

public class
RouteBiPredicate_HorizontalyOverlapping
implements BiPredicate<Route,Route>
{
	@Override
	public boolean
	test( Route rte1, Route rte2 )
	{
		if (rte1 == rte2)
			return false ;

		List<RoutePoint> rte1Pts = rte1.getRoutePoints() ;
		List<RoutePoint> rte2Pts = rte2.getRoutePoints() ;
		int rte1PtsSize = rte1Pts.size() ;
		int rte2PtsSize = rte2Pts.size() ;

		if (rte1PtsSize != rte2PtsSize)
			return false ;

		for (int rtePtIdx = 0  ;  rtePtIdx < rte1PtsSize  ;  ++rtePtIdx)
			if (!GenericPointConflict.HORIZONTALYOVERLAPPING.test( rte1Pts.get(rtePtIdx), rte2Pts.get(rtePtIdx) ))
				return false ;

		return true ;
	}
}

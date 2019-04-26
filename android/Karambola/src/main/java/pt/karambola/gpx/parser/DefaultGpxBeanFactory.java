/*
 * DefaultGpxBeanFactory.java
 * 
 * Copyright (c) 2016, Karambola. All rights reserved.
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

package pt.karambola.gpx.parser;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.beans.TrackSegment;


public class
DefaultGpxBeanFactory
	implements GpxBeanFactory
{
	public static final DefaultGpxBeanFactory INSTANCE = new DefaultGpxBeanFactory( ) ;

	@Override
	public
	Gpx
	newGpx( )
	{
		return new Gpx( ) ;
	}

	@Override
	public
	Point
	newPoint( )
	{
		return new Point( ) ;
	}

	@Override
	public
	Route
	newRoute( )
	{
		return new Route( ) ;
	}

	@Override
	public
	RoutePoint
	newRoutePoint( )
	{
		return new RoutePoint( ) ;
	}

	@Override
	public
	Track
	newTrack( )
	{
		return new Track( ) ;
	}

	@Override
	public
	TrackSegment
	newTrackSegment( )
	{
		return new TrackSegment( ) ;
	}

	@Override
	public
	TrackPoint
	newTrackPoint( )
	{
		return new TrackPoint( ) ;
	}
}

/*
 * Geo.java
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


package pt.karambola.geo;

import pt.karambola.R3.R3;

public class
Geo
{
	public final static double EARTHMEANRADIUS_MTR = 6371000.0;

	/**
	 * Calculate the initial bearing (forward azimuth) between two geographic locations.
	 *
	 * @param lat1deg latitude  of geographic point 1 (degrees)
	 * @param lon1deg longitude of geographic point 1 (degrees)
	 * @param lat2deg latitude  of geographic point 2 (degrees)
	 * @param lon2deg longitude of geographic point 2 (degrees)
	 * @return the initial bearing (degrees clockwise from true North), at the first point, of the line that joins the 2 points.
	 * @author Afonso Santos
	 */
	public static
    double
    initialBearing( final double lat1deg        // latitude  of geographic point 1 (degrees)
                  , final double lon1deg        // longitude of geographic point 1 (degrees)
                  , final double lat2deg        // latitude  of geographic point 2 (degrees)
                  , final double lon2deg        // longitude of geographic point 2 (degrees)
				  )
	{
		final double lat1rad = Math.toRadians( lat1deg );
		final double lat2rad = Math.toRadians( lat2deg );

		final double deltaLonRad = Math.toRadians( lon2deg - lon1deg );
		final double cosLat2     = Math.cos( lat2rad );
		final double y           = Math.sin( deltaLonRad ) * cosLat2;
		final double x           = Math.cos( lat1rad ) * Math.sin( lat2rad ) - Math.sin( lat1rad ) * cosLat2 * Math.cos( deltaLonRad );

		double initialBearingDeg = Math.toDegrees( Math.atan2( y, x ) );

		if (initialBearingDeg < 0.0)
			initialBearingDeg += 360.0;    // Normalize to [0-360] range.

		return initialBearingDeg;
	}


	/**
	 * Calculate horizontal (earth surface) distance between two geographic locations.
	 *
	 * @param lat1deg latitude of geographic point 1 (degrees)
	 * @param lon1deg longitude of geographic point 1 (degrees)
	 * @param lat2deg latitude of geographic point 2 (degrees)
	 * @param lon2deg longitude of geographic point 2 (degrees)
	 * @return distance between the points (meters)
	 * @author Afonso Santos
	 */
	public static
    double
    horizontalDistance( final double lat1deg        // latitude  of geographic point 1 (degrees)
                      , final double lon1deg        // longitude of geographic point 1 (degrees)
                      , final double lat2deg        // latitude  of geographic point 2 (degrees)
                      , final double lon2deg        // longitude of geographic point 2 (degrees)
					  )
	{
		if (lat1deg == lat2deg && lon1deg == lon2deg)
			return 0.0;

		final double lat1rad = Math.toRadians( lat1deg );
		final double lat2rad = Math.toRadians( lat2deg );
		final double cos12   = Math.sin( lat1rad ) * Math.sin( lat2rad ) + Math.cos( lat1rad ) * Math.cos( lat2rad ) * Math.cos( Math.toRadians( lon1deg - lon2deg ) );
		final double dist    = EARTHMEANRADIUS_MTR * Math.acos( cos12 );            // Distance in meters.

		return dist;
	}


	/**
	 * Calculate geodesic (considering elevation) distance between two points.
	 *
	 * @param lat1deg latitude of geographic point 1 (degrees)
	 * @param lon1deg longitude of geographic point 1 (degrees)
	 * @param ele1mtr elevation of geographic point 1 (meters)
	 * @param lat2deg latitude of geographic point 2 (degrees)
	 * @param lon2deg longitude of geographic point 2 (degrees)
	 * @param ele2mtr elevation of geographic point 2 (meters)
	 * @return distance between the points (meters)
	 * @author Afonso Santos
	 */
	public static
    double
    distance( final double lat1deg        // latitude of geographic point 1 (degrees)
			, final double lon1deg        // longitude of geographic point 1 (degrees)
			, final double ele1mtr        // elevation of geographic point 1 (meters)
			, final double lat2deg        // latitude of geographic point 2 (degrees)
			, final double lon2deg        // longitude of geographic point 2 (degrees)
			, final double ele2mtr        // elevation of geographic point 2 (meters)
			)
	{
		return Math.hypot( horizontalDistance( lat1deg, lon1deg, lat2deg, lon2deg ), ele1mtr - ele2mtr );
	}


	/**
	 * Calculate unit length 3D vector (from center of earth) towards a geographic location.
	 *
	 * @param latDeg latitude (degrees)
	 * @param lonDeg longitude (degrees)
	 * @return unit length 3D vector
	 * @author Afonso Santos
	 */
	public static
    R3
    versor(final double latDeg, final double lonDeg)
	{
		// Convert degrees coordinates into trigonometric friendly radians.
		final double latRad = Math.toRadians( latDeg );
		final double lonRad = Math.toRadians( lonDeg );

		final double cosLat = Math.cos( latRad );

		return new R3( cosLat * Math.sin( lonRad )        // x
                     , cosLat * Math.cos( lonRad )        // y
                     , Math.sin( latRad )                 // z
                     );
	}


	/**
	 * Calculate 3D vector from center of earth to a point.
	 *
	 * @param latDeg latitude (degrees)
	 * @param lonDeg longitude (degrees)
	 * @param eleMtr elevation (meters)
	 * @return 3D cartesian vector.
	 * @author Afonso Santos
	 */
	public static
    R3
    cartesian(final double latDeg, final double lonDeg, final double eleMtr)
	{
		return R3.scalar( EARTHMEANRADIUS_MTR + eleMtr, versor( latDeg, lonDeg ) );
	}


	/**
	 * Calculate 3D vector from center of earth to a point's projection on earth horizontal (mean Earth radius) plane.
	 *
	 * @param latDeg latitude (degrees)
	 * @param lonDeg longitude (degrees)
	 * @return 3D cartesian vector.
	 * @author Afonso Santos
	 */
	public static
	R3
	horizontalCartesian( final double latDeg, final double lonDeg )
	{
		return cartesian( latDeg, lonDeg, 0.0 );
	}


	public static
	double
	turningAngle( final double bearingDeg )
	{
		double turningAngleDeg = bearingDeg ;

		while (turningAngleDeg > 180.0)
			turningAngleDeg -= 360.0 ;        // force the turningAngleDeg value into the range [-180, +180]

		while (turningAngleDeg < -180.0)
			turningAngleDeg += 360.0 ;        // force the turningAngleDeg value into the range [-180, +180]

		return turningAngleDeg ;
	}
}
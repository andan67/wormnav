/*
 * GpxRouteUtils.java
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

package pt.karambola.gpx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.karambola.R3.R3;
import pt.karambola.R3.util.PathSimplifier;
import pt.karambola.R3.util.PathSimplifierResult;
import pt.karambola.geo.Geo;
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;

public class
GpxRouteUtils
{
	final public Route         route ;
	public    int              nPoints ;
	public    int              nSegments ;
	public    int              lastPointIdx ;
	public    List<RoutePoint> routePoints	;

	private   R3[]             routePoints3D ;
	private   R3[]             routeSegments3D ;
	private   double[]         routeSegments3Dmodulus ;
	
	private   R3[]             routePoints2D ;
	private   R3[]             routeSegments2D ;
	private   double[]         routeSegments2Dmodulus ;

	private   double[]         lengthOfRouteSegments ;
	private   double[]         lengthFromFirstPoint ;
	private   double[]         lengthToLastPoint ;

    private   double[]         horizontalLengthOfRouteSegments ;
    private   double[]         horizontalLengthFromFirstPoint ;
	private   double[]         horizontalLengthToLastPoint ;

	private   double[]         initialBearingOfRouteSegments ;
	private   double[]         turningAngleAtPoint ;

	final private Map<String,List<RoutePoint>>	cachedSimplifiedRoutePoints	= new HashMap<>() ;
	final private Map<String,Double>			cachedSimplifiedRouteErrors	= new HashMap<>() ;

    private	Boolean          is2D ;		// route is flat ?

    public
	GpxRouteUtils( Route route )
	{
		this.route = route ;
		refreshCaches( ) ;
	}


    protected
    void
	refreshCaches( )
	{
		routePoints	         		= new ArrayList<>( route.getRoutePoints() ) ;
		routePoints3D	     		= GpxUtils.cartesianPath( routePoints ) ;
		routePoints2D	     		= GpxUtils.horizontalCartesianPath( routePoints ) ;
		nPoints          	 		= routePoints.size() ;
		lastPointIdx = nSegments    = nPoints - 1 ;
		routeSegments3D       		= new R3[nSegments] ;
		routeSegments2D       		= new R3[nSegments] ;
		routeSegments3Dmodulus 		= new double[nSegments] ;
		routeSegments2Dmodulus 		= new double[nSegments] ;

		lengthOfRouteSegments 			= new double[nSegments] ;
		lengthFromFirstPoint    		= new double[nPoints] ;
		lengthToLastPoint               = new double[nPoints] ;

		horizontalLengthOfRouteSegments	= new double[nSegments] ;
		horizontalLengthFromFirstPoint	= new double[nPoints] ;
		horizontalLengthToLastPoint		= new double[nPoints] ;

		initialBearingOfRouteSegments	= new double[nSegments] ;
        turningAngleAtPoint             = new double[nPoints] ;

		cachedSimplifiedRoutePoints.clear( ) ;
		cachedSimplifiedRouteErrors.clear( ) ;

        is2D = null ;																	// To be computed on demand.

		for (int segmentIdx = 0  ;  segmentIdx < nSegments  ;  ++segmentIdx )
		{
			RoutePoint segmentFrom = routePoints.get( segmentIdx ) ;
			RoutePoint segmentTo   = routePoints.get( segmentIdx+1 ) ;

			// The geodesic length of the segment.
			lengthOfRouteSegments[segmentIdx] = GpxUtils.distance( segmentFrom, segmentTo ) ;

			// The horizontal length of the segment.
			horizontalLengthOfRouteSegments[segmentIdx] = GpxUtils.horizontalDistance( segmentFrom, segmentTo ) ;

			// The initial bearing of the segment.
			initialBearingOfRouteSegments[segmentIdx] = GpxUtils.initialBearing( segmentFrom, segmentTo ) ;

			// 3D vector from start point to end point of segment.
			routeSegments3D[segmentIdx] = R3.sub( routePoints3D[segmentIdx+1], routePoints3D[segmentIdx] ) ;

			// 2D vector from start point to end point of segment.
			routeSegments2D[segmentIdx] = R3.sub( routePoints2D[segmentIdx+1], routePoints2D[segmentIdx] ) ;

			// Pre-calculate the modulus of the 3D segments.
			routeSegments3Dmodulus[segmentIdx] = R3.modulus( routeSegments3D[segmentIdx] ) ;

			// Pre-calculate the modulus of the 2D segments.
			routeSegments2Dmodulus[segmentIdx] = R3.modulus( routeSegments2D[segmentIdx] ) ;
		}

		// Pre-calculate the lengths from first route point.
		lengthFromFirstPoint[0] = 0.0 ;

		for (int pointIdx = 1  ;  pointIdx <= lastPointIdx  ;  ++pointIdx )
			lengthFromFirstPoint[pointIdx] = lengthFromFirstPoint[pointIdx-1] + lengthOfRouteSegments[pointIdx-1] ;

		// Pre-calculate the horizontal lengths from first route point.
		horizontalLengthFromFirstPoint[0] = 0.0 ;

		for (int pointIdx = 1  ;  pointIdx <= lastPointIdx  ;  ++pointIdx )
			horizontalLengthFromFirstPoint[pointIdx] = horizontalLengthFromFirstPoint[pointIdx-1] + horizontalLengthOfRouteSegments[pointIdx-1] ;

		// Pre-calculate the lengths to last route point.
		lengthToLastPoint[lastPointIdx] = 0.0 ;

		for (int pointIdx = lastPointIdx-1  ;  pointIdx >= 0  ;  --pointIdx )
			lengthToLastPoint[pointIdx] = lengthToLastPoint[pointIdx+1] + lengthOfRouteSegments[pointIdx] ;

		// Pre-calculate the horizontal lengths to last route point.
		horizontalLengthToLastPoint[lastPointIdx] = 0.0 ;

		for (int pointIdx = lastPointIdx-1  ;  pointIdx >= 0  ;  --pointIdx )
			horizontalLengthToLastPoint[pointIdx] = horizontalLengthToLastPoint[pointIdx+1] + horizontalLengthOfRouteSegments[pointIdx] ;

        // Pre-calculate the turning angles at the points.
        turningAngleAtPoint[0] = turningAngleAtPoint[lastPointIdx] = 0.0 ;  // No turning at route's tips.

        for (int pointIdx = 1  ;  pointIdx < lastPointIdx  ;  ++pointIdx )
            turningAngleAtPoint[pointIdx] = Geo.turningAngle( initialBearingOfRouteSegments[pointIdx] - initialBearingOfRouteSegments[pointIdx-1] ) ;
	}


	/**
	 * Simplifies the route given <b>on instantiation</b> with the given simplification parameters.
	 *
	 * @param maxSegments 	Maximum number of segments allowed in the simplified route.
	 * @param accuracyMtr 	Maximum tolerated error of the simplified route, provided <b>maxSegments</b> is not exceeded.
	 *
	 * @return 				The error of the simplified route.
	 *
	 * @author 				Afonso Santos
	 */
	public
	double
	simplify( int maxSegments, double accuracyMtr )
	{
		if (accuracyMtr < 0.0)
			accuracyMtr = 0.0 ;

		if (maxSegments > nSegments)
			maxSegments = nSegments ;

		final String requestKey = maxSegments + "|" + accuracyMtr ;

		List<RoutePoint> simplifiedRoutePoints  = null ;
		Double 			 simplifiedRouteError	= null ;

		if ((simplifiedRoutePoints = cachedSimplifiedRoutePoints.get( requestKey )) == null)
		{
			PathSimplifierResult pathSimplifierResult = PathSimplifier.simplifySection( routePoints3D, 0, lastPointIdx, maxSegments, accuracyMtr ) ;

			simplifiedRoutePoints = new ArrayList<>( ) ;

			for (final int pointIdx: pathSimplifierResult.pointsIdxs)
				simplifiedRoutePoints.add( routePoints.get( pointIdx ) ) ;

			// Remember these results to avoid recalculating them if asked again.
			cachedSimplifiedRoutePoints.put( requestKey, simplifiedRoutePoints ) ;
			cachedSimplifiedRouteErrors.put( requestKey, simplifiedRouteError = pathSimplifierResult.error ) ;
		}
		else
			simplifiedRouteError = cachedSimplifiedRouteErrors.get( requestKey ) ;

		route.setRoutePoints( simplifiedRoutePoints ) ;

		return simplifiedRouteError ;
	}


	/**
	 * Calculates the Cartesian distance from a point to this route's path line.
	 * If the route's path points lack elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param v 	the point from which the distance is measured
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public
	double[]
	distance( final R3 v )
	{
		return is2D( )																			// Route has no 3D elevation info on it's route points ?
			 ? R3.distanceToPath( v, routePoints2D, routeSegments2D, routeSegments2Dmodulus )	// Flattened Earth shadow 2D projections of segments used.
			 : R3.distanceToPath( v, routePoints3D, routeSegments3D, routeSegments3Dmodulus )	// Fully 3D spatial segment vectors used.
			 ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to the Earth surface projection of this route's path line.
	 *
	 * @param v 	the point from which the distance is measured
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public
	double[]
	horizontalDistance( final R3 v )
	{
		return R3.distanceToPath( v, routePoints2D, routeSegments2D, routeSegments2Dmodulus ) ;	// Flattened Earth shadow 2D projections of segments used.
	}


    public
    boolean
    is2D( )
    {
        if (is2D == null)
        	is2D = GpxUtils.isPath2D( routePoints ) ;

        return is2D ;
    }


	/**
	 * Calculates the Cartesian distance from a point to this route's path line.
	 * If the route's path points lack elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param refLat 	the latitude  of the point from which the distance is measured
	 * @param refLon 	the longitude of the point from which the distance is measured
	 * @param refEle 	the elevation of the point from which the distance is measured
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public
	double[]
	distance( final double refLat, final double refLon, final double refEle )
	{
		return is2D( )													// Route has no 3D elevation info on it's route points ?
			 ? horizontalDistance( refLat, refLon )						// Flattened Earth shadow 2D projections of segments used.
			 : distance( Geo.cartesian( refLat, refLon, refEle ) ) ;	// Fully 3D spatial segment vectors used.
	}
	
	
	/**
	 * Calculates the horizontal (map projection) distance from a point to the Earth surface projection of this route's path line.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured
	 * @param refLon 	the longitude of the point from which the distance is measured
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public
	double[]
	horizontalDistance( final double refLat, final double refLon )
	{
		return horizontalDistance( Geo.horizontalCartesian( refLat, refLon ) ) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to the Earth surface projection of this route's path line.
	 *
	 * @param p 	the point from which the distance is measured.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the (integer part is the) index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public  <P extends GenericPoint>
	double[]
	horizontalDistance( final P p )
	{
		return horizontalDistance( p.getLatitude(), p.getLongitude() ) ;
	}


	/**
	 * Calculates the Geodesic distance from a point to this route's path line.
	 * If either the point or the route's points lacks elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param p 	the point from which the distance is measured.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the (integer part is the) index of the route segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest route segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public  <P extends GenericPoint>
	double[]
	distance( final P p )
	{
		return p.getElevation() == null  ||  is2D( )
			 ? horizontalDistance( p.getLatitude(), p.getLongitude() )
			 : distance( p.getLatitude(), p.getLongitude(), p.getElevation() )
			 ;
	}


	/**
	 * Calculates the horizontal (map projection) length of the 3D line that joins this route's waypoints in sequence.
	 *
	 * @return 		this route's path horizontal length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	horizontalLength( )
	{
		return horizontalLengthToLastPoint[0] ;
	}


	/**
	 * Calculates the horizontal (map projection) length of the route's path from its first point.
	 *
	 * @param pointIdx 	the index of the point up to which length is calculated.
	 *
	 * @return 		path's initial section horizontal length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	horizontalLengthFromFirstPoint( final int pointIdx )
	{
		return horizontalLengthFromFirstPoint[pointIdx] ;
	}


	/**
	 * Calculates the horizontal (map projection) length of a path section.
	 *
	 * @param firstSectionBoundaryPointIdx      the point index of the first path section boundary.
	 * @param secondSectionBoundaryPointIdx 	the point index of the second path section boundary.
	 *
	 * @return 		path's section horizontal length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	horizontalLengthOfSection( int firstSectionBoundaryPointIdx
			                 , int secondSectionBoundaryPointIdx
							 )
	{
		switch( Integer.signum( secondSectionBoundaryPointIdx - firstSectionBoundaryPointIdx ) )
		{
			case 0 :	// <a> and <b> are the same point.
				return 0.0 ;

			case -1 :	// must switch aPointIdx <-> bPointIdx.
				final int tmpPointIdx = firstSectionBoundaryPointIdx ;
				firstSectionBoundaryPointIdx = secondSectionBoundaryPointIdx ;
				secondSectionBoundaryPointIdx = tmpPointIdx ;

			case 1 :	// normal case.
			default:
				return horizontalLengthFromFirstPoint[secondSectionBoundaryPointIdx] - horizontalLengthFromFirstPoint[firstSectionBoundaryPointIdx] ;
		}
	}


	/**
	 * Calculates the horizontal (map projection) length of a segment.
	 *
	 * @param	segmentIdx 	the index of the path segment.
	 *
	 * @return 	path segment's horizontal length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	horizontalLengthOfSegment( final int segmentIdx )
	{
		return horizontalLengthOfRouteSegments[segmentIdx] ;
	}


	/**
	 * Calculates the horizontal (map projection) length of the route's path till its last point.
	 *
	 * @param pointIdx 	the index of the point from which length is calculated.
	 *
	 * @return 		path's final section horizontal length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	horizontalLengthToLastPoint( final int pointIdx )
	{
		return horizontalLengthToLastPoint[pointIdx] ;
	}


	/**
	 * Calculates the geodesic length of a segment.
	 *
	 * @param	segmentIdx 	the index of the path segment.
	 *
	 * @return 	path segment's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	lengthOfSegment( final int segmentIdx )
	{
		return lengthOfRouteSegments[segmentIdx] ;
	}


	/**
	 * Calculates the geodesic length of the route's path from its first point.
	 *
	 * @param pointIdx 	the index of the point up to which length is calculated.
	 *
	 * @return 		path's initial section geodesic length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	lengthFromFirstPoint( final int pointIdx )
	{
		return lengthFromFirstPoint[pointIdx] ;
	}


	/**
	 * Calculates the geodesic length of the route's path till its last point.
	 *
	 * @param pointIdx 	the index of the point from which length is calculated.
	 *
	 * @return 		path's final section geodesic length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	lengthToLastPoint( final int pointIdx )
	{
		return lengthToLastPoint[pointIdx] ;
	}


	/**
	 * Calculates the geodesic length of a path section.
	 *
     * @param firstSectionBoundaryPointIdx      the point index of the first path section boundary.
     * @param secondSectionBoundaryPointIdx 	the point index of the second path section boundary.
	 *
	 * @return 		path's section geodesic length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	lengthOfSection( final int firstSectionBoundaryPointIdx
                   , final int secondSectionBoundaryPointIdx
                   )
	{
        int fromPointIdx ;
        int toPointIdx ;

        if (firstSectionBoundaryPointIdx < secondSectionBoundaryPointIdx)
        {
            fromPointIdx = firstSectionBoundaryPointIdx ;
            toPointIdx   = secondSectionBoundaryPointIdx ;
        }
        else
        {
            fromPointIdx = secondSectionBoundaryPointIdx ;
            toPointIdx   = firstSectionBoundaryPointIdx ;
        }

        if (fromPointIdx < 0)
            fromPointIdx = 0 ;

        if (toPointIdx < 0)
            toPointIdx = 0 ;

        if (fromPointIdx > lastPointIdx)
            fromPointIdx = lastPointIdx ;

        if (toPointIdx > lastPointIdx)
            toPointIdx = lastPointIdx ;

		return lengthFromFirstPoint[toPointIdx] - lengthFromFirstPoint[fromPointIdx] ;
	}


	/**
	 * Calculates the geodesic length of the 3D line that joins this route's waypoints in sequence.
	 *
	 * @return 		this route's path geodesic length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public double
	length( )
	{
		return lengthToLastPoint[0] ;
	}


	public
	double
    turningAngleAtPoint( final int pointIdx )
	{
		return turningAngleAtPoint[pointIdx] ;
	}


	public
	double
	turningAngleWithSegment( final double currentBearingDeg
						   , final int    segmentIdx
						   )
	{
		return Geo.turningAngle( initialBearingOfRouteSegments[segmentIdx] - currentBearingDeg ) ;
	}


	/**
	 * The turning angle between the bearing <b>from</b> a location to a waypoint and the bearing of that waypoint's forward segment.
	 *
	 * @param fromLat	    The latitude of the <b>from</b> location.
	 * @param fromLon	    The longitude of the <b>from</b> location.
	 * @param toPointIdx	The index of the <b>to</b> route's point.
	 */
	public
	double
	turningAngleFromLocation( final double  fromLat
							, final double  fromLon
							, final int     toPointIdx
							)
	{
		RoutePoint  toPoint = routePoints.get( toPointIdx ) ;

		return turningAngleWithSegment( Geo.initialBearing( fromLat, fromLon, toPoint.getLatitude(), toPoint.getLongitude() ), toPointIdx ) ;
	}


	public
	void
	reverse( )
	{
		GpxUtils.reverseRoute( route ) ;
		refreshCaches( ) ;
	}
}

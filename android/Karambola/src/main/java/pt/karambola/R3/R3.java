/*
 * R3.java
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

package pt.karambola.R3;

public class
R3
{
	public final double	x ;
	public final double	y ;
	public final double	z ;
	
	public final static R3	O	= new R3( 0.0, 0.0, 0.0 ) ;
	public final static R3	I	= new R3( 1.0, 0.0, 0.0 ) ;
	public final static R3	J	= new R3( 0.0, 1.0, 0.0 ) ;
	public final static R3	K	= new R3( 0.0, 0.0, 1.0 ) ;

	public
	R3( final double x, final double y, final double z )
	{
		this.x = x ;
		this.y = y ;
		this.z = z ;
	}


	public static
	R3
	add( final R3 a
       , final R3 b
       )
	{
		return new R3( a.x + b.x	// x
				     , a.y + b.y	// y
				     , a.z + b.z	// z
				     ) ;
	}


	public static
	R3
    sub( final R3 a
       , final R3 b
       )
	{
		return new R3( a.x - b.x	// x
				     , a.y - b.y	// y
				     , a.z - b.z	// z
				     ) ;
	}


	public static
	R3
	scalar( final double k
          , final R3     v
          )
	{
		return new R3( k * v.x		// x
				     , k * v.y		// y
				     , k * v.z		// z
				     ) ;
	}


	public static
	double
	dot( final R3 a
       , final R3 b
       )
	{
		return a.x * b.x  +  a.y * b.y  +  a.z * b.z ;
	}


	public static
	R3
	cross( final R3 a
         , final R3 b
         )
	{
		return new R3( a.y * b.z - a.z * b.y	// x
					 , a.z * b.x - a.x * b.z	// y
					 , a.x * b.y - a.y * b.x	// z
					 ) ;
	}


	public static
	double
	modulus( final R3 v )
	{
		return Math.sqrt( v.x * v.x  +  v.y * v.y  +  v.z * v.z ) ;
	}


	public static
	R3
	versor( final R3 v )
	{
		final double m = modulus( v ) ;
		
		if (m == 0.0)
			return O ;

		return scalar( 1.0 / m, v ) ;
	}


	/**
	 * Calculates the distance between two points.
	 * 
	 * @param a 	first point
	 * @param b 	second point
     *
	 * @return 		distance between a and b
	 *
	 * @author 		Afonso Santos
	 */
	public static
	double
	distance( final R3 a
            , final R3 b
            )
	{
		return modulus( sub( b, a ) ) ;
	}


	/**
	 * Calculates the distance from a point to a line segment.
	 * (optimized variant, this method has redundant parameters so as to facilitate its use with pre-calculated cached values)
	 *
	 * @param v 	        	the point from which distance is measured.
	 * @param a 		        start of line segment.
	 * @param b 	            end of line segment.
	 * @param segment 	        a to b line segment vector. (redundant)
	 * @param segmentModulus	length of line segment. (redundant)
	 *
	 * @return 		an array of 2 doubles:
	 *              [0] distance from v to the closest point of line segment [a,b],
	 *       		[1] segment coefficient of the closest point of the segment.
	 *              Coefficient values < 0 mean the closest point is a.
	 *              Coefficient values > 1 mean the closest point is b.
	 *              Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static
	double[]
	distanceToSegment( final R3     v
                     , final R3     a
                     , final R3     b
                     , final R3     segment
                     , final double segmentModulus
                     )
	{
		final double[] results	= new double[2] ;

		final R3 	 av	        = R3.sub( v, a ) ;
		final double avModulus 	= R3.modulus( av ) ;

		if (segmentModulus == 0.0)
		{
			results[0] = avModulus ;		// Distance
			results[1] = 0.0 ;				// Segment coefficient.
		}
		else
		{
			final double avScaProjSegment  = R3.dot( av, segment) / segmentModulus ;
			final double segmentCoeficient = results[1] = avScaProjSegment / segmentModulus ;

			if (segmentCoeficient <= 0.0)					// Point is before start of the segment ?
				results[0] = avModulus ;		            // Use distance to start of segment.
			else if (segmentCoeficient >= 1.0)				// Point is past the end of the segment ?
				results[0] = R3.modulus( R3.sub( v, b ) ) ;	// Use distance to end of segment.
			else									    	// Point is within the segment's start/end perpendicular boundaries.
			{
				if (avScaProjSegment >= avModulus)	// Test to avoid machine float representation epsilon rounding errors that would result in exception on sqrt.
					results[0] = 0.0 ;																			// v point is co-linear with segment.
				else
					results[0] = Math.sqrt( avModulus * avModulus - avScaProjSegment * avScaProjSegment ) ;		// Perpendicular distance from v point to segment.
			}
		}

		return results ;
	}


	/**
	 * Calculates the distance from a point to a line segment.
	 *
	 * @param v 	the point from which distance is measured.
	 * @param a 	start of line segment.
	 * @param b 	end of line segment.
	 * 
	 * @return 		an array of 2 doubles:
	 *              [0] distance from v to the closest point of line segment [a,b],
	 *       		[1] segment coefficient of the closest point of the segment.
	 *              Coefficient values < 0 mean the closest point is a.
	 *              Coefficient values > 1 mean the closest point is b.
	 *              Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static
	double[]
	distanceToSegment( final R3 v
                     , final R3 a
                     , final R3 b
                     )
	{
		final R3 a2b = R3.sub( b, a ) ;

		return distanceToSegment( v, a, b, a2b, R3.modulus( a2b ) ) ;
	}


	/**
	 * Calculates the distance from a point to a path. (pre-calculated optimized variant)
	 *
	 * @param v 					the point from which the distance is measured.
	 * @param pathPoints 			the array of points which, when sequentially joined by line segments, form a path.
	 * @param pathSegments 			pre-calculated (redundant) array of path segment's vectors (from start to end of each segment).
	 * @param pathSegmentsModulus   pre-calculated (redundant) array with path segment's lengths.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  n is the segment that joins points [n, n+1]
	 *       		[2] segment coefficient for the closest point (within the closest segment).
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static
	double[]
    distanceToPath( final R3         v
                  , final R3[]       pathPoints
                  , final R3[]       pathSegments
                  , final double[]   pathSegmentsModulus
                  )
	{
		int nSegments = pathPoints.length - 1 ;

        double	closestSegmentDistance   = distance( v, pathPoints[0] ) ;
		int 	closestSegmentIdx        = 0 ;
		double	closestSegmentCoeficient = 0.0 ;

		for (int segmentIdx = 0  ;  segmentIdx < nSegments  ;  ++segmentIdx)
		{
			final double[] distanceToSegmentResults = distanceToSegment( v, pathPoints[segmentIdx], pathPoints[segmentIdx+1], pathSegments[segmentIdx], pathSegmentsModulus[segmentIdx] ) ;

			if (distanceToSegmentResults[0] < closestSegmentDistance)
			{
				closestSegmentDistance   = distanceToSegmentResults[0] ;
				closestSegmentIdx        = segmentIdx ;
				closestSegmentCoeficient = distanceToSegmentResults[1] ;
			}
		}

		final double[] results	= new double[3] ;
		results[0] = closestSegmentDistance ;
		results[1] = closestSegmentIdx ;
		results[2] = closestSegmentCoeficient ;

		return results ;
	}


	/**
	 * Calculates the distance from a point to a line segmented path.
	 *
	 * @param v 			the point from which the distance is measured.
	 * @param pathPoints 	the array of points which, when sequentially joined by line segments, form a path.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  n is the segment that joins points [n, n+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static
	double[]
	distanceToPath( final R3   v
                  , final R3[] pathPoints
                  )
	{
		int nSegments = pathPoints.length - 1 ;

        double	closestSegmentDistance   = distance( v, pathPoints[0] ) ;
		int 	closestSegmentIdx        = 0 ;
		double	closestSegmentCoeficient = 0.0 ;

		for (int segmentIdx = 0  ;  segmentIdx < nSegments  ;  ++segmentIdx)
		{
			final double[] distanceToSegmentResults = distanceToSegment( v, pathPoints[segmentIdx], pathPoints[segmentIdx+1] ) ;
			
			if (distanceToSegmentResults[0] < closestSegmentDistance)
			{
				closestSegmentDistance   = distanceToSegmentResults[0] ;
				closestSegmentIdx        = segmentIdx ;
				closestSegmentCoeficient = distanceToSegmentResults[1] ;
			}
		}

		final double[] results	= new double[3] ;
		results[0] = closestSegmentDistance ;
		results[1] = closestSegmentIdx ;
		results[2] = closestSegmentCoeficient ;

		return results ;
	}
}

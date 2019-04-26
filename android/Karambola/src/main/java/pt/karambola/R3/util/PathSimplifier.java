/*
 * PathSimplifier.java
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

package pt.karambola.R3.util;

import java.util.ArrayList;
import java.util.List;

import pt.karambola.R3.R3;

public class
PathSimplifier
{
	private static
	PathSegmentError
	computeSegmentError( final R3[] pathPoints
                       , final int  firstPointIdx
                       , final int  lastPointIdx
                       )
	{
		PathSegmentError segmentError = new PathSegmentError( firstPointIdx, lastPointIdx ) ;

		final R3     firstPoint     = pathPoints[firstPointIdx] ;
		final R3     lastPoint	    = pathPoints[lastPointIdx] ;
		final R3     segment        = R3.sub( lastPoint, firstPoint ) ;
		final double segmentModulus = R3.modulus( segment ) ;

		for (int pointIdx = firstPointIdx + 1  ;  pointIdx < lastPointIdx  ;  ++pointIdx )
		{
			final double   pointError = R3.distanceToSegment( pathPoints[pointIdx], firstPoint, lastPoint, segment, segmentModulus )[0] ;

			// Retain which path point is the farthest from the segment.
			if (pointError > segmentError.maxError)
			{
				segmentError.maxError    	  = pointError ;
				segmentError.maxErrorPointIdx = pointIdx ;
			}
		}

		return segmentError ;
	}


	/**
	 * Finds the path with the minimum amount of segments for which none of the discarded points from the original path is more than <b>maxError</b> distance away from one of those segments.
	 * The simplified path will not have more than <b>maxSegments</b> points even if the <b>maxError</b> condition cannot be honored.
	 *
	 * @param pathPoints    vector of Cartesian points that define a path.
	 * @param fromPointIdx 	index of the path point where the section to be simplified begins.
	 * @param toPointIdx	index of the path point where the section to be simplified ends.
	 * @param maxSegments 	maximum number of segments of the simplified path.
	 * @param maxError    	maximum allowed distance to simplified path.
	 *
	 * @return PathSimplifierResult object with the indexes of the points that form the simplified path, and error of the solution.
	 *
	 * @author Afonso Santos
	 */
	public static
	PathSimplifierResult
	simplifySection( final R3[] 	pathPoints
			       , final int 		fromPointIdx
			       , final int 		toPointIdx
			       , final int 		maxSegments
			       , final double	maxError
				   )
	{
		final List<PathSegmentError> segmentCandidates = new ArrayList<>( ) ;
		PathSegmentError worstSegment = computeSegmentError( pathPoints, fromPointIdx, toPointIdx ) ;
		segmentCandidates.add( worstSegment ) ;

		while (segmentCandidates.size() < maxSegments  &&  worstSegment.maxError > maxError)
		{
			worstSegment = null ;

			// Determine which is the section with the worst error.
			for (PathSegmentError segment: segmentCandidates)
				if (worstSegment == null  ||  segment.maxError > worstSegment.maxError)
					worstSegment = segment ;

			if (worstSegment.maxError == 0.0)
				break ;							// No more need to loop.

			// Split the worst segment in 2 new sub-segments, split occurs where the worst offender (most distant) point is.
			segmentCandidates.remove( worstSegment ) ;
			segmentCandidates.add( computeSegmentError( pathPoints, worstSegment.firstPointIdx   , worstSegment.maxErrorPointIdx ) ) ;
			segmentCandidates.add( computeSegmentError( pathPoints, worstSegment.maxErrorPointIdx, worstSegment.lastPointIdx     ) ) ;
		}

		final boolean[]	isSegmentTipPoint	= new boolean[pathPoints.length] ;

		for (PathSegmentError segment: segmentCandidates)
			isSegmentTipPoint[segment.firstPointIdx] = isSegmentTipPoint[segment.lastPointIdx] = true ;

		final int[] pointsIdxs = new int[segmentCandidates.size() + 1] ;
		int   resultItemIdx = 0 ;

		for (int pointIdx = fromPointIdx  ;  pointIdx <= toPointIdx  ;  ++pointIdx )
			if (isSegmentTipPoint[pointIdx])
				pointsIdxs[resultItemIdx++] = pointIdx ;

		return new PathSimplifierResult( maxSegments, maxError, pointsIdxs, worstSegment.maxError ) ;
	}


	/**
	 * Finds the path with the minimum amount of segments for which none of the discarded points is more than <b>maxErr</b> distance away from one of those segments.
	 * The simplified path will not have more than <b>maxSegments</b> segments even if the <b>maxErr</b> condition cannot be honored.
	 *
	 * @param pathPoints    vector of Cartesian points that define a path.
	 * @param maxSegments 	maximum number of segments of simplified path.
	 * @param maxErr    	maximum allowed distance error.
	 *
	 * @return PathSimplifierResult object with:
     *         - indexes of the points selected to form the simplified path
     *         - error of the solution (max distance of all discarded points to the simplified path)
	 *
	 * @author Afonso Santos
	 */
	public static
	PathSimplifierResult
	simplify( final R3[]    pathPoints
            , final int     maxSegments
            , final double  maxErr
            )
	{
		return simplifySection( pathPoints, 0, pathPoints.length - 1, maxSegments, maxErr ) ;
	}
}

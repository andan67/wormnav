/*
 * GpxUtils.java
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
import java.util.List;

import pt.karambola.R3.R3;
import pt.karambola.R3.util.PathSimplifier;
import pt.karambola.R3.util.PathSimplifierResult;
import pt.karambola.commons.collections.ListUtils;
import pt.karambola.commons.util.NamedUtils;
import pt.karambola.commons.util.StringDecorator;
import pt.karambola.commons.util.TypedUtils;
import pt.karambola.geo.Geo;
import pt.karambola.geo.Units;
import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.beans.TrackSegment;
import pt.karambola.gpx.comparator.GenericPointComparator;
import pt.karambola.gpx.comparator.GenericPointComparator_Distance;
import pt.karambola.gpx.comparator.GenericPointComparator_HorizontalDistance;
import pt.karambola.gpx.comparator.RouteComparator;
import pt.karambola.gpx.comparator.RouteComparator_DistanceToPath;
import pt.karambola.gpx.comparator.RouteComparator_DistanceToStart;
import pt.karambola.gpx.comparator.RouteComparator_HorizontalDistanceToPath;
import pt.karambola.gpx.comparator.RouteComparator_HorizontalDistanceToStart;
import pt.karambola.gpx.comparator.TrackComparator;
import pt.karambola.gpx.comparator.TrackComparator_DistanceToPath;
import pt.karambola.gpx.comparator.TrackComparator_DistanceToStart;
import pt.karambola.gpx.comparator.TrackComparator_HorizontalDistanceToPath;
import pt.karambola.gpx.comparator.TrackComparator_HorizontalDistanceToStart;
import pt.karambola.gpx.decorator.GenericPointDecorator;
import pt.karambola.gpx.decorator.GenericPointDecorator_Age;
import pt.karambola.gpx.decorator.GenericPointDecorator_Distance;
import pt.karambola.gpx.decorator.GenericPointDecorator_DistanceType;
import pt.karambola.gpx.decorator.GenericPointDecorator_HorizontalDistance;
import pt.karambola.gpx.decorator.GenericPointDecorator_HorizontalDistanceType;
import pt.karambola.gpx.decorator.RouteDecorator;
import pt.karambola.gpx.decorator.RouteDecorator_Age;
import pt.karambola.gpx.decorator.RouteDecorator_DistanceToRouteType;
import pt.karambola.gpx.decorator.RouteDecorator_DistanceToStartType;
import pt.karambola.gpx.decorator.RouteDecorator_HorizontalDistanceToRouteType;
import pt.karambola.gpx.decorator.RouteDecorator_HorizontalDistanceToStartType;
import pt.karambola.gpx.decorator.RouteDecorator_HorizontalLengthTypeArity;
import pt.karambola.gpx.decorator.RouteDecorator_Length;
import pt.karambola.gpx.decorator.RouteDecorator_LengthType;
import pt.karambola.gpx.decorator.RouteDecorator_LengthTypeArity;
import pt.karambola.gpx.decorator.TrackDecorator;
import pt.karambola.gpx.decorator.TrackDecorator_Age;
import pt.karambola.gpx.decorator.TrackDecorator_DistanceToTrackType;
import pt.karambola.gpx.decorator.TrackDecorator_DistanceToStartType;
import pt.karambola.gpx.decorator.TrackDecorator_HorizontalDistanceToStartType;
import pt.karambola.gpx.decorator.TrackDecorator_HorizontalDistanceToTrackType;
import pt.karambola.gpx.decorator.TrackDecorator_HorizontalLengthTypeArity;
import pt.karambola.gpx.decorator.TrackDecorator_LengthTypeArity;
import pt.karambola.gpx.predicate.GenericPointConflict;
import pt.karambola.gpx.predicate.RouteConflict;

public
class
GpxUtils
{
	final static String CREDITS = "by GeoKarambolaLib" ;


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly( final Iterable<P>	pts
                                    , List<P>			ptsSorted		// Optional output parameter.
									)
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME	// Sort by ascending name.
									   , null							// no decorator.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly( final Iterable<P>						pts
                                    , final StringDecorator<GenericPoint>	decorator
                                    , List<P>								ptsSorted		// Optional output parameter.
									)
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME	// Sort by ascending name.
									   , decorator						// Externally provided (decorator).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly_Type( final Iterable<P>	pts
										 , List<P>				ptsSorted		// Optional output parameter.
	                                     )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME	// Sort by ascending point name.
									   , GenericPointDecorator.TYPE		// Decorate with (type).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly_Distance( final Iterable<P>	pts
			                                 , final double			refLat
			                                 , final double			refLon
			                                 , final double			refEle
			                                 , final Units			units
			                                 , List<P>				ptsSorted		// Optional output parameter.
											 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME											// Sort by ascending point name.
									   , new GenericPointDecorator_Distance( refLat, refLon, refEle, units )	// Decorate with (distance).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly_HorizontalDistance( final Iterable<P>	pts
						                               , final double		refLat
						                               , final double		refLon
						                               , final Units		units
						                               , List<P>			ptsSorted		// Optional output parameter.
													   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME											// Sort by ascending point name.
									   , new GenericPointDecorator_HorizontalDistance( refLat, refLon, units )	// Decorate with (distance).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly_DistanceType( final Iterable<P>	pts
			                                     , final double			refLat
			                                     , final double			refLon
			                                     , final double			refEle
			                                     , final Units			units
			                                     , List<P>				ptsSorted		// Optional output parameter.
												 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME												// Sort by ascending point name.
									   , new GenericPointDecorator_DistanceType( refLat, refLon, refEle, units )	// Decorate with (distance, type).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedAlphabeticaly_HorizontalDistanceType( final Iterable<P>	pts
						                                   , final double		refLat
						                                   , final double		refLon
						                                   , final Units		units
						                                   , List<P>			ptsSorted		// Optional output parameter.
														   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.NAME												// Sort by ascending point name.
									   , new GenericPointDecorator_HorizontalDistanceType( refLat, refLon, units )	// Decorate with (distance, type).
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByDistance( final Iterable<P>	pts
                                 , final double			refLat
                                 , final double			refLon
                                 , final double			refEle
								 , List<P>				ptsSorted		// Optional output parameter.
                                 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_Distance( refLat, refLon, refEle )	// Sorted, closer to farther.
			   						   , null															// No decoration.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByHorizontalDistance( final Iterable<P>	pts
			                               , final double		refLat
			                               , final double		refLon
										   , List<P>			ptsSorted		// Optional output parameter.
			                               )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_HorizontalDistance( refLat, refLon )	// Sorted, closer to farther.
			   						   , null																// No decoration.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByDistance_Distance( final Iterable<P>	pts
                                          , final double		refLat
                                          , final double		refLon
                                          , final double		refEle
                                          , final Units			units
    									  , List<P>				ptsSorted		// Optional output parameter.
                                          )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_Distance( refLat, refLon, refEle )			// Sorted, closer to farther.
			   						   , new GenericPointDecorator_Distance( refLat, refLon, refEle, units )	// Decorate with (distance) info.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByHorizontalDistance_HorizontalDistance( final Iterable<P>	pts
															  , final double		refLat
															  , final double		refLon
															  , final Units			units
															  , List<P>				ptsSorted		// Optional output parameter.
															  )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_HorizontalDistance( refLat, refLon )			// Sorted, closer to farther.
			   						   , new GenericPointDecorator_HorizontalDistance( refLat, refLon, units )		// Decorate with (distance) info.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByDistance_DistanceType( final Iterable<P>	pts
                                              , final double		refLat
                                              , final double		refLon
                                              , final double		refEle
                                              , final Units			units
     										  , List<P>				ptsSorted		// Optional output parameter.
                                              )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_Distance( refLat, refLon, refEle )				// Sorted, closer to farther.
									   , new GenericPointDecorator_DistanceType( refLat, refLon, refEle, units )	// Decorate with (distance, type) info.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByHorizontalDistance_HorizontalDistanceType( final Iterable<P>	pts
						                                          , final double		refLat
						                                          , final double		refLon
						                                          , final Units			units
						     									  , List<P>				ptsSorted		// Optional output parameter.
						                                          )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , new GenericPointComparator_HorizontalDistance( refLat, refLon )			// Sorted, closer to farther.
									   , new GenericPointDecorator_HorizontalDistanceType( refLat, refLon, units )	// Decorate with (distance, type) info.
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByType( final Iterable<P>					pts
                             , final StringDecorator<GenericPoint>	decorator
							 , List<P>								ptsSorted		// Optional output parameter.
                             )
	{
		return NamedUtils
				.getNamesSortedDecorated( pts
										, GenericPointComparator.TYPE		// Sort by ascending point type.
										, decorator							// Externally provided (decorator).
										, ptsSorted
										) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByType( final Iterable<P>	pts
							 , List<P>				ptsSorted		// Optional output parameter.
                             )
	{
		return getPointNamesSortedByType( pts
										, GenericPointDecorator.TYPE	// Decorate with (type) info
										, ptsSorted
										) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByTime( final Iterable<P>	pts
						     , List<P>				ptsSorted		// Optional output parameter.
                             )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.TIME_YOUNGER2OLDER	// Sort by ascending point time.
									   , GenericPointDecorator.AGE					// Decorate with (age) info
									   , ptsSorted
									   ) ;
	}


	public static <P extends GenericPoint>
	List<String>
	getPointNamesSortedByTime( final Iterable<P>	pts
						     , final String			day				// Localized string for singular "day"
						     , final String			days			// Localized string for plural "days"
						     , List<P>				ptsSorted		// Optional output parameter.
                             )
	{
		return NamedUtils
			   .getNamesSortedDecorated( pts
									   , GenericPointComparator.TIME_YOUNGER2OLDER	// Sort by ascending point time.
									   , new GenericPointDecorator_Age( day, days )	// Decorate with localized (age) info
									   , ptsSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedAlphabeticaly( final Iterable<R>		rtes
                                    , List<R>				rtesSorted		// Optional output parameter.
									)
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NAME	// Sort by ascending route name.
									   , null 					// No decorations
			   						   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedAlphabeticaly_Type( final Iterable<R>	rtes
										 , List<R>				rtesSorted		// Optional output parameter.
            							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NAME		// Sort by ascending route name.
									   , RouteDecorator.TYPE		// Decorate with (type) info
			   						   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedAlphabeticaly_Length( final Iterable<R>	rtes
                                           , final Units		units
   	                                       , List<R>			rtesSorted		// Optional output parameter.
            							   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NAME					// Sort by ascending route name.
									   , new RouteDecorator_Length( units )		// Decorate with (length) info
			   						   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedAlphabeticaly_LengthType( final Iterable<R>	rtes
                                               , final Units		units
       	                                       , List<R>			rtesSorted		// Optional output parameter.
            								   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NAME						// Sort by ascending route name.
									   , new RouteDecorator_LengthType( units )		// Decorate with (length, type) info
			   						   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedAlphabeticaly_LengthTypeArity( final Iterable<R>	rtes
                                                    , final Units		units
            	                                    , List<R>			rtesSorted		// Optional output parameter.
            										)
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NAME							// Sort by ascending route name.
									   , new RouteDecorator_LengthTypeArity( units )	// Decorate with (length, type, #pts) info
			   						   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByNumber( final Iterable<R>	rtes
							   , final Units		units
                               , List<R>			rtesSorted		// Optional output parameter.
							   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NUMBER		// Sort by ascending route number.
									   , null 						// No decorations
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByNumber_LengthTypeArity( final Iterable<R>	rtes
											   , final Units		units
				                               , List<R>			rtesSorted		// Optional output parameter.
											   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.NUMBER							// Sort by ascending route number.
									   , new RouteDecorator_LengthTypeArity( units )	// Decorate with (length, type) info
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByLength( final Iterable<R>	rtes
                               , final Units		units
                               , List<R>			rtesSorted		// Optional output parameter.
							   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
					   				   , RouteComparator.LENGTH							// Sort by ascending route length.
									   , new RouteDecorator_LengthTypeArity( units )	// Decorate with (length, type) info
					   				   , rtesSorted
					   				   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByHorizontalLength( final Iterable<R>	rtes
			                             , final Units			units
			                             , List<R>				rtesSorted		// Optional output parameter.
										 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
					   				   , RouteComparator.HORIZONTALLENGTH						// Sort by ascending route length.
									   , new RouteDecorator_HorizontalLengthTypeArity( units )	// Decorate with (length, type) info
					   				   , rtesSorted
					   				   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByDistanceToPath( final Iterable<R>	rtes
		                               , final double		refLat
		                               , final double		refLon
		                               , final double		refEle
                                       , final Units		units			// Units to use in decorator
                                       , List<R>			rtesSorted		// Optional output parameter.
                                       )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , new RouteComparator_DistanceToPath( refLat, refLon, refEle )				// Sorted, closer to farther.
			   						   , new RouteDecorator_DistanceToRouteType( refLat, refLon, refEle, units )	// Decorate with (distance, type) info.
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByHorizontalDistanceToPath( final Iterable<R>	rtes
					                             , final double			refLat
					                             , final double			refLon
			                                     , final Units			units			// Units to use in decorator
			                                     , List<R>				rtesSorted		// Optional output parameter.
			                                     )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , new RouteComparator_HorizontalDistanceToPath( refLat, refLon )					// Sorted, closer to farther.
			   						   , new RouteDecorator_HorizontalDistanceToRouteType( refLat, refLon, units )		// Decorate with (distance, type) info.
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByDistanceToStart( final Iterable<R>	rtes
							            , final double		refLat			// Reference latitude
							            , final double		refLon			// Reference longitude
							            , final double		refEle			// Reference elevation
                                        , final Units		units			// Units to use in decorator
	                                    , List<R>			rtesSorted		// Optional output parameter.
                                        )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , new RouteComparator_DistanceToStart( refLat, refLon, refEle )				// Sorted, closer to farther.
									   , new RouteDecorator_DistanceToStartType( refLat, refLon, refEle, units )	// Decorate with (distance, type) info.
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByHorizontalDistanceToStart( final Iterable<R>	rtes
										          , final double		refLat			// Reference latitude
										          , final double		refLon			// Reference longitude
			                                      , final Units			units			// Units to use in decorator
				                                  , List<R>				rtesSorted		// Optional output parameter.
			                                      )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , new RouteComparator_HorizontalDistanceToStart( refLat, refLon )			// Sorted, closer to farther.
									   , new RouteDecorator_HorizontalDistanceToStartType( refLat, refLon, units )	// Decorate with (distance, type) info.
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByDistance( final Iterable<R>	rtes
		                         , final double			refLat
		                         , final double			refLon
		                         , final double			refEle
                                 , final Units			units			// Units to use in decorator
                                 , final boolean        toClosest		// true: to closest route path point, false: to start point.
                                 , List<R>				rtesSorted		// Optional output parameter.
                                 )
	{
		return toClosest
			 ? getRouteNamesSortedByDistanceToPath( rtes, refLat, refLon, refEle, units, rtesSorted )
			 : getRouteNamesSortedByDistanceToStart( rtes, refLat, refLon, refEle, units, rtesSorted )
			 ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByHorizontalDistance( final Iterable<R>	rtes
					                       , final double		refLat
					                       , final double		refLon
			                               , final Units		units			// Units to use in decorator
			                               , final boolean      toClosest		// true: to closest route path point, false: to start point.
			                               , List<R>			rtesSorted		// Optional output parameter.
			                               )
	{
		return toClosest
			 ? getRouteNamesSortedByHorizontalDistanceToPath( rtes, refLat, refLon, units, rtesSorted )
			 : getRouteNamesSortedByHorizontalDistanceToStart( rtes, refLat, refLon, units, rtesSorted )
			 ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByTime( final Iterable<R>	rtes
                             , List<R>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.TIME_YOUNGER2OLDER		// Sort by ascending age.
									   , RouteDecorator.AGE						// Decorate with (days) info
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByTime( final Iterable<R>	rtes
						     , final String			day				// Localized string for singular "day"
						     , final String			days			// Localized string for plural "days"
                             , List<R>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.TIME_YOUNGER2OLDER		// Sort by ascending age.
									   , new RouteDecorator_Age( day, days )	// Decorate with localized (days) info
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	List<String>
	getRouteNamesSortedByType( final Iterable<R>	rtes
                             , final Units			units
                             , List<R>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( rtes
									   , RouteComparator.TYPE							// Sort by ascending type.
									   , new RouteDecorator_LengthTypeArity( units )	// Decorate with (length, type, #points) info
									   , rtesSorted
									   ) ;
	}


	public static <R extends Route>
	String
	getRouteNameAnnotated( final R		rte
					     , final Units	units
					     )
	{
		String decoration = new RouteDecorator_LengthTypeArity( units ).getStringDecoration( rte ) ;

		return (rte.getName() != null)
			 ? rte.getName() + " (" + decoration + ")"
			 : decoration
			 ;
	}


    public static <T extends Track>
    List<String>
    getTrackNamesSortedAlphabeticaly( final Iterable<T> trks
                                    , final Units       units
                                    , List<T>           trksSorted  // Optional output parameter
                                    )
    {
        return NamedUtils
               .getNamesSortedDecorated( trks
                                       , TrackComparator.NAME           // Sort by ascending track name.
                                       , null                           // No decorators
                                       , trksSorted
                                       ) ;
    }


    public static <T extends Track>
    List<String>
    getTrackNamesSortedAlphabeticaly_LengthTypeArity( final Iterable<T> trks
                                                    , final Units       units
                                                    , List<T>           trksSorted  // Optional output parameter
                                                    )
    {
        return NamedUtils
               .getNamesSortedDecorated( trks
                                       , TrackComparator.NAME                           // Sort by ascending track name.
                                       , new TrackDecorator_LengthTypeArity( units )    // Decorate with (length, type) info
                                       , trksSorted
                                       ) ;
    }


    public static <T extends Track>
    List<String>
    getTrackNamesSortedAlphabeticaly_HorizontalLengthTypeArity( final Iterable<T> trks
			                                                  , final Units       units
			                                                  , List<T>           trksSorted  // Optional output parameter
			                                                  )
    {
        return NamedUtils
               .getNamesSortedDecorated( trks
                                       , TrackComparator.NAME                           		// Sort by ascending track name.
                                       , new TrackDecorator_HorizontalLengthTypeArity( units )  // Decorate with (length, type) info
                                       , trksSorted
                                       ) ;
    }


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByDistanceToPath( final Iterable<T>	trks
                                       , final double		refLat			// Reference latitude
                                       , final double		refLon			// Reference longitude
                                       , final double		refEle			// Reference elevation
                                       , final Units		units
                                       , List<T>			trksSorted		// Optional output parameter
                                       )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , new TrackComparator_DistanceToPath( refLat, refLon, refEle )				// Sorted, closer to farther.
									   , new TrackDecorator_DistanceToTrackType( refLat, refLon, refEle, units )		// Decorate with (distance, type) info.
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByHorizontalDistanceToPath( final Iterable<T>	trks
			                                     , final double			refLat			// Reference latitude
			                                     , final double			refLon			// Reference longitude
			                                     , final Units			units
			                                     , List<T>				trksSorted		// Optional output parameter
			                                     )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , new TrackComparator_HorizontalDistanceToPath( refLat, refLon )					// Sorted, closer to farther.
									   , new TrackDecorator_HorizontalDistanceToTrackType( refLat, refLon, units )		// Decorate with (distance, type) info.
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByDistanceToStart( final Iterable<T>	trks
							            , final double		refLat			// Reference latitude
							            , final double		refLon			// Reference longitude
                                        , final double		refEle			// Reference elevation
                                        , final Units		units
                                        , List<T>			trksSorted		// Optional output parameter
                                        )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , new TrackComparator_DistanceToStart( refLat, refLon, refEle )				// Sorted, closer to farther.
									   , new TrackDecorator_DistanceToStartType( refLat, refLon, refEle, units )	// Decorate with (distance, type) info.
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByHorizontalDistanceToStart( final Iterable<T>	trks
			                                      , final double		refLat
			                                      , final double		refLon
			                                      , final Units			units
			                                      , List<T>				trksSorted		// Optional output parameter
			                                      )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , new TrackComparator_HorizontalDistanceToStart( refLat, refLon )			// Sorted, closer to farther.
									   , new TrackDecorator_HorizontalDistanceToStartType( refLat, refLon, units )	// Decorate with (distance, type) info.
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByLength( final Iterable<T>	trks
                               , final Units		units
                               , List<T>			trksSorted	// Optional output parameter
							   )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , TrackComparator.LENGTH							// Sort by ascending Track length.
									   , new TrackDecorator_LengthTypeArity( units )	// Decorate with (length, type, #points) info
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByHorizontalLength( final Iterable<T>	trks
			                             , final Units			units
			                             , List<T>				trksSorted	// Optional output parameter
										 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , TrackComparator.HORIZONTALLENGTH						// Sort by ascending Track length.
									   , new TrackDecorator_HorizontalLengthTypeArity( units )	// Decorate with (length, type, #points) info
									   , trksSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByTime( final Iterable<T>	trks
                             , List<T>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , TrackComparator.TIME_YOUNGER2OLDER		// Sort by ascending age.
									   , TrackDecorator.AGE						// Decorate with (n Days) info
									   , rtesSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByTime( final Iterable<T>	trks
						     , final String			day				// Localized string for singular "day"
						     , final String			days			// Localized string for plural "days"
                             , List<T>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , TrackComparator.TIME_YOUNGER2OLDER		// Sort by ascending age.
									   , new TrackDecorator_Age( day, days )	// Decorate with localized (n Days) info
									   , rtesSorted
									   ) ;
	}


	public static <T extends Track>
	List<String>
	getTrackNamesSortedByType( final Iterable<T>	trks
                             , final Units			units
                             , List<T>				rtesSorted		// Optional output parameter.
							 )
	{
		return NamedUtils
			   .getNamesSortedDecorated( trks
									   , TrackComparator.TYPE							// Sort by ascending type.
									   , new TrackDecorator_LengthTypeArity( units )	// Decorate with (length, type, #points) info
									   , rtesSorted
									   ) ;
	}


	public static <T extends Track>
	String
	getTrackNameAnnotated( final T		trk
						 , final Units	units
						 )
	{
		String decoration = new TrackDecorator_LengthTypeArity( units ).getStringDecoration( trk ) ;

		return (trk.getName() != null)
			 ? trk.getName() + " (" + decoration + ")"
			 : decoration
			 ;
	}


	// Similar points: overlapping points with same Name & Type.
	public static	<P extends GenericPoint>
	List<P>
	getPointsSimilar( List<P> pts )
	{
		return ListUtils.getConflictingItems( pts, GenericPointConflict.SIMILAR )  ;
	}


	// Overlapping points: those less than 10m appart.
	public static	<P extends GenericPoint>
	List<P>
	getPointsOverlapping( List<P> pts )
	{
		return ListUtils.getConflictingItems( pts, GenericPointConflict.OVERLAPPING )  ;
	}


	public static	<P extends GenericPoint>
	List<String>
	getDistinctPointTypes( Iterable<P> pts )
	{
		return 	TypedUtils.getTypes( pts ) ;
	}


	public static	<P extends GenericPoint>
	boolean
	arePointsOverlapping( final P pt1
			            , final P pt2
						)
	{
		return GenericPointConflict.OVERLAPPING.test( pt1, pt2 ) ;
	}


	public static	<P extends GenericPoint>
	boolean
	arePointsHorizontalyOverlapping( final P pt1
			                       , final P pt2
								   )
	{
		return GenericPointConflict.HORIZONTALYOVERLAPPING.test( pt1, pt2 ) ;
	}


	public static	<P extends GenericPoint>
	boolean
	arePointsSimilar( final P pt1
			        , final P pt2
					)
	{
		return GenericPointConflict.SIMILAR.test( pt1, pt2 ) ;
	}


	public static	<P extends GenericPoint>
	boolean
	arePointsHorizontalySimilar( final P pt1
			                   , final P pt2
							   )
	{
		return GenericPointConflict.HORIZONTALYSIMILAR.test( pt1, pt2 ) ;
	}


	public static	<R extends Route>
	boolean
	areRoutesOverlapping( final R rte1
			            , final R rte2
						)
	{
		return RouteConflict.OVERLAPPING.test( rte1, rte2 ) ;
	}


	public static	<R extends Route>
	boolean
	areRoutesHorizontalyOverlapping( final R rte1
			                       , final R rte2
								   )
	{
		return RouteConflict.HORIZONTALYOVERLAPPING.test( rte1, rte2 ) ;
	}


	public static  <P extends GenericPoint>
	List<P>
	purgePointsOverlapping( List<P> pts )
	{
		return ListUtils.purge( pts, GenericPointConflict.OVERLAPPING, GenericPointComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static  <P extends GenericPoint>
	List<P>
	purgePointsHorizontalyOverlapping( List<P> pts )
	{
		return ListUtils.purge( pts, GenericPointConflict.HORIZONTALYOVERLAPPING, GenericPointComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static  <P extends GenericPoint>
	List<P>
	purgePointsSimilar( List<P> pts )
	{
		return ListUtils.purge( pts, GenericPointConflict.SIMILAR, GenericPointComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static  <P extends GenericPoint>
	List<P>
	purgePointsHorizontalySimilar( List<P> pts )
	{
		return ListUtils.purge( pts, GenericPointConflict.HORIZONTALYSIMILAR, GenericPointComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static
	int
	purgePointsOverlapping( Gpx gpx )
	{
		List<Point>	originalPts			= gpx.getPoints( ) ;
		int			originalPtsCount	= originalPts.size() ;
		List<Point> purgedPts			= purgePointsOverlapping( originalPts ) ;
		int			purgedPtsCount		= purgedPts.size() ;

		if (purgedPtsCount != originalPtsCount)
			gpx.setPoints( purgedPts ) ;

		return originalPtsCount - purgedPtsCount ;
    }


	public static
	int
	purgePointsHorizontalyOverlapping( Gpx gpx )
	{
		List<Point>	originalPts			= gpx.getPoints( ) ;
		int			originalPtsCount	= originalPts.size() ;
		List<Point> purgedPts			= purgePointsHorizontalyOverlapping( originalPts ) ;
		int			purgedPtsCount		= purgedPts.size() ;

		if (purgedPtsCount != originalPtsCount)
			gpx.setPoints( purgedPts ) ;

		return originalPtsCount - purgedPtsCount ;
    }


	public static
	int
	purgePointsSimilar( Gpx gpx )
	{
		List<Point>	originalPts			= gpx.getPoints( ) ;
		int			originalPtsCount	= originalPts.size() ;
		List<Point> purgedPts			= purgePointsSimilar( originalPts ) ;
		int			purgedPtsCount		= purgedPts.size() ;

		if (purgedPtsCount != originalPtsCount)
			gpx.setPoints( purgedPts ) ;

		return originalPtsCount - purgedPtsCount ;
	}


	public static
	int
	purgePointsHorizontalySimilar( Gpx gpx )
	{
		List<Point>	originalPts			= gpx.getPoints( ) ;
		int			originalPtsCount	= originalPts.size() ;
		List<Point> purgedPts			= purgePointsHorizontalySimilar( originalPts ) ;
		int			purgedPtsCount		= purgedPts.size() ;

		if (purgedPtsCount != originalPtsCount)
			gpx.setPoints( purgedPts ) ;

		return originalPtsCount - purgedPtsCount ;
	}


	public static	<P extends Point>
	int
	addPointsPurgeOverlapping( Gpx                  gpx
			                 , final Iterable<P>	pts
							 )
	{
		gpx.addPoints( pts ) ;
		return purgePointsOverlapping( gpx ) ;
	}


	public static	<P extends Point>
	int
	addPointsPurgeHorizontalyOverlapping( Gpx 				gpx
			                            , final Iterable<P> pts
										)
	{
		gpx.addPoints( pts ) ;
		return purgePointsHorizontalyOverlapping( gpx ) ;
	}


	public static	<P extends Point>
	int
	addPointsPurgeSimilar( Gpx 					gpx
			             , final Iterable<P> 	pts
						 )
	{
		gpx.addPoints( pts ) ;
		return purgePointsSimilar( gpx ) ;
	}


	public static	<P extends Point>
	int
	addPointsPurgeHorizontalySimilar( Gpx 				gpx
									, final Iterable<P> pts
									)
	{
		gpx.addPoints( pts ) ;
		return purgePointsHorizontalySimilar( gpx ) ;
	}


	public static	<R extends Route>
	List<String>
	getDistinctRouteTypes( final Iterable<R> rtes )
	{
		return TypedUtils.getTypes( rtes ) ;
	}

    /**
     * Piotr Miller 24th June, 2017
	 * Missing from the original library. Needed in the Track Manager / edit properties
     */
	public static <R extends Track>
	List<String>
	getDistinctTrackTypes(final Iterable<R> tracks) {
		return TypedUtils.getTypes(tracks);
	}


	public static
	int
	getRoutesMaxNumber( final Gpx gpx )
	{
		int maxNumber = 0 ;

		for (Route rte: gpx.getRoutes())
			if (rte.getNumber() != null  &&  rte.getNumber() > maxNumber)
				maxNumber = rte.getNumber() ;

		return maxNumber ;
	}


	public static	<R extends Route>
	boolean
	isRouteLooped( final R 		rte
			     , final double proximityThreshold
				 )
	{
		List<RoutePoint> routePoints = rte.getRoutePoints() ;
		RoutePoint firstPoint = routePoints.get( 0 ) ;
		RoutePoint lastPoint  = routePoints.get( routePoints.size() - 1 ) ;

		return distance( firstPoint, lastPoint ) < proximityThreshold ;
	}


	/**
	 * Split's a route at a certain split point.
	 * If requested loop closes the route back to that same split point.
	 *
	 * @param rte 	        the route that defines the path line. The route will be modified by this method.
	 * @param splitPointIdx	the index of the route point where the spliting takes place.
	 * @param closeLoop 	if true the route will be closed back to the point.
	 *
	 * @authors		Piotr Miller, Afonso Santos
	 */
	public static	<R extends Route>
	void
	splitRoute(       R   		rte
			  , final int 		splitPointIdx
			  , final boolean	closeLoop
			  )
	{
		List<RoutePoint> rtePoints          = rte.getRoutePoints() ;
		List<RoutePoint> rtePointsReordered = new ArrayList<>(rtePoints.subList( splitPointIdx, rtePoints.size() )) ;

		if (closeLoop)
			rtePointsReordered.addAll( rtePoints.subList( 0, splitPointIdx+1 ) ) ;

		rte.setRoutePoints( rtePointsReordered ) ;
	}


	/**
	 * Calculates the initial bearing (forward azimuth) between 2 points.
	 *
	 * @param first 	1st point.
	 * @param second 	2nd point.
	 *
	 * @return 		initial bearing (degrees clockwise from true North) at 1st point, headed towards 2nd point.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <P extends GenericPoint>
	double
	initialBearing( final P first
	              , final P second
	              )
	{
		return Geo.initialBearing( first.getLatitude(), first.getLongitude(), second.getLatitude(), second.getLongitude() ) ;
	}


	/**
	 * True if any of the path's points lacks elevation information.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	boolean
    isPath2D( final List<P> path )
	{
		for (GenericPoint pt: path)
			if (pt.getElevation() == null)
				return true ;

		return false ;
	}


	/**
	 * True if any of the route's points lacks elevation information.
	 *
	 * @author 		Afonso Santos
	 */
	public static	<R extends Route>
	boolean
	isRoute2D( final R rte )
	{
		return isPath2D( rte.getRoutePoints() ) ;
	}


	/**
	 * True if any of the tracks's points lacks elevation information.
	 *
	 * @author 			Afonso Santos
	 */
	public static  <T extends Track>
	boolean
	isTrack2D( final T trk )
	{
		for (TrackSegment trkSeg: trk.getTrackSegments( ))
			if (isPath2D( trkSeg.getTrackPoints( ) ))
				return true ;

		return false ;
	}


    public static	<R extends Route>
    boolean
    isRouteHorizontalyLooped( final R 		rte
                            , final double  proximityThreshold
                            )
    {
        List<RoutePoint> routePoints = rte.getRoutePoints() ;
        RoutePoint firstPoint = routePoints.get( 0 ) ;
        RoutePoint lastPoint  = routePoints.get( routePoints.size() - 1 ) ;

        return horizontalDistance( firstPoint, lastPoint ) < proximityThreshold ;
    }


	// Overlapping routes: those with WPs spatialy less than 10m appart.
	public static  <R extends Route>
	List<R>
	getRoutesOverlapping( final List<R> rtes )
	{
		return ListUtils.getConflictingItems( rtes, RouteConflict.OVERLAPPING )  ;
	}


	// Horizontaly overlapping routes: those with WPs horizontaly less than 10m appart.
	public static  <R extends Route>
	List<R>
	getRoutesHorizontalyOverlapping( final List<R> rtes )
	{
		return ListUtils.getConflictingItems( rtes, RouteConflict.HORIZONTALYOVERLAPPING )  ;
	}


	public static  <R extends Route>
	List<R>
	purgeRoutesOverlapping( List<R> rtes )
	{
		return ListUtils.purge( rtes, RouteConflict.OVERLAPPING, RouteComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static  <R extends Route>
	List<R>
	purgeRoutesHorizontalyOverlapping( List<R> rtes )
	{
		return ListUtils.purge( rtes, RouteConflict.HORIZONTALYOVERLAPPING, RouteComparator.TIME_OLDER2YOUNGER ) ;
	}


	public static
	int
	purgeRoutesOverlapping( Gpx gpx )
	{
		List<Route>	originalRoutes		= gpx.getRoutes( ) ;
		int			originalRoutesCount	= originalRoutes.size() ;
		List<Route> purgedRoutes		= purgeRoutesOverlapping( originalRoutes ) ;
		int			purgedRoutesCount	= purgedRoutes.size() ;

		if (purgedRoutesCount != originalRoutesCount)
			gpx.setRoutes( purgedRoutes ) ;

		return originalRoutesCount - purgedRoutesCount ;
	}


	public static
	int
	purgeRoutesHorizontalyOverlapping( Gpx gpx )
	{
		List<Route>	originalRoutes		= gpx.getRoutes( ) ;
		int			originalRoutesCount	= originalRoutes.size() ;
		List<Route> purgedRoutes		= purgeRoutesHorizontalyOverlapping( originalRoutes ) ;
		int			purgedRoutesCount	= purgedRoutes.size() ;

		if (purgedRoutesCount != originalRoutesCount)
			gpx.setRoutes( purgedRoutes ) ;

		return originalRoutesCount - purgedRoutesCount ;
	}


	/**
	 * Reverses route-point order.
	 *
	 * @param route
	 */
	public static  <R extends Route>
	void
	reverseRoute( R route )
	{
		route.setRoutePoints( ListUtils.reverse( route.getRoutePoints( ) ) ) ;
	}


	/**
	 * Reverses track segments order and track-point order within each track segment.
	 *
	 * @param track
	 */
	public static  <T extends Track>
	void
	reverseTrack( T track )
	{
		track.setTrackSegments( ListUtils.reverse( track.getTrackSegments( ) ) ) ;
	
		for (TrackSegment trackSegment: track.getTrackSegments( ))
			trackSegment.setTrackPoints( ListUtils.reverse( trackSegment.getTrackPoints( ) ) ) ;
	}


	public static	<R extends Route>
	int
	addRoutesPurgeOverlapping( Gpx                  gpx
                             , final Iterable<R>    rtes
                             )
	{
		gpx.addRoutes( rtes ) ;
		return purgeRoutesOverlapping( gpx ) ;
	}


	public static	<R extends Route>
	int
	addRoutesPurgeHorizontalyOverlapping( Gpx               gpx
                                        , final Iterable<R> rtes
                                        )
	{
		gpx.addRoutes( rtes ) ;
		return purgeRoutesHorizontalyOverlapping( gpx ) ;
	}


	/**
	 * Calculates the geodesic length of a path's section.
	 *
	 * @param path 		    the points that define the path.
	 * @param fromPointIdx 	the index of the point where the path section begins.
	 * @param toPointIdx 	the index of the point where the path section ends.
	 *
	 * @return 		path's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double
    lengthOfPathSection( final List<P>  path
                       , final int      fromPointIdx
                       , final int      toPointIdx
                       )
	{
		double 	lengthOfPathSection = 0.0 ;

		for (int segmentIdx = fromPointIdx  ;  segmentIdx < toPointIdx  ;  ++segmentIdx)
			lengthOfPathSection += distance( path.get( segmentIdx ), path.get( segmentIdx+1 ) ) ;

		return lengthOfPathSection ;			// Distance in meters.
	}


	/**
	 * Calculates the horizontal (map projection) length of the 3D line that joins a path section's points in sequence.
	 *
     * @param path 		    the points that define the path.
     * @param fromPointIdx 	the index of the point where the path section begins.
     * @param toPointIdx 	the index of the point where the path section ends.
	 *
	 * @return 		path's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double
	horizontalLengthOfPathSection( final List<P> path
                                 , final int     fromPointIdx
                                 , final int     toPointIdx
                                 )
	{
		double 	horizontalLengthOfPathSection = 0.0 ;

		for (int pointIdx = fromPointIdx  ;  pointIdx < toPointIdx  ;  ++pointIdx)
			horizontalLengthOfPathSection += horizontalDistance( path.get( pointIdx ), path.get( pointIdx+1 ) ) ;

		return horizontalLengthOfPathSection ;			// Distance in meters.
	}


	/**
	 * Calculates the geodesic length of the 3D line that joins all the path points in sequence.
	 *
	 * @param path 	the point path who's length is to be calculated.
	 * @return 		path's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double
	lengthOfPath( final List<P> path )
	{
		return lengthOfPathSection( path, 0, path.size() - 1 ) ;
	}


	/**
	 * Calculates the horizontal (map projection) length of the 3D line that joins all the path points in sequence.
	 *
	 * @param path 	the point path who's length is to be calculated.
	 * @return 		path's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double
	horizontalLengthOfPath( final List<P> path )
	{
		return horizontalLengthOfPathSection( path, 0, path.size() - 1 ) ;
	}
	

	/**
	 * Calculates the geodesic length of the 3D line that joins all the route's waypoints in sequence.
	 *
	 * @param rte 	the route who's length is to be calculated.
	 * @return 		route's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static  <R extends Route>
	double
	lengthOfRoute( final R rte )
	{
		return lengthOfPath( rte.getRoutePoints( ) ) ;
	}
	

	/**
	 * Calculates the horizontal (map projection) length of the 3D line that joins all the route's waypoints in sequence.
	 *
	 * @param rte 	the route who's length is to be calculated.
	 * @return 		route's length (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static  <R extends Route>
	double
	horizontalLengthOfRoute( final R rte )
	{
		return horizontalLengthOfPath( rte.getRoutePoints( ) ) ;
	}


	/**
	 * Calculates the geodesic length of the 3D line that joins all track points in sequence.
	 *
	 * @param trk 		the track who's length is to be calculated.
	 * @return 			track's length (meters)
	 *
	 * @author 			Afonso Santos
	 */
	public static  <T extends Track>
	double
	lengthOfTrack( final T trk )
	{
		double 	acumDistance	= 0.0 ;
	
		for (TrackSegment trkSeg: trk.getTrackSegments( ))
			acumDistance += lengthOfPath( trkSeg.getTrackPoints( ) ) ;

		return acumDistance ;			// Distance in meters.
	}


	/**
	 * Calculates the horizontal (map projection) length of the 3D line that joins all track points in sequence.
	 *
	 * @param trk 		the track who's length is to be calculated.
	 * @return 			track's length (meters)
	 *
	 * @author 			Afonso Santos
	 */
	public static  <T extends Track>
	double
	horizontalLengthOfTrack( final T trk )
	{
		double 	acumDistance	= 0.0 ;

		for (TrackSegment trkSeg: trk.getTrackSegments( ))
			acumDistance += horizontalLengthOfPath( trkSeg.getTrackPoints( ) ) ;

		return acumDistance ;			// Distance in meters.
	}


	public static <P extends GenericPoint>
	R3
	cartesianPoint( final P p )
	{
		return p.getElevation() == null
			 ? Geo.horizontalCartesian( p.getLatitude(), p.getLongitude() )
			 : Geo.cartesian( p.getLatitude(), p.getLongitude(), p.getElevation() )
			 ;
	}


	static <P extends GenericPoint>
	R3[]
	cartesianPath( final List<P> points )
	{
		final R3[] pointsR3	= new R3[points.size()] ;
	
		int pointIdx = 0 ;
	
		for (final P p: points)
			pointsR3[pointIdx++] = cartesianPoint( p ) ;
	
		return pointsR3;
	}


	static <P extends GenericPoint>
	R3[]
	horizontalCartesianPath( final List<P> points )
	{
		final R3[] pointsR3	= new R3[points.size()] ;
	
		int pointIdx = 0 ;
	
		for (final P p: points)
			pointsR3[pointIdx++] = Geo.horizontalCartesian( p.getLatitude(), p.getLongitude() ) ;
	
		return pointsR3;
	}


	private static <P extends GenericPoint>
	double
	simplifyPoints( List<P>      points
                  , final int    maxSegments
                  , final double accuracyMtr
                  )
	{
		PathSimplifierResult pathSimplifierResult = PathSimplifier.simplify( cartesianPath( points ), maxSegments, accuracyMtr ) ;
	
		final List<P>	simplifiedPoints = new ArrayList<>( ) ;
	
		for (final int pointIdx: pathSimplifierResult.pointsIdxs)
			simplifiedPoints.add( points.get( pointIdx ) ) ;
	
		points.clear( ) ;
		points.addAll( simplifiedPoints ) ;
	
		return pathSimplifierResult.error ;
	}


	/**
	 * Finds a simpler route path with the minimum possible amount of linear sub-segments for which none of the
	 * discarded route points is more than <b>accuracyMtr</b> distance away from one of those linear sub-segments.
	 * The new simplified route path will not have more than <b>maxSegments</b> linear sub-segments even if the <b>accuracyMtr</b> cannot be honored.
	 *
	 * @param route         route who's route points define a path to be simplified.
	 * @param maxSegments 	maximum number of segments of simplified path.
	 * @param accuracyMtr  	maximum allowed distance error.
	 *
     * @return  error of the solution (max distance of all discarded points to the simplified path)
	 *
	 * @author Afonso Santos
	 */
	public static  <R extends Route>
	double
	simplifyRoute( R            route
                 , final int    maxSegments
                 , final double accuracyMtr
                 )
	{
		final List<RoutePoint>	routePoints	= new ArrayList<>( route.getRoutePoints() ) ;
		final double			resultError = simplifyPoints( routePoints, maxSegments, accuracyMtr ) ;
	
		if (route.getSrc( ) == null)
			route.setSrc( CREDITS ) ;
	
		route.setRoutePoints( routePoints ) ;
	
		return resultError ;
	}


    /**
     * For each of the track's track segments finds a simpler track segment path with the minimum possible amount of linear sub-segments
     * for which none of the discarded track points is more than <b>accuracyMtr</b> distance away from one of those linear sub-segments.
     * Each track segment is simplified as an independent path, track segments are NOT joined/merged together.
     * Each of the track's track segment's new simplified path will not have more than <b>maxSegments</b> linear sub-segments even if the <b>accuracyMtr</b> cannot be honored.
     *
     * @param track         track who's track points define a path to be simplified.
     * @param maxSegments 	maximum number of linear sub-segments (per track segment) of simplified path.
     * @param accuracyMtr  	maximum allowed distance error.
     *
     * @return  error of the solution (max distance of all discarded points to the simplified path)
     *
     * @author Afonso Santos
     */
	public static  <T extends Track>
	double
	simplifyTrack( T            track
                 , final int    maxSegments
                 , final double accuracyMtr
                 )
	{
		double trackError = 0.0 ;
	
		for (TrackSegment trkSeg: track.getTrackSegments() )
		{
			final List<TrackPoint>	trackSegmentPoints = new ArrayList<>( trkSeg.getTrackPoints() ) ;
			final double			trackSegmentError  = simplifyPoints( trackSegmentPoints, maxSegments, accuracyMtr ) ;
			trkSeg.setTrackPoints( trackSegmentPoints ) ;
	
			if (track.getSrc( ) == null)
				track.setSrc( CREDITS ) ;
	
			if (trackSegmentError > trackError)
				trackError = trackSegmentError ;
		}
	
		return trackError ;
	}


	/**
	 * Calculates the geodesic distance between 2 points.
	 * If any of the 2 points lacks elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param a 	fist point.
	 * @param a 	second point.
	 * @return 		geodesic distance from a to b (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static  <P extends GenericPoint>
	double
	distance( final P a
            , final P b
            )
	{
		if (a == b)
			return 0.0 ;

		if (a.getElevation() == null  ||  b.getElevation() == null)
			return Geo.horizontalDistance( a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude() ) ;

		return Geo.distance( a.getLatitude(), a.getLongitude(), a.getElevation(), b.getLatitude(), b.getLongitude(), b.getElevation()) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance between 2 points.
	 * Elevation information of both points is disregarded even if available.
	 *
	 * @param a 	1st point.
	 * @param b 	2nd point.
	 *
	 * @return 		horizontal distance from 1st point to 2nd point (meters).
	 *
	 * @author 		Afonso Santos
	 */
	public static  <P extends GenericPoint>
	double
	horizontalDistance( final P a
                      , final P b
                      )
	{
		if (a == b)
			return 0.0 ;

		return Geo.horizontalDistance( a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude() ) ;
	}


	/**
	 * Calculates the distance from a point to a path line.
	 *
	 * @param v 		the point from which the distance is measured.
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	distanceToPath( final R3 		v
			      , final List<P>	points
				  )
	{
		return R3.distanceToPath( v, cartesianPath( points ) ) ;
	}


	/**
	 * Calculates the distance from a point to a path line.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured
	 * @param refLon 	the longitude of the point from which the distance is measured
	 * @param refEle 	the elevation of the point from which the distance is measured
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	distanceToPath( final double 	refLat
				  , final double 	refLon
				  , final double 	refEle
				  , final List<P>	points
				  )
	{
		return distanceToPath( Geo.cartesian( refLat, refLon, refEle ), points ) ;
	}


	/**
	 * Calculates the distance from a point to a path line.
	 * If the point lacks elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param p    		the point from which the distance is measured.
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	distanceToPath( final P 		p
				  , final List<P>	points
				  )
	{
		return p.getElevation() == null
			 ? horizontalDistanceToPath( p.getLatitude(), p.getLongitude(), points )
			 : distanceToPath( p.getLatitude(), p.getLongitude(), p.getElevation(), points )
			 ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a path line.
	 * Path point elevation information is disregarded even if available.
	 *
	 * @param v 		the point from which the distance is measured.
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	horizontalDistanceToPath( final R3 		v
							, final List<P>	points
							)
	{
		return R3.distanceToPath( v, horizontalCartesianPath( points ) ) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a path line.
	 * Point elevation information is disregarded even if available.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured.
	 * @param refLon 	the longitude of the point from which the distance is measured.
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	horizontalDistanceToPath( final double 	refLat
							, final double 	refLon
							, final List<P>	points
							)
	{
		return horizontalDistanceToPath( Geo.horizontalCartesian( refLat, refLon ), points ) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a path line.
	 * Point elevation information is disregarded even if available.
	 *
	 * @param p    		the point from which the distance is measured.
	 * @param points 	the points that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance from v to the closest point of the path line,
	 *       		[1] the integer part is the index of the segment that contains the closest point.
	 *                  0 is the segment that joins points [0, 1]
	 *                  1 is the segment that joins points [1, 2]
	 *                  i is the segment that joins points [i, i+1]
	 *       		[2] segment coefficient for the closest point within the closest segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the segment.
	 *                  Coefficient values between 0 and 1 mean how far along the segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static <P extends GenericPoint>
	double[]
	horizontalDistanceToPath( final P 		p
							, final List<P>	points
							)
	{
		return horizontalDistanceToPath( p.getLatitude( ), p.getLongitude( ), points ) ;
	}


	/**
	 * Calculates the distance from a point to a route's path line.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured.
	 * @param refLon 	the longitude of the point from which the distance is measured.
	 * @param refEle 	the elevation of the point from which the distance is measured.
	 * @param rte 	    the route that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
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
	public static  <R extends Route>
	double[]
	distanceToRoute( final double refLat
                   , final double refLon
                   , final double refEle
                   , final R      rte
                   )
	{
		return distanceToPath( Geo.cartesian( refLat, refLon, refEle ), rte.getRoutePoints( ) ) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a route's path line.
	 * Route point elevation information is disregarded even if available.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured.
	 * @param refLon 	the longitude of the point from which the distance is measured.
	 * @param rte 	    the route that defines the path line.
	 *
	 * @return 		an array of 3 doubles:
	 *              [0] distance to the closest point of the route's path line,
	 *       		[1] the integer part is the index of the route segment that contains the closest point.
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
	public static  <R extends Route>
	double[]
	horizontalDistanceToRoute( final double refLat
			                 , final double refLon
			                 , final R      rte
							 )
	{
		return horizontalDistanceToPath( Geo.horizontalCartesian( refLat, refLon ), rte.getRoutePoints( ) ) ;
	}


	/**
	 * Calculates the distance from a point to a route's path line.
	 * If the point lacks elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param p 	the point from which the distance is measured.
	 * @param rte 	the route that defines the path line.
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
	public static  <P extends GenericPoint, R extends Route>
	double[]
	distanceToRoute( final P p
			       , final R rte
				   )
	{
		return p.getElevation() == null
			 ? horizontalDistanceToPath( p.getLatitude(), p.getLongitude(), rte.getRoutePoints( ) )
			 : distanceToPath( p.getLatitude(), p.getLongitude(), p.getElevation(), rte.getRoutePoints( ) )
			 ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to the Earth's surface projection of a route's path line.
     * Point and route point's elevation information is disregarded even if available.
	 *
	 * @param p 	the point from which the distance is measured.
	 * @param rte 	the route that defines the path line.
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
	public static  <P extends GenericPoint, R extends Route>
	double[]
	horizontalDistanceToRoute( final P p
			                 , final R rte
							 )
	{
		return horizontalDistanceToRoute( p.getLatitude(), p.getLongitude(), rte ) ;
	}


	/**
	 * Calculates the distance from a point to a track's path line.
	 *
	 * @param v 	the point from which the distance is measured.
	 * @param trk 	the track that defines the path line.
	 *
	 * @return 		an array of 4 doubles:
	 *              [0] distance from v to the closest point of the path line.
	 *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
	 *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
	 *                  0 is the linear sub-segment that joins points [0, 1]
	 *                  1 is the linear sub-segment that joins points [1, 2]
	 *                  n is the linear sub-segment that joins points [n, n+1]
	 *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
	 *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
	 *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
	 *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <T extends Track>
	double[]
	distanceToTrack( final R3   v
                   , final T    trk
                   )
	{
		final double[] results	= new double[4] ;

		double minDistanceToTrackSegment = Double.MAX_VALUE ;

        int trkSegmentIdx = 0 ;

		for (TrackSegment trkSeg: trk.getTrackSegments() )
		{
			final double[] distanceToPathResults  = distanceToPath( v, trkSeg.getTrackPoints( ) ) ;

			if (distanceToPathResults[0] < minDistanceToTrackSegment)
            {
                results[0] = minDistanceToTrackSegment = distanceToPathResults[0] ;
                results[1] = trkSegmentIdx ;
                results[2] = distanceToPathResults[1] ;
                results[3] = distanceToPathResults[2] ;
            }

            ++trkSegmentIdx ;
		}

		return results ;
	}


	/**
	 * Calculates the distance from a point to a track's path line.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured.
	 * @param refLon 	the longitude of the point from which the distance is measured.
	 * @param refEle 	the elevation of the point from which the distance is measured.
	 * @param trk 	    the track that defines the path line.
	 *
     * @return 		an array of 4 doubles:
     *              [0] distance from v to the closest point of the path line.
     *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
     *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
     *                  0 is the linear sub-segment that joins points [0, 1]
     *                  1 is the linear sub-segment that joins points [1, 2]
     *                  n is the linear sub-segment that joins points [n, n+1]
     *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
     *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
     *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
     *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
     *
     * @author 		Afonso Santos
     */
	public static  <T extends Track>
	double[]
	distanceToTrack( final double   refLat
                   , final double   refLon
                   , final double   refEle
                   , final T        trk
                   )
	{
		return distanceToTrack( Geo.cartesian( refLat, refLon, refEle ), trk ) ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a track's path line.
	 * Track point's elevation information is disregarded even if available.
	 *
	 * @param v 	the point from which the distance is measured.
	 * @param trk 	the track that defines the path line.
	 *
     * @return 		an array of 4 doubles:
     *              [0] distance from v to the closest point of the path line.
     *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
     *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
     *                  0 is the linear sub-segment that joins points [0, 1]
     *                  1 is the linear sub-segment that joins points [1, 2]
     *                  n is the linear sub-segment that joins points [n, n+1]
     *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
     *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
     *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
     *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <T extends Track>
	double[]
	horizontalDistanceToTrack( final R3 v
                             , final T  trk
                             )
	{
        final double[] results	= new double[4] ;

        double minDistanceToTrackSegment = Double.MAX_VALUE ;

        int trkSegmentIdx = 0 ;

        for (TrackSegment trkSeg: trk.getTrackSegments() )
        {
			final double[] distanceToPathResults  = horizontalDistanceToPath( v, trkSeg.getTrackPoints( ) ) ;

            if (distanceToPathResults[0] < minDistanceToTrackSegment)
            {
                results[0] = minDistanceToTrackSegment = distanceToPathResults[0] ;
                results[1] = trkSegmentIdx ;
                results[2] = distanceToPathResults[1] ;
                results[3] = distanceToPathResults[2] ;
            }

            ++trkSegmentIdx ;
        }

        return results ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a track's path line.
	 * Track point's elevation information is disregarded even if available.
	 *
	 * @param refLat 	the latitude of the point from which the distance is measured.
	 * @param refLon 	the longitude of the point from which the distance is measured.
	 * @param trk 	    the track that defines the path line.
	 *
     * @return 		an array of 4 doubles:
     *              [0] distance from v to the closest point of the path line.
     *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
     *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
     *                  0 is the linear sub-segment that joins points [0, 1]
     *                  1 is the linear sub-segment that joins points [1, 2]
     *                  n is the linear sub-segment that joins points [n, n+1]
     *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
     *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
     *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
     *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <T extends Track>
	double[]
	horizontalDistanceToTrack( final double refLat
                             , final double refLon
                             , final T      trk
                             )
	{
		return horizontalDistanceToTrack( Geo.horizontalCartesian( refLat, refLon ), trk )  ;
	}


	/**
	 * Calculates the distance from a point to a track's path line.
	 * If the point lacks elevation information then horizontal (map projection) distance is calculated instead.
	 *
	 * @param p    	the point from which the distance is measured.
	 * @param trk   the track that defines the path line.
	 *
     * @return 		an array of 4 doubles:
     *              [0] distance from v to the closest point of the path line.
     *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
     *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
     *                  0 is the linear sub-segment that joins points [0, 1]
     *                  1 is the linear sub-segment that joins points [1, 2]
     *                  n is the linear sub-segment that joins points [n, n+1]
     *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
     *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
     *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
     *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <P extends GenericPoint, T extends Track>
	double[]
	distanceToTrack( final P p
                   , final T trk
                   )
	{
		return p.getElevation() == null
			 ? horizontalDistanceToTrack( p.getLatitude(), p.getLongitude(), trk )
			 : distanceToTrack( p.getLatitude(), p.getLongitude(), p.getElevation(), trk )
			 ;
	}


	/**
	 * Calculates the horizontal (map projection) distance from a point to a track's path line.
	 * Point and track point's elevation information is disregarded even if available.
	 *
	 * @param p    	the point from which the distance is measured.
	 * @param trk   the track that defines the path line.
	 *
     * @return 		an array of 4 doubles:
     *              [0] distance from v to the closest point of the path line.
     *       		[1] the integer part is the index of the track segment that contains the closest linear sub-segment.
     *       		[2] the integer part is the index of the track segment's linear sub-segment that contains the closest point.
     *                  0 is the linear sub-segment that joins points [0, 1]
     *                  1 is the linear sub-segment that joins points [1, 2]
     *                  n is the linear sub-segment that joins points [n, n+1]
     *       		[3] linear sub-segment coefficient for the closest point within the closest linear sub-segment.
     *                  Coefficient values < 0 mean the closest point is the start point of the linear sub-segment.
     *                  Coefficient values > 1 mean the closest point is the end point of the linear sub-segment.
     *                  Coefficient values between 0 and 1 mean how far along the linear sub-segment the closest point is.
	 *
	 * @author 		Afonso Santos
	 */
	public static  <P extends GenericPoint, T extends Track>
	double[]
	horizontalDistanceToTrack( final P p
                             , final T trk
                             )
	{
		return horizontalDistanceToTrack( p.getLatitude(), p.getLongitude(), trk ) ;
	}
}

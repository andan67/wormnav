/*
 * PointFilter.java
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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import pt.karambola.commons.collections.Predicate;
import pt.karambola.commons.collections.functors.AllPredicate;
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Point;

public class
PointFilter
	extends		AbstractTypeDistanceFilter
	implements	Predicate<Point>
{
	private boolean 			isAgeFilterEnabled		= false ;
	private Integer 			ageMin					= null ;
	private Integer 			ageMax					= null ;
	private Predicate<Point>	compoundPredicate		= null ;


	@Override
	protected
	void
	clearCompoundPredicate( )
	{
		compoundPredicate = null ;			// Mark this singleton as dirty, to force recalculation on next getCompoundPredicate( ) call.
	}


	public
	void
	enableAgeFilter( final Integer ageMin, final Integer ageMax )
	{
		setAgeFilterEnabled( true ) ;
		setAgeMin( ageMin ) ;
		setAgeMax( ageMax ) ;
	}


	public
	void
	setAgeFilterEnabled( final boolean isAgeFilterEnabled )
	{
		this.isAgeFilterEnabled = isAgeFilterEnabled ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setAgeMin( final Integer ageMin )
	{
		this.ageMin = ageMin ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setAgeMax( final Integer ageMax )
	{
		this.ageMax = ageMax ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	disableAgeFilter( )
	{
		setAgeFilterEnabled( false ) ;
	}


	public
	boolean
	isAgeFilterEnabled( )
	{
		return isAgeFilterEnabled ;
	}


	public
	Integer
	getAgeMin( )
	{
		return ageMin ;
	}


	public
	Integer
	getAgeMax( )
	{
		return ageMax ;
	}


	@Override
	public
	boolean
	isEnabled( )
	{
		return isAgeFilterEnabled || super.isEnabled( ) ;
	}


	@Override
	public
	boolean
	evaluate( final Point pt )
	{
		if (compoundPredicate == null)
		{
			Collection<Predicate<GenericPoint>> predicates = null ;

			if (isTypeFilterEnabled)
			{
				if (predicates == null)
					predicates = new HashSet<>() ;
					
				predicates.add( new GenericPointPredicate_TypeAny( acceptedTypes ) ) ;
			}

			if (isDistanceFilterEnabled  &&  (distanceMin != null  ||  distanceMax != null))
			{
				if (predicates == null)
					predicates = new HashSet<>() ;

				if (isDistanceHorizontal)
					predicates.add( new GenericPointPredicate_HorizontalDistanceRange( distanceRefLat, distanceRefLon, distanceMin, distanceMax ) ) ;
				else
					predicates.add( new GenericPointPredicate_DistanceRange( distanceRefLat, distanceRefLon, distanceRefEle, distanceMin, distanceMax ) ) ;
			}

			if (isAgeFilterEnabled)
			{
				Date dateMax = null ;

				if (ageMin != null)
				{
					Calendar calMax = Calendar.getInstance(); 			// starts with today's date and time
					calMax.add( Calendar.DAY_OF_YEAR, -ageMin ) ;		// backdate by ageMin days
					dateMax = calMax.getTime( ) ; 						// gets modified date
				}

				Date dateMin = null ;

				if (ageMax != null)
				{
					Calendar calMin = Calendar.getInstance(); 			// starts with today's date and time
					calMin.add( Calendar.DAY_OF_YEAR, -ageMax ) ;		// backdate by ageMax days
					dateMin = calMin.getTime( ) ; 						// gets modified date
				}

				if (predicates == null)
					predicates = new HashSet<>() ;
					
				predicates.add( new GenericPointPredicate_TimeRange( dateMin, dateMax ) ) ;
			}

			if (predicates != null)
				compoundPredicate = AllPredicate.allPredicate( predicates ) ;
		}

		return (compoundPredicate == null) ? true : compoundPredicate.evaluate( pt ) ;
	}
}

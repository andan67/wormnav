/*
 * RouteFilter.java
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
import pt.karambola.gpx.beans.Route;

public class
RouteFilter
	extends		AbstractTypeDistanceFilter
	implements	Predicate<Route>
{
	private boolean 			isLengthFilterEnabled	= false ;
	private Double 				lengthMin				= null ;
	private Double	 			lengthMax				= null ;
	private boolean 			isAgeFilterEnabled		= false ;
	private Integer 			ageMin					= null ;
	private Integer 			ageMax					= null ;
	private Predicate<Route>	compoundPredicate		= null ;
	private boolean 			isDistanceToClosest		= true ;


	@Override
	protected
	void
	clearCompoundPredicate( )
	{
		compoundPredicate = null ;			// Mark this singleton as dirty, to force recalculation.
	}


	// Length filter.
	public
	void
	enableLengthFilter( final Double lengthMin, final Double lengthMax )
	{
		setLengthFilterEnabled( true ) ;
		setLengthMin( lengthMin ) ;
		setLengthMax( lengthMax ) ;
	}


	public
	void
	disableLengthFilter( )
	{
		setLengthFilterEnabled( false ) ;
	}


	public
	boolean
	isLengthFilterEnabled( )
	{
		return isLengthFilterEnabled ;
	}

	
	public
	Double
	getLengthMin( )
	{
		return lengthMin ;
	}


	public
	Double
	getLengthMax( )
	{
		return lengthMax ;
	}


	public
	boolean
	isDistanceToClosest( )
	{
		return isDistanceToClosest ;
	}


	public
	void
	setLengthFilterEnabled( final boolean isLengthFilterEnabled )
	{
		this.isLengthFilterEnabled = isLengthFilterEnabled ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceToClosest( final boolean isDistanceToClosest )
	{
		this.isDistanceToClosest = isDistanceToClosest ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setLengthMin( final Double lengthMin )
	{
		this.lengthMin = lengthMin ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setLengthMax( final Double lengthMax )
	{
		this.lengthMax = lengthMax ;
		clearCompoundPredicate( ) ;
	}


	// Age filter.
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


	public
	boolean
	isEnabled( )
	{
		return isLengthFilterEnabled || isAgeFilterEnabled || super.isEnabled( ) ;
	}


	@Override
	public
	boolean
	evaluate( final Route rte )
	{
		if (compoundPredicate == null)
		{
			Collection<Predicate<Route>> predicates = null ;

			if (isTypeFilterEnabled)
			{
				if (predicates == null)
					predicates = new HashSet<>() ;
				
				predicates.add( new RoutePredicate_TypeAny( acceptedTypes ) ) ;
			}

			if (isDistanceFilterEnabled  &&  (distanceMin != null  ||  distanceMax != null))
			{
				if (predicates == null)
					predicates = new HashSet<>() ;
				
				if (isDistanceToClosest)
				{
					if (isDistanceHorizontal)
						predicates.add( new RoutePredicate_HorizontalDistanceToPath( distanceRefLat, distanceRefLon, distanceMin, distanceMax ) ) ;
					else
						predicates.add( new RoutePredicate_DistanceToPath( distanceRefLat, distanceRefLon, distanceRefEle, distanceMin, distanceMax ) ) ;
				}
				else
				{
					if (isDistanceHorizontal)
						predicates.add( new RoutePredicate_HorizontalDistanceToStart( distanceRefLat, distanceRefLon, distanceMin, distanceMax ) ) ;
					else
						predicates.add( new RoutePredicate_DistanceToStart( distanceRefLat, distanceRefLon, distanceRefEle, distanceMin, distanceMax ) ) ;
				}
			}

			if (isLengthFilterEnabled  &&  (lengthMin != null  ||  lengthMax != null))
			{
				if (predicates == null)
					predicates = new HashSet<>() ;
				
				predicates.add( new RoutePredicate_LengthRange( lengthMin, lengthMax ) ) ;
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

				predicates.add( new RoutePredicate_TimeRange( dateMin, dateMax ) ) ;
			}

			if (predicates != null)
				compoundPredicate = AllPredicate.allPredicate( predicates ) ;
		}

		return (compoundPredicate == null) ? true : compoundPredicate.evaluate( rte ) ;
	}
}

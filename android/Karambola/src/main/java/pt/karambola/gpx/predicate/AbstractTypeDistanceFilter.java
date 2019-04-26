/*
 * AbstractTypeDistanceFilter.java
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

import java.util.ArrayList;
import java.util.List;

public abstract class
AbstractTypeDistanceFilter
	extends AbstractCompoundFilter
{
	protected boolean 				isTypeFilterEnabled		= false ;
	protected boolean 				isDistanceFilterEnabled	= false ;
	protected boolean 				isDistanceHorizontal	= false ;
	protected double 				distanceRefLat ;
	protected double 				distanceRefLon ;
	protected double 				distanceRefEle ;
	protected Double				distanceMin				= null ;
	protected Double				distanceMax				= null ;
	protected final List<String>	acceptedTypes			= new ArrayList<>( ) ;


	public
	void
	enableTypeFilter( final List<String> acceptedTypes )
	{
		setTypeFilterEnabled( true ) ;
		setAcceptedTypes( acceptedTypes ) ;
	}


	public
	void
	disableTypeFilter( )
	{
		setTypeFilterEnabled( false ) ;
	}


	public
	void
	enableDistanceFilter( final double refLat, final double refLon, final double refEle, final Double distanceMin, final Double distanceMax )
	{
		setDistanceFilterEnabled( true ) ;
		setDistanceHorizontal( false ) ;

		setDistanceRefLat( refLat ) ;
		setDistanceRefLon( refLon ) ;
		setDistanceRefEle( refEle ) ;
		setDistanceMin( distanceMin ) ;
		setDistanceMax( distanceMax ) ;
	}


	public
	void
	enableHorizontalDistanceFilter( final double refLat, final double refLon, final Double distanceMin, final Double distanceMax )
	{
		setDistanceFilterEnabled( true ) ;
		setDistanceHorizontal( true ) ;

		setDistanceRefLat( refLat ) ;
		setDistanceRefLon( refLon ) ;
		setDistanceMin( distanceMin ) ;
		setDistanceMax( distanceMax ) ;
	}


	public
	void
	disableDistanceFilter( )
	{
		setDistanceFilterEnabled( false ) ;
	}


	/** === SETTERS === **/

	public
	void
	clearAcceptedTypes( )
	{
		acceptedTypes.clear( ) ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setTypeFilterEnabled( final boolean isTypeFilterEnabled )
	{
		this.isTypeFilterEnabled = isTypeFilterEnabled ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceHorizontal( final boolean isDistanceHorizontal )
	{
		this.isDistanceHorizontal = isDistanceHorizontal ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceFilterEnabled( final boolean isDistanceFilterEnabled )
	{
		this.isDistanceFilterEnabled = isDistanceFilterEnabled ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceRefLat( final double distanceRefLat )
	{
		this.distanceRefLat = distanceRefLat ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceRefLon( final double distanceRefLon )
	{
		this.distanceRefLon = distanceRefLon ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceRefEle( final double distanceRefEle )
	{
		this.distanceRefEle = distanceRefEle ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceMin( final Double distanceMin )
	{
		this.distanceMin = distanceMin ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setDistanceMax( final Double distanceMax )
	{
		this.distanceMax = distanceMax ;
		clearCompoundPredicate( ) ;
	}


	public
	void
	setAcceptedTypes( final List<String> acceptedTypes )
	{
		clearAcceptedTypes( ) ;
		this.acceptedTypes.addAll( acceptedTypes ) ;
		clearCompoundPredicate( ) ;
	}


	/** === GETTERS === **/

	public
	double
	getDistanceRefLat( )
	{
		return distanceRefLat ;
	}


	public
	double
	getDistanceRefLon( )
	{
		return distanceRefLon ;
	}


	public
	double
	getDistanceRefEle( )
	{
		return distanceRefEle ;
	}


	public
	boolean
	isDistanceHorizontal( )
	{
		return isDistanceHorizontal ;
	}

	
	public
	boolean
	isTypeFilterEnabled( )
	{
		return isTypeFilterEnabled ;
	}


	public
	boolean
	isDistanceFilterEnabled( )
	{
		return isDistanceFilterEnabled ;
	}


	public
	boolean
	isEnabled( )
	{
		return isDistanceFilterEnabled || isTypeFilterEnabled ;
	}


	public
	List<String>
	getAcceptedTypes( )
	{
		return acceptedTypes ;
	}


	public
	Double
	getDistanceMin( )
	{
		return distanceMin ;
	}


	public
	Double
	getDistanceMax( )
	{
		return distanceMax ;
	}
}

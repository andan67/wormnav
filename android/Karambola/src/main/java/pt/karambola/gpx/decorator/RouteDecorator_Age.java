/*
 * RouteDecorator_Age.java
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


package pt.karambola.gpx.decorator;

import java.util.Date;

import pt.karambola.commons.util.StringDecorator;
import pt.karambola.gpx.beans.Route;

public class
RouteDecorator_Age
	implements StringDecorator<Route>
{
	private static final long MSINADAY = 1000 * 60 * 60 * 24 ;
	private final String day ;
	private final String days ;

	public
	RouteDecorator_Age( final String day, final String days )
	{
		this.day  = day ;
		this.days = days ;
	}

	public
	RouteDecorator_Age( )
	{
		this( "day", "days" ) ;
	}

	@Override
	public
	String
	getStringDecoration( Route rte )
	{
		Date ptTime = rte.getRoutePoints( ).get( 0 ).getTime( ) ;
		
		if (ptTime == null )
			return "?" ;

		Date now = new Date( ) ;
		Long ageInDays = (now.getTime() - ptTime.getTime()) / MSINADAY ;
		return ageInDays.toString( ) + " " + (ageInDays == 1 ? day : days) ;
	}
}

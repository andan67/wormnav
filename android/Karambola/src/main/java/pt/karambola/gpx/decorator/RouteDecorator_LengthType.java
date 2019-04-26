/*
 * RouteDecorator_LengthType.java
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

import pt.karambola.geo.Units;
import pt.karambola.gpx.beans.Route;


public class
RouteDecorator_LengthType
	extends RouteDecorator_Length
{
	public
	RouteDecorator_LengthType( Units units )
	{
		super( units ) ;
	}


	@Override
	public
	String
	getStringDecoration( Route rte )
	{
		String result = super.getStringDecoration( rte ) ;

		if (rte.getType() != null)
			result += ", " + rte.getType() ;

		return result ;
	}
}

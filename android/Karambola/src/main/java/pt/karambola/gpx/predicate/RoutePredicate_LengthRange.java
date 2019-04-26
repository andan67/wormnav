/*
 * RoutePredicate_LengthRange.java
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

import pt.karambola.commons.collections.Predicate;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.util.GpxUtils;

public class
RoutePredicate_LengthRange
	implements Predicate<Route>
{
	private Double minLength ;
	private Double maxLength ;

	public
	RoutePredicate_LengthRange( Double minLength, Double maxLength )
	{
		this.minLength = minLength ;
		this.maxLength = maxLength ;
	}

	public
	boolean
	evaluate( Route rte )
	{
		if (this.minLength == null  &&  this.maxLength == null)  return true ;
		double rteLength = GpxUtils.lengthOfRoute( rte ) ;
		
		if (this.minLength != null  &&  rteLength < this.minLength)  return false ;
		if (this.maxLength != null  &&  rteLength > this.maxLength)  return false ;

		return true ;
	}
}

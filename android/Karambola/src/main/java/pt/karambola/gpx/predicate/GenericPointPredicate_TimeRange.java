/*
 * GenericPointPredicate_TimeRange.java
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

import java.util.Date;

import pt.karambola.commons.collections.Predicate;
import pt.karambola.gpx.beans.GenericPoint;

public class
GenericPointPredicate_TimeRange
	implements Predicate<GenericPoint>
{
	private final Date	timeMin, timeMax ;

	public
	GenericPointPredicate_TimeRange( Date timeMin, Date timeMax )
	{
		this.timeMin = timeMin ;
		this.timeMax = timeMax ;
	}

	@Override
	public
	boolean
	evaluate( GenericPoint gpt )
	{
		Date time = gpt.getTime() ;

		if (time == null)  return (this.timeMin == null  &&  this.timeMax == null) ;				// To be able to catch the "untimed" ones.
		if (this.timeMin != null  &&  time.before( this.timeMin ))  return false ;
		if (this.timeMax != null  &&  time.after( this.timeMax ))   return false ;

		return true ;
	}
}

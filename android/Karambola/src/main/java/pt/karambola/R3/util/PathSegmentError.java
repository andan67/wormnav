/*
 * PathSegmentError.java
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

class
PathSegmentError
{
	final int	firstPointIdx ;
	final int	lastPointIdx ;
	double		maxError ;
	int			maxErrorPointIdx ;

	public
	PathSegmentError( final int firstPointIdx, final int lastPointIdx )
	{
		this.firstPointIdx	= firstPointIdx ;
		this.lastPointIdx	= lastPointIdx ;
		maxError            = 0.0 ;
		maxErrorPointIdx	= -1 ;
	}


    /**
     * Returns a String representation of this segment.
     */
    @Override
    public
    String
    toString( )
    {
		return new StringBuffer()
				   .append( firstPointIdx )
				   .append( "|" )
				   .append( lastPointIdx )
		           .toString()
		           ;
    }
}

/*
 * PathSimplifierResult.java
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

public class
PathSimplifierResult
{
	public final int    requestedMaxSegments;		// PK
	public final double requestedMaxError ;			// PK
	public final int[]  pointsIdxs ;
	public final double error ;

	public
	PathSimplifierResult( final int		requestedMaxSegments
					    , final double	requestedMaxError
			            , final int[]	pointsIdxs
			            , final double	error
			            )
	{
		this.requestedMaxSegments = requestedMaxSegments ;
		this.requestedMaxError	  = requestedMaxError ;
		this.pointsIdxs			  = pointsIdxs ;
		this.error      		  = error ;
	}


    /**
     * Returns a String representation of this result.
     */
    @Override
    public
    String
    toString( )
    {
		return new StringBuffer()
				   .append(requestedMaxSegments)
				   .append( "|" )
				   .append( requestedMaxError )
		           .toString()
		           ;
    }
}

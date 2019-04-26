/*
 * GpxParserOptions.java
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


package pt.karambola.gpx.parser;

public
class
GpxParserOptions
{
	public final boolean	skipPoints ;
	public final boolean	skipRoutes ;
	public final boolean	skipTracks ;

	public static final GpxParserOptions PARSE_ALL		= new GpxParserOptions( false, false, false ) ;
	public static final GpxParserOptions SKIP_POINTS	= new GpxParserOptions( true , false, false ) ;
	public static final GpxParserOptions SKIP_ROUTES	= new GpxParserOptions( false, true , false ) ;
	public static final GpxParserOptions SKIP_TRACKS	= new GpxParserOptions( false, false, true  ) ;
	public static final GpxParserOptions ONLY_POINTS 	= new GpxParserOptions( false, true , true  ) ;
	public static final GpxParserOptions ONLY_ROUTES	= new GpxParserOptions( true , false, true  ) ;
	public static final GpxParserOptions ONLY_TRACKS	= new GpxParserOptions( true , true , false ) ;

	public
	GpxParserOptions( final boolean skipPoints, final boolean skipRoutes, final boolean skipTracks )
	{
		this.skipPoints = skipPoints ;
		this.skipRoutes = skipRoutes ;
		this.skipTracks = skipTracks ;
	}
}

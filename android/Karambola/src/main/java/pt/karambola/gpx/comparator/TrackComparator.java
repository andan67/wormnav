/*
 * TrackComparator.java
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


package pt.karambola.gpx.comparator;

import java.util.Comparator;

import pt.karambola.gpx.beans.Track;


public class
TrackComparator
{
	public static final Comparator<Track> HORIZONTALLENGTH		= new TrackComparator_HorizontalLength( ) ;
	public static final Comparator<Track> LENGTH				= new TrackComparator_Length( ) ;
	public static final Comparator<Track> NAME					= new TrackComparator_Name( ) ;
	public static final Comparator<Track> TIME_OLDER2YOUNGER	= new TrackComparator_Time_Older2Younger( ) ;
	public static final Comparator<Track> TIME_YOUNGER2OLDER	= new TrackComparator_Time_Younger2Older( ) ;
	public static final Comparator<Track> TYPE					= new TrackComparator_Type( ) ;
}

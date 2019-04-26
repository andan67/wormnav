/*
 * GpxConstants.java
 * 
 * Copyright (c) 2012 AlternativeVision, 2016 Karambola. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package pt.karambola.gpx.parser;

public interface
GpxConstants
{
	/*GPX nodes and attributes*/
	public static final String NODE_GPX			= "gpx";
	public static final String NODE_WPT			= "wpt";
	public static final String NODE_RTE			= "rte";
	public static final String NODE_TRK			= "trk";
	public static final String ATTR_VERSION		= "version";
	public static final String ATTR_CREATOR		= "creator";
	/*End GPX nodes and attributes*/
	
	/*Waypoint nodes and attributes*/
	public static final String ATTR_LAT         = "lat";
	public static final String ATTR_LON         = "lon";
	public static final String NODE_ELE         = "ele";
	public static final String NODE_TIME        = "time";
	public static final String NODE_NAME        = "name";
	public static final String NODE_CMT         = "cmt";
	public static final String NODE_DESC        = "desc";
	public static final String NODE_SRC         = "src";
	public static final String NODE_MAGVAR      = "magvar";
	public static final String NODE_GEOIDHEIGHT	= "geoidheight";
	public static final String NODE_LINK        = "link";
	public static final String NODE_SYM         = "sym";
	public static final String NODE_TYPE        = "type";
	public static final String NODE_FIX         = "fix";
	public static final String NODE_SAT         = "sat";
	public static final String NODE_HDOP        = "hdop";
	public static final String NODE_VDOP        = "vdop";
	public static final String NODE_PDOP        = "pdop";
	public static final String NODE_AGEOFGPSDATA= "ageofdgpsdata";
	public static final String NODE_DGPSID      = "dgpsid";
	public static final String NODE_EXTENSIONS  = "extensions";
	/*End Waypoint nodes and attributes*/
	
	/*Route Nodes*/
	public static final String NODE_RTEPT 		= "rtept";
	/*End route nodes*/
	
	/*Track nodes and attributes*/
	public static final String NODE_NUMBER 		= "number";
	public static final String NODE_TRKSEG 		= "trkseg";
	public static final String NODE_TRKPT  		= "trkpt";
	/*End Track nodes and attributes*/
}

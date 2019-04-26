/*
 * AbstractGpxExtensionParser.java
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackSegment;
import pt.karambola.gpx.beans.GenericPoint;

public abstract class
AbstractGpxExtensionParser
	extends		GpxScalarNodeParser
	implements	GpxExtensionParser	
{
	protected String extensionId = null ;				// To be set by each call of individual methods.

	public
	String
	getId( )
	{
		return this.extensionId ;
	}

	public
	Object
	parseGpxExtension( Node node )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
		return null ;
	}

	public
	Object
	parseGenericPointExtension( Node node )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
		return null ;
	}
	
	public
	Object
	parseRouteExtension( Node node )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
		return null ;
	}

	public
	Object
	parseTrackExtension( Node node )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
		return null ;
	}

	public
	Object
	parseTrackSegmentExtension( Node node )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
		return null ;
	}

	public
	void
	writeGpxExtension( Node node, Gpx wpt, Document doc )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
	}

	public
	void
	writeGenericPointExtension( Node node, GenericPoint wpt, Document doc )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
	}

	public
	void
	writeRouteExtension( Node node, Route rte, Document doc )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
	}

	public
	void
	writeTrackExtension( Node node, Track trk, Document doc )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
	}

	public
	void
	writeTrackSegmentExtension( Node node, TrackSegment trkSeg, Document doc )
	{
		this.extensionId = null ;		// Overriding methods on sub-classes should assign proper value here.
	}
}

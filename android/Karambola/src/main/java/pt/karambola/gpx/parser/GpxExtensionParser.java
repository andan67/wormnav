/*
 * GpxExtensionParser.java
 * 
 * Copyright (c) 2012, AlternativeVision. All rights reserved.
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
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackSegment;

/**
 * This interface defines extension parsers methods. 
 * <br>
 * <p>All custom extension parser must implement this interface.</p>
 * <p>Any custom parser must be added to {@link pt.karambola.gpx.parser.GpxParser} as an extension parser 
 * before parsing a gpx file, or writing a {@link Gpx} to a file. This is done by
 * calling addExtensionParser() method of {@link pt.karambola.gpx.parser.GpxParser}
 * <p>{@link pt.karambola.gpx.parser.GpxParser} parseGPX method calls several methods from the registered 
 * extension parsers added at different steps of processing:</p>
 * <ul>
 * <li>parseGPXExtension() for parsing &lt;extensions&gt;  of a &lt;gpx&gt; node</li>
 * <li>parseTrackExtension() for parsing &lt;extensions&gt; of a &lt;trk&gt; node</li>
 * <li>parseRouteExtension() for parsing &lt;extensions&gt; of a &lt;rte&gt; node</li>
 * <li>parseGenericPointExtension() for parsing &lt;extensions&gt; of a &lt;wpt&gt; node</li>
 * </ul>
 * <br>
 * 
 * <p>{@link pt.karambola.gpx.parser.GpxParser} writeGPX method also calls several methods from the registered 
 * extensions parsers at different steps of writing data:</p>
 * <ul>
 * <li>writeGPXExtensionData() when writing  the &lt;extensions&gt;  from the {@link Gpx}</li>
 * <li>writeTrackExtensionData() when writing  the &lt;extensions&gt;  from the {@link Track}</li>
 * <li>writeRouteExtensionData() when writing  the &lt;extensions&gt;  from the {@link Route}</li>
 * <li>writeGenericPointExtensionData() when writing  the &lt;extensions&gt;  from the {@link GenericPoint}</li>
 * </ul> 
 */
public interface
GpxExtensionParser
{
	public String getId( ) ;

	public Object parseGpxExtension(Node node) ;

	public Object parseGenericPointExtension(Node node) ;

	public Object parseRouteExtension(Node node) ;

	public Object parseTrackExtension(Node node) ;

	public Object parseTrackSegmentExtension(Node node) ;

	public void writeGpxExtension(Node node, Gpx gpx, Document doc) ;

	public void writeGenericPointExtension(Node node, GenericPoint gpt, Document doc) ;

	public void writeRouteExtension(Node node, Route rte, Document doc) ;

	public void writeTrackExtension(Node node, Track trk, Document doc) ;

	public void writeTrackSegmentExtension(Node node, TrackSegment trkSeg, Document doc) ;
}

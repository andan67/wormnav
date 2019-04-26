/*
 * GpxParser.java
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.beans.TrackSegment;
import pt.karambola.gpx.beans.GenericPoint;


/**
 * <p>
 * This class defines methods for parsing and writing gpx files.
 * </p>
 * <br>
 * Usage for parsing a gpx file into a {@link Gpx} object:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileInputStream in = new FileInputStream("inFile.gpx");<br>
 * GPX gpx = p.parseGPX(in);<br>
 * </code> <br>
 * Usage for writing a {@link Gpx} object to a file:<br>
 * <code>
 * GPXParser p = new GPXParser();<br>
 * FileOutputStream out = new FileOutputStream("outFile.gpx");<br>
 * p.writeGPX(gpx, out);<br>
 * out.close();<br>
 * </code>
 */
public class
GpxParser
	extends GpxScalarNodeParser
{
    private final List<GpxExtensionParser>	extensionParsers = new ArrayList<GpxExtensionParser>( ) ;
    
    private final GpxBeanFactory beanFactory ;


    public
    GpxParser( GpxBeanFactory beanFactory )
	{
		super( ) ;
		this.beanFactory = beanFactory ;
	}

    public
    GpxParser( )
	{
		this( DefaultGpxBeanFactory.INSTANCE ) ;
	}

    
    /**
     * Adds a new extension parser to be used when parsing a gpx stream
     *
     * @param parser
     *            an instance of a {@link GpxExtensionParser} implementation
     */
    public void
    addExtensionParser( GpxExtensionParser parser )
    {
    	this.extensionParsers.add( parser ) ;
    }

	/**
     * Removes an extension parser previously added
     *
     * @param parser
     *            an instance of a {@link GpxExtensionParser} implementation
     */
    public void
    removeExtensionParser( GpxExtensionParser parser )
    {
    	this.extensionParsers.remove( parser ) ;
    }

    /**
     * Parses a stream containing GPX data
     *
     * @param in
     *            the input stream
     * @return {@link Gpx} object containing parsed data, or null if no gpx data
     *         was found in the stream
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public
    Gpx
    parseGpx( InputStream in, GpxParserOptions options )
	    throws ParserConfigurationException, SAXException, IOException
    {
		DocumentBuilder builder    = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( ) ;
		Document        doc        = builder.parse( in ) ;
		Node            firstChild = doc.getFirstChild( ) ;
	
		if (firstChild == null  ||  !GpxConstants.NODE_GPX.equals( firstChild.getNodeName( ) )) return null ;	// Not in GPX format.
	
		NamedNodeMap attrs = firstChild.getAttributes( );

		Gpx  gpx = this.beanFactory.newGpx( ) ;

		if (gpx != null)
		{
			for ( int idx = 0 ; idx < attrs.getLength( ) ; idx++ )
			{
			    Node   attr = attrs.item( idx ) ;

				switch (attr.getNodeName( ))
				{
			        case GpxConstants.ATTR_VERSION:
			        	gpx.setVersion( attr.getNodeValue( ) ) ;
						break ;

			        case GpxConstants.ATTR_CREATOR:
			        	gpx.setCreator( attr.getNodeValue( ) ) ;
						break ;
				}
			}

			if (gpx.getVersion( ) == null  ||  gpx.getCreator( ) == null)  return null ;				// Not in GPX format: VERSION and CREATOR attributes are mandatory.

			NodeList nodes = firstChild.getChildNodes( ) ;

			for ( int idx = 0 ; idx < nodes.getLength( ) ; idx++ )
			{
			    Node   currentNode = nodes.item( idx ) ;

				switch (currentNode.getNodeName( ))
				{
			        case GpxConstants.NODE_WPT:
					    if (!options.skipPoints)	gpx.addPoint( parsePoint( currentNode ) ) ;
						break ;

			        case GpxConstants.NODE_RTE:
					    if (!options.skipRoutes)	gpx.addRoute( parseRoute( currentNode ) ) ;
						break ;

			        case GpxConstants.NODE_TRK:
					    if (!options.skipTracks)	gpx.addTrack( parseTrack( currentNode ) ) ;
						break ;

			        case GpxConstants.NODE_EXTENSIONS:
						for ( GpxExtensionParser extensionParser: this.extensionParsers )
						{
							Object extensionData = extensionParser.parseGpxExtension( currentNode ) ;
						    gpx.setExtensionData( extensionParser.getId( ), extensionData ) ;
						}

						break ;
				}
			}
		}

		gpx.resetIsChanged( ) ;

		return gpx ;
    }


    public
    Gpx
    parseGpx( InputStream in )
	    throws ParserConfigurationException, SAXException, IOException
    {
    	return parseGpx( in, GpxParserOptions.PARSE_ALL ) ;
    }



    /**
     * Parses a wpt node into a Point object
     *
     * @param node
     * @return Point object with info from the received node
     */
    private
    Point
    parsePoint( Node node )
    {
    	return (Point)parseGenericPoint( this.beanFactory.newPoint( ), node ) ;

    }


    /**
     * Parses a rtePt node into a RoutePoint object
     *
     * @param node
     * @return RoutePoint object with info from the received node
     */
    private
    RoutePoint
    parseRoutePoint( Node node )
    {
    	return (RoutePoint)parseGenericPoint( this.beanFactory.newRoutePoint( ), node ) ;
    }


    /**
     * Parses a rtePt node into a TrackPoint object
     *
     * @param node
     * @return TrackPoint object with info from the received node
     */
    private
    TrackPoint
    parseTrackPoint( Node node )
    {
    	return (TrackPoint)parseGenericPoint( this.beanFactory.newTrackPoint( ), node ) ;
    }


    /**
     * Parses a wpt node into a Waypoint object
     *
     * @param node
     * @return Waypoint object with info from the received node
     */
    private
    GenericPoint
    parseGenericPoint( GenericPoint gpt, Node node )
    {
		if (gpt == null  ||  node == null) return null;
	
		NamedNodeMap attrs = node.getAttributes( );
	
		// check for lat attribute
		Node latNode = attrs.getNamedItem( GpxConstants.ATTR_LAT );
	
		if (latNode != null)
		{
		    Double latVal = null;
	
		    try
		    { latVal = Double.valueOf( latNode.getNodeValue( ) ); }
		    catch (NumberFormatException ex)
		    { }
	
		    gpt.setLatitude( latVal );
		}
	
		// check for lon attribute
		Node lonNode = attrs.getNamedItem( GpxConstants.ATTR_LON );
	
		if (lonNode != null)
		{
		    Double lonVal = null;
		    try
		    { lonVal = Double.valueOf( lonNode.getNodeValue( ) ); }
		    catch (NumberFormatException ex)
		    { }
	
		    gpt.setLongitude( lonVal );
		}

		if (gpt.getLatitude( ) == null  ||  gpt.getLongitude( ) == null)  return null ;					// Invalid waypoint: latitude and longitude are mandatory.

		NodeList childNodes = node.getChildNodes( );
	
	    for ( int idx = 0 ; idx < childNodes.getLength( ) ; idx++ )
	    {
			Node   currentNode     = childNodes.item( idx ) ;

			switch (currentNode.getNodeName( ))
			{
				case GpxConstants.NODE_ELE:
		        	gpt.setElevation( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_TIME:
		        	gpt.setTime( getNodeValueAsDate( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_NAME:
		        	gpt.setName( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_CMT:
		        	gpt.setComment( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_DESC:
		        	gpt.setDescription( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_SRC:
		        	gpt.setSrc( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_MAGVAR:
		        	gpt.setMagneticDeclination( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_GEOIDHEIGHT:
		        	gpt.setGeoidHeight( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_SYM:
		        	gpt.setSym( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_FIX:
		        	gpt.setFix( getNodeValueAsFixType( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_TYPE:
		        	gpt.setType( getNodeValueAsString( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_SAT:
		        	gpt.setSat( getNodeValueAsInteger( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_HDOP:
		        	gpt.setHdop( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_VDOP:
		        	gpt.setVdop( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_PDOP:
		        	gpt.setPdop( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_AGEOFGPSDATA:
		        	gpt.setAgeOfGpsData( getNodeValueAsDouble( currentNode ) ) ;
		            break ;

		        case GpxConstants.NODE_DGPSID:
		        	gpt.setDgpsid( getNodeValueAsInteger( currentNode ) ) ;
		        	break ;

		        case GpxConstants.NODE_LINK:
					// TODO: parse link
				    // wpt.setLink( getNodeValueAsLink( currentNode ) ) ;
					break ;

		        case GpxConstants.NODE_EXTENSIONS:
					for (GpxExtensionParser extensionParser: this.extensionParsers)
					{
						Object extensionData = extensionParser.parseGenericPointExtension( currentNode ) ;
						gpt.setExtensionData( extensionParser.getId( ), extensionData ) ;
					}

					break ;
		     }
	    }

		return gpt ;
    }


    private
    Route
    parseRoute( Node node )
    {
		if (node == null) return null ;

		NodeList nodes = node.getChildNodes( ) ;
		if (nodes.getLength( ) == 0)  return null ;

		Route rte = this.beanFactory.newRoute( ) ;

		if (rte != null)
		    for (int idx = 0 ; idx < nodes.getLength( ) ; idx++)
		    {
				Node   currentNode = nodes.item( idx ) ;
		
				switch (currentNode.getNodeName( ))
				{
			        case GpxConstants.NODE_NAME:
			        	rte.setName( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_CMT:
			        	rte.setComment( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_DESC:
			        	rte.setDescription( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_SRC:
			        	rte.setSrc( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_NUMBER:
			        	rte.setNumber( getNodeValueAsInteger( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_TYPE:
			        	rte.setType( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_RTEPT:
			        	rte.addRoutePoint( parseRoutePoint( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_LINK:
					    // TODO: parse link
					    // rte.setLink( getNodeValueAsLink( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_EXTENSIONS:
					    for (GpxExtensionParser extensionParser: this.extensionParsers)
						{
							Object extensionData = extensionParser.parseRouteExtension( currentNode ) ;
							rte.setExtensionData( extensionParser.getId( ), extensionData ) ;
						}
	
						break ;
			     }
		    }
	
		return rte;
    }


    private
    Track
    parseTrack( Node node )
    {
		if (node == null) return null;
	
		NodeList nodes = node.getChildNodes( ) ;
		if (nodes.getLength( ) == 0)  return null ;

		Track	trk = this.beanFactory.newTrack( ) ;

		if (trk != null)
		    for ( int idx = 0 ; idx < nodes.getLength( ) ; idx++ )
		    {
				Node   currentNode = nodes.item( idx ) ;
				
				switch (currentNode.getNodeName( ))
				{
			        case GpxConstants.NODE_NAME:
			        	trk.setName( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_CMT:
			        	trk.setComment( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_DESC:
			        	trk.setDescription( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_SRC:
			        	trk.setSrc( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_NUMBER:
			        	trk.setNumber( getNodeValueAsInteger( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_TYPE:
			        	trk.setType( getNodeValueAsString( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_TRKSEG:
			        	trk.addTrackSegment( parseTrackSegment( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_LINK:
					    // TODO: parse link
					    // trk.setLink(getNodeValueAsLink(currentNode));
			            break ;
	
			        case GpxConstants.NODE_EXTENSIONS:
					    for (GpxExtensionParser extensionParser: this.extensionParsers)
						{
							Object extensionData = extensionParser.parseTrackExtension( currentNode ) ;
							trk.setExtensionData( extensionParser.getId( ), extensionData ) ;
						}
	
						break ;
			     }
		    }

		return trk;
    }


    private
    TrackSegment
    parseTrackSegment( Node node )
    {
		if (node == null) return null ;

		NodeList nodes = node.getChildNodes( ) ;
		if (nodes.getLength( ) == 0)  return null ;

		TrackSegment	trkSeg = this.beanFactory.newTrackSegment( ) ;
	
		if (trkSeg != null)
		    for ( int idx = 0 ; idx < nodes.getLength( ) ; idx++ )
		    {
				Node   currentNode = nodes.item( idx ) ;
	
				switch (currentNode.getNodeName( ))
				{
			        case GpxConstants.NODE_TRKPT:
			        	trkSeg.addTrackPoint( parseTrackPoint( currentNode ) ) ;
			            break ;
	
			        case GpxConstants.NODE_EXTENSIONS:
			    		for ( GpxExtensionParser extensionParser: this.extensionParsers )
						{
							Object extensionData = extensionParser.parseTrackSegmentExtension( currentNode ) ;
							trkSeg.setExtensionData( extensionParser.getId( ), extensionData ) ;
						}
	
			    		break ;
			     }
		    }

		return trkSeg ;
    }


    public
    void
    writeGpx( Gpx gpx, OutputStream out, GpxParserOptions options )
	    throws ParserConfigurationException, TransformerException
    {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( );
		Document        doc     = builder.newDocument( );
		Node            gpxNode = doc.createElement( GpxConstants.NODE_GPX ) ;

		if (gpx.getCreator( ) == null)
			gpx.setCreator( this.getClass( ).getCanonicalName( ) + " (authors: 2012 AlternativeVision, 2016 Karambola)" ) ;

		if (gpx.getVersion( ) == null)
			gpx.setVersion( "1.1" ) ;

		addBasicGpxInfoToNode( gpx, gpxNode, doc );

		if (!options.skipPoints)
			for (Point pt : gpx.getPoints( ))
				addPointToGpxNode( pt , gpxNode, doc ) ;

		if (!options.skipRoutes)
			for (Route rte: gpx.getRoutes( ))
				addRouteToGpxNode( rte, gpxNode, doc ) ;

		if (!options.skipTracks)
			for (Track trk: gpx.getTracks( ))
				addTrackToGpxNode( trk, gpxNode, doc ) ;

		doc.appendChild( gpxNode ) ;
	
		// Use a Transformer for output
		Transformer transformer = TransformerFactory.newInstance( ).newTransformer( ) ;
		transformer.setOutputProperty( OutputKeys.INDENT, "yes" ) ;

		DOMSource source = new DOMSource( doc ) ;
		StreamResult result = new StreamResult( out ) ;
		transformer.transform( source, result ) ;
    }


    public
    void
    writeGpx( Gpx gpx, OutputStream out )
	    throws ParserConfigurationException, TransformerException
    {
    	writeGpx( gpx, out, GpxParserOptions.PARSE_ALL ) ;
    }

	
    private void
    addPointToGpxNode( Point pt, Node gpxNode, Document doc )
    {
    	addGenericPointToGpxNode( GpxConstants.NODE_WPT, pt, gpxNode, doc );
    }
	
    private void
    addGenericPointToGpxNode( String tagName, GenericPoint gpt, Node gpxNode, Document doc )
    {
		Node         gptNode = doc.createElement( tagName ) ;
		NamedNodeMap attrs   = gptNode.getAttributes( ) ;

		if (gpt.getLatitude( ) != null)
		{
		    Node node = doc.createAttribute( GpxConstants.ATTR_LAT ) ;
		    node.setNodeValue( gpt.getLatitude( ).toString( ) ) ;
		    attrs.setNamedItem( node ) ;
		}
	
		if (gpt.getLongitude( ) != null)
		{
		    Node node = doc.createAttribute( GpxConstants.ATTR_LON ) ;
		    node.setNodeValue( gpt.getLongitude( ).toString( ) ) ;
		    attrs.setNamedItem( node ) ;
		}
	
		if (gpt.getElevation( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_ELE );
		    node.appendChild( doc.createTextNode( gpt.getElevation( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getTime( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_TIME ) ;
		    node.appendChild( doc.createTextNode( dateFormat.format( gpt.getTime( ) ) ) ) ;
		    gptNode.appendChild( node ) ;
		}
	
		if (gpt.getMagneticDeclination( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_MAGVAR );
		    node.appendChild( doc.createTextNode( gpt.getMagneticDeclination( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getGeoidHeight( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_GEOIDHEIGHT );
		    node.appendChild( doc.createTextNode( gpt.getGeoidHeight( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getName( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_NAME );
		    node.appendChild( doc.createTextNode( gpt.getName( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getComment( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_CMT );
		    node.appendChild( doc.createTextNode( gpt.getComment( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getDescription( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_DESC );
		    node.appendChild( doc.createTextNode( gpt.getDescription( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getSrc( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_SRC );
		    node.appendChild( doc.createTextNode( gpt.getSrc( ) ) );
		    gptNode.appendChild( node );
		}
	
		// TODO: write link node
	
		if (gpt.getSym( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_SYM );
		    node.appendChild( doc.createTextNode( gpt.getSym( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getType( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_TYPE );
		    node.appendChild( doc.createTextNode( gpt.getType( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getFix( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_FIX );
		    node.appendChild( doc.createTextNode( gpt.getFix( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getSat( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_SAT );
		    node.appendChild( doc.createTextNode( gpt.getSat( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getHdop( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_HDOP );
		    node.appendChild( doc.createTextNode( gpt.getHdop( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getVdop( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_VDOP );
		    node.appendChild( doc.createTextNode( gpt.getVdop( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getPdop( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_PDOP );
		    node.appendChild( doc.createTextNode( gpt.getPdop( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getAgeOfGpsData( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_AGEOFGPSDATA );
		    node.appendChild( doc.createTextNode( gpt.getAgeOfGpsData( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getDgpsid( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_DGPSID );
		    node.appendChild( doc.createTextNode( gpt.getDgpsid( ).toString( ) ) );
		    gptNode.appendChild( node );
		}
	
		if (gpt.getExtensionsParsed( ) > 0)
		{
		    Node node = doc.createElement( GpxConstants.NODE_EXTENSIONS );
			for ( GpxExtensionParser extensionParser: this.extensionParsers )  extensionParser.writeGenericPointExtension( node, gpt, doc ) ;
		    gptNode.appendChild( node ) ;
		}
	
		gpxNode.appendChild( gptNode );
    }

    private void
    addTrackToGpxNode( Track trk, Node gpxNode, Document doc )
    {
		Node trkNode = doc.createElement( GpxConstants.NODE_TRK ) ;
	
		if (trk.getName( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_NAME );
		    node.appendChild( doc.createTextNode( trk.getName( ) ) );
		    trkNode.appendChild( node );
		}
	
		if (trk.getComment( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_CMT );
		    node.appendChild( doc.createTextNode( trk.getComment( ) ) );
		    trkNode.appendChild( node );
		}
	
		if (trk.getDescription( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_DESC );
		    node.appendChild( doc.createTextNode( trk.getDescription( ) ) );
		    trkNode.appendChild( node );
		}
	
		if (trk.getSrc( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_SRC );
		    node.appendChild( doc.createTextNode( trk.getSrc( ) ) );
		    trkNode.appendChild( node );
		}
	
		// TODO: write link
	
		if (trk.getNumber( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_NUMBER );
		    node.appendChild( doc.createTextNode( trk.getNumber( ).toString( ) ) );
		    trkNode.appendChild( node );
		}
	
		if (trk.getType( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_TYPE );
		    node.appendChild( doc.createTextNode( trk.getType( ) ) );
		    trkNode.appendChild( node );
		}
	
		if (trk.getExtensionsParsed( ) > 0)
		{
		    Node node = doc.createElement( GpxConstants.NODE_EXTENSIONS ) ;

			for ( GpxExtensionParser extensionParser: this.extensionParsers )
				extensionParser.writeTrackExtension( node, trk, doc ) ;

			trkNode.appendChild( node );
		}

	    for (TrackSegment trkSeg: trk.getTrackSegments( ))  addTrackSegmentToGpxNode( trkSeg, trkNode, doc ) ;

		gpxNode.appendChild( trkNode ) ;
    }

    private
    void
    addTrackSegmentToGpxNode( TrackSegment trkSeg, Node gpxNode, Document doc )
	{
		Node trksegNode = doc.createElement( GpxConstants.NODE_TRKSEG ) ;
		for (TrackPoint trkPt: trkSeg.getTrackPoints( ))  addGenericPointToGpxNode( GpxConstants.NODE_TRKPT, trkPt, trksegNode, doc ) ;
		gpxNode.appendChild( trksegNode ) ;
	}

	private
	void
    addRouteToGpxNode( Route rte, Node gpxNode, Document doc )
    {
		Node rteNode = doc.createElement( GpxConstants.NODE_RTE ) ;

		if (rte.getName( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_NAME );
		    node.appendChild( doc.createTextNode( rte.getName( ) ) );
		    rteNode.appendChild( node );
		}

		if (rte.getComment( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_CMT );
		    node.appendChild( doc.createTextNode( rte.getComment( ) ) );
		    rteNode.appendChild( node );
		}

		if (rte.getDescription( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_DESC );
		    node.appendChild( doc.createTextNode( rte.getDescription( ) ) );
		    rteNode.appendChild( node );
		}

		if (rte.getSrc( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_SRC );
		    node.appendChild( doc.createTextNode( rte.getSrc( ) ) );
		    rteNode.appendChild( node );
		}

		// TODO: write link
	
		if (rte.getNumber( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_NUMBER );
		    node.appendChild( doc.createTextNode( rte.getNumber( ).toString( ) ) );
		    rteNode.appendChild( node );
		}
	
		if (rte.getType( ) != null)
		{
		    Node node = doc.createElement( GpxConstants.NODE_TYPE );
		    node.appendChild( doc.createTextNode( rte.getType( ) ) );
		    rteNode.appendChild( node );
		}
	
		if (rte.getExtensionsParsed( ) > 0)
		{
		    Node node = doc.createElement( GpxConstants.NODE_EXTENSIONS );
			for ( GpxExtensionParser extensionParser: this.extensionParsers )  extensionParser.writeRouteExtension( node, rte, doc ) ;
		    rteNode.appendChild( node ) ;
		}

		for (RoutePoint rtePt: rte.getRoutePoints( ))
			addGenericPointToGpxNode( GpxConstants.NODE_RTEPT, rtePt, rteNode, doc ) ;
	
		gpxNode.appendChild( rteNode ) ;
    }


    private
    void
    addBasicGpxInfoToNode( Gpx gpx, Node gpxNode, Document doc )
    {
		NamedNodeMap attrs = gpxNode.getAttributes( );
	
		if (gpx.getVersion( ) != null)
		{
		    Node verNode = doc.createAttribute( GpxConstants.ATTR_VERSION );
		    verNode.setNodeValue( gpx.getVersion( ) );
		    attrs.setNamedItem( verNode );
		}
	
		if (gpx.getCreator( ) != null)
		{
		    Node creatorNode = doc.createAttribute( GpxConstants.ATTR_CREATOR );
		    creatorNode.setNodeValue( gpx.getCreator( ) );
		    attrs.setNamedItem( creatorNode );
		}
	
		if (gpx.getExtensionsParsed( ) > 0)
		{
		    Node node = doc.createElement( GpxConstants.NODE_EXTENSIONS ) ;
			for ( GpxExtensionParser extensionParser: this.extensionParsers )  extensionParser.writeGpxExtension( node, gpx, doc ) ;
		    gpxNode.appendChild( node );
		}
    }
}

/*
 * GpxStreamIo.java
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


package pt.karambola.gpx.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.parser.GpxParser;
import pt.karambola.gpx.parser.GpxParserOptions;


public abstract class
GpxStreamIo
{
	public static
    Gpx
    parseIn( final GpxParser parser, final InputStream is, final GpxParserOptions options )
    {
		Gpx	gpx = null ;

		try
		{ gpx = parser.parseGpx( is, options ) ; }
		catch (ParserConfigurationException e)
		{ e.printStackTrace( ) ; }
		catch (SAXException e)
		{ e.printStackTrace( ) ; }
		catch (IOException e)
		{ e.printStackTrace( ) ; }
	
		return gpx ;
	}


	public static
    Gpx
    parseIn( final GpxParser parser, final InputStream is )
    {
		return parseIn( parser, is, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    Gpx
    parseIn( final InputStream is, final GpxParserOptions options )
    {
		return parseIn( new GpxParser( ), is, options ) ;
	}


	public static
    Gpx
    parseIn( final InputStream is )
    {
		return parseIn( is, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final OutputStream os, final GpxParserOptions options )
    {
		try
		{ parser.writeGpx( gpx, os, options ) ; }
		catch (ParserConfigurationException e)
		{ e.printStackTrace( ) ; }
		catch (TransformerException e)
		{ e.printStackTrace( ) ; }
    }


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final OutputStream os )
    {
	    parseOut( parser, gpx, os, GpxParserOptions.PARSE_ALL ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final OutputStream os, final GpxParserOptions options )
    {
		parseOut( new GpxParser( ), gpx, os, options ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final OutputStream os )
    {
		parseOut( gpx, os, GpxParserOptions.PARSE_ALL ) ;
    }
}

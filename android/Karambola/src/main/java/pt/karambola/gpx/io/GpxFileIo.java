/*
 * GpxFileIo.java
 * 
 * Copyright (c) 2016 Karambola. All rights reserved.
 *
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.parser.GpxParser;
import pt.karambola.gpx.parser.GpxParserOptions;


public abstract class
GpxFileIo
	extends GpxStreamIo
{
	public static
    Gpx
    parseIn( final GpxParser parser, final String fileName, final GpxParserOptions options )
    {
		FileInputStream fis = null ;
	
		try
		{ fis = new FileInputStream( fileName ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return null ;
		}

		return parseIn( parser, fis, options ) ;
	}


	public static
    Gpx
    parseIn( String fileName, GpxParserOptions options )
    {
		FileInputStream fis = null ;
	
		try
		{ fis = new FileInputStream( fileName ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return null ;
		}

		return parseIn( fis, options ) ;
	}


	public static
    Gpx
    parseIn( final GpxParser parser, final String fileName )
    {
		return parseIn( parser, fileName, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    Gpx
    parseIn( final String fileName )
    {
		return parseIn( fileName, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    Gpx
    parseIn( final GpxParser parser, final File file, final GpxParserOptions options )
    {
		FileInputStream fis = null ;
	
		try
		{ fis = new FileInputStream( file ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return null ;
		}
	
		return parseIn( parser, fis, options ) ;
	}


	public static
    Gpx
    parseIn( final File file, final GpxParserOptions options )
    {
		FileInputStream fis = null ;
	
		try
		{ fis = new FileInputStream( file ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return null ;
		}
	
		return parseIn( fis, options ) ;
	}


	public static
    Gpx
    parseIn( final GpxParser parser, final File file )
    {
		return parseIn( parser, file, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    Gpx
    parseIn( final File file )
    {
		return parseIn( file, GpxParserOptions.PARSE_ALL ) ;
	}


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final String fileName, final GpxParserOptions options )
    {
		FileOutputStream fos = null ;
		
		try
		{ fos = new FileOutputStream( fileName ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return ;
		}

		parseOut( parser, gpx, fos, options ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final String fileName, final GpxParserOptions options )
    {
		FileOutputStream fos = null ;
		
		try
		{ fos = new FileOutputStream( fileName ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return ;
		}

		parseOut( gpx, fos, options ) ;
    }


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final String fileName )
    {
	    parseOut( parser, gpx, fileName, GpxParserOptions.PARSE_ALL ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final String fileName )
    {
	    parseOut( gpx, fileName, GpxParserOptions.PARSE_ALL ) ;
    }


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final File file, final GpxParserOptions options )
    {
		FileOutputStream fos = null ;
		
		try
		{ fos = new FileOutputStream( file ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return ;
		}

		parseOut( parser, gpx, fos, options ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final File file, final GpxParserOptions options )
    {
		FileOutputStream fos = null ;
		
		try
		{ fos = new FileOutputStream( file ) ; }
		catch (FileNotFoundException e)
		{
			e.printStackTrace( ) ;
			return ;
		}

		parseOut( gpx, fos, options ) ;
    }


	public static
    void
    parseOut( final GpxParser parser, final Gpx gpx, final File file )
    {
	    parseOut( parser, gpx, file, GpxParserOptions.PARSE_ALL ) ;
    }


	public static
    void
    parseOut( final Gpx gpx, final File file )
    {
	    parseOut( gpx, file, GpxParserOptions.PARSE_ALL ) ;
    }
}

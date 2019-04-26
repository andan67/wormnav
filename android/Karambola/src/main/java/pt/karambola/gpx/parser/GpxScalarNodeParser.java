/*
 * GpxScalarNodeParser.java
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


import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import pt.karambola.gpx.types.FixType;


public abstract class
GpxScalarNodeParser
{
	// 2012-02-25T09:28:45Z
	protected final static	SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'kk:mm:ssZ" ) ;

    protected
    Double
    getNodeValueAsDouble( Node node )
    {
		Double val = null;
	
		try
		{ val = Double.parseDouble( node.getFirstChild( ).getNodeValue( ) ); }
		catch (Exception ex)
		{ }
	
		return val ;
    }

    protected
    Date
    getNodeValueAsDate( Node node )
    {
		Date val = null;
	
		try
		{
		    val = dateFormat.parse( node.getFirstChild( ).getNodeValue( ) ) ;
		}
		catch (Exception ex)
		{ }
	
		return val;
    }

    protected
    String
    getNodeValueAsString( Node node )
    {
		String val = null ;
	
		try
		{ val = node.getFirstChild( ).getNodeValue( ) ; }
		catch (Exception ex)
		{ }
	
		return val ;
    }

    protected
    FixType
    getNodeValueAsFixType( Node node )
    {
		FixType val = null ;
	
		try
		{ val = FixType.returnType( node.getFirstChild( ).getNodeValue( ) ) ; }
		catch (Exception ex)
		{ }
	
		return val ;
    }

    protected
    Integer
    getNodeValueAsInteger( Node node )
    {
		Integer val = null ;
	
		try
		{ val = Integer.parseInt( node.getFirstChild( ).getNodeValue( ) ) ; }
		catch (Exception ex)
		{ }
	
		return val ;
    }
}

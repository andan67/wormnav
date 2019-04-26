/*
 * Extension.java
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

package pt.karambola.gpx.beans;

import java.util.HashMap;

/**
 * This class holds generic extension information from any node that can have extensions defined. 
 * <br>
 * <p>Any custom extension parser used when parsing a gpx file will return a custom object 
 * instance that will be kept in properties defined in this class.</p>
 * <br>
 * <p>Multiple extension parsers can be used when parsing. Every extension parser defines
 * an unique id that will be used as a HashMap key for the parsed object values.</p>
 */
public class
Extension
{
	protected HashMap<String, Object> extensionData ;
	protected boolean isChanged = false ;

	/**
	 * Adds a new parsed extension object into the extension data with the key set by parserId.
	 * 
	 * @param parserId a unique key representing the id of the extension parser used.
	 * @param data an object holding the parsed information. This can be any object type and it is
	 * 				the extension parser's job to set it properly.
	 */
	public
	void
	setExtensionData( String parserId, Object data )
	{
		if (parserId != null)
		{
			if (data == null)
			{
				if (this.extensionData != null)
				{
					this.extensionData.remove( parserId ) ;
					this.isChanged = true ;
				}
			}
			else
			{
				if (this.extensionData == null) this.extensionData = new HashMap<String, Object>( ) ;
				this.extensionData.put( parserId, data ) ;
				this.isChanged = true ;
			}
		}
	}


	/**
	 * Returns the extension data parsed by the extension parser with id <i>parserId</i>
	 * @param parserId a String representing the id of an extension parser
	 * @return the extension data parsed by the extension parser with id <i>parserId</i>
	 */
	public
	Object
	getExtensionData( String parserId )
	{
		return (this.extensionData != null) ? this.extensionData.get( parserId ) : null ;
	}
	
	/**
	 * Returns the number of extension data objects that are currently set.
	 * @return the number of extension data objects that are currently set.
	 */
	public
	int
	getExtensionsParsed( )
	{
		return (this.extensionData == null) ? 0 : this.extensionData.size( ) ;
	}


    public
    boolean
    isChanged( )
    {
    	return this.isChanged ;
    }


    public
    void
    resetIsChanged( )
    {
    	this.isChanged = false ;
    }
}

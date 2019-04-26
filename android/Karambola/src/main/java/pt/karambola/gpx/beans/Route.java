/*
 * Route.java
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

package pt.karambola.gpx.beans;

import java.util.ArrayList;
import java.util.List;

import pt.karambola.commons.collections.CollectionUtils;
import pt.karambola.commons.util.Named;
import pt.karambola.commons.util.Typed;

/**
 * This class holds route information from a &lt;rte&gt; node.
 * <br>
 * <p>GPX specification for this tag:</p>
 * <code>
 * &lt;rte&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;name&gt; xsd:string &lt;/name&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;cmt&gt; xsd:string &lt;/cmt&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;desc&gt; xsd:string &lt;/desc&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;src&gt; xsd:string &lt;/src&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;link&gt; linkType &lt;/link&gt; [0..*]<br>
 * &nbsp;&nbsp;&nbsp;&lt;number&gt; xsd:nonNegativeInteger &lt;/number&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;type&gt; xsd:string &lt;/type&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;extensions&gt; extensionsType &lt;/extensions&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;rtept&gt; wptType &lt;/rtept&gt; [0..*]<br>
 * &lt;/rte&gt;<br>
 *</code>
 */
public class
Route
	extends		Extension
	implements	Named, Typed
{
    private			String				name ;
    private			String				comment ;
    private			String				description ;
    private			String				src ;
    private			Integer				number ;
    private			String				type ;
    private final	List<RoutePoint>	routePoints = new ArrayList<>( ) ;

    /**
     * Returns the name of this route.
     * @return A String representing the name of this route.
     */
    @Override
	public
    String
    getName()
    {
    	return this.name ;
    }

    /**
     * Setter for route name property. This maps to &lt;name&gt; tag value.
     * @param name A String representing the name of this route.
     */
    public
    void
    setName( String name )
    {
    	this.name = name ;
		this.isChanged = true ;
    }

    /**
     * Returns the comment of this route.
     * @return A String representing the comment of this route.
     */
    public
    String
    getComment()
    {
    	return this.comment ;
    }

    /**
     * Setter for route comment property. This maps to &lt;comment&gt; tag value.
     * @param comment A String representing the comment of this route.
     */
    public
    void
    setComment( String comment )
    {
    	this.comment = comment ;
		this.isChanged = true ;
    }

    /**
     * Returns the description of this route.
     * @return A String representing the description of this route.
     */
    public
    String
    getDescription()
    {
    	return this.description ;
    }

    /**
     * Setter for route description property. This maps to &lt;description&gt; tag value.
     * @param description A String representing the description of this route.
     */
    public
    void
    setDescription( String description )
    {
    	this.description = description ;
		this.isChanged = true ;
    }

    /**
     * Returns the src of this route.
     * @return A String representing the src of this route.
     */
    public
    String
    getSrc( )
    {
    	return this.src ;
    }

    /**
     * Setter for src type property. This maps to &lt;src&gt; tag value.
     * @param src A String representing the src of this route.
     */
    public
    void
    setSrc( String src )
    {
    	this.src = src ;
		this.isChanged = true ;
    }

    /**
     * Returns the number of this route.
     * @return A String representing the number of this route.
     */
    public
    Integer
    getNumber( )
    {
    	return this.number ;
    }

    /**
     * Setter for route number property. This maps to &lt;number&gt; tag value.
     * @param number An Integer representing the number of this route.
     */
    public
    void
    setNumber( Integer number )
    {
    	this.number = number ;
		this.isChanged = true ;
    }

    /**
     * Returns the type of this route.
     * @return A String representing the type of this route.
     */
    @Override
	public
    String
    getType( )
    {
    	return this.type ;
    }

    /**
     * Setter for route type property. This maps to &lt;type&gt; tag value.
     * @param type A String representing the type of this route.
     */
    public
    void
    setType(String type)
    {
    	this.type = type ;
		this.isChanged = true ;
    }


    /**
     * Getter for the list of waypoints of this route.
     * @return an ArrayList of {@link RoutePoint} representing the points of the route.
     */
    public
    List<RoutePoint>
    getRoutePoints( )
    {
    	return this.routePoints ;
    }


	/**
	 * Adds this new routePoint to this route.
	 * @param rtePt a {@link RoutePoint}.
	 */
	public
	void
	addRoutePoint( RoutePoint rtePt )
	{
		if (CollectionUtils.addIgnoreNull( this.routePoints, rtePt ))
			this.isChanged = true ;
	}


    /**
     * Adds this new routePoint to this route.
     * @param rtePt a {@link RoutePoint}.
     */
    public
    void
    addRoutePoint( int position, RoutePoint rtePt )
    {
    	if (rtePt != null)
    	{
    		this.routePoints.add( position, rtePt ) ;
    		this.isChanged = true ;
    	}
    }


    public
    void
    addRoutePoints( List<? extends RoutePoint> rtePts )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.routePoints, rtePts ))
    		this.isChanged = true ;
    }


	/**
	 * Removes a routePoint from this route.
	 * @param rtePt a {@link RoutePoint}.
	 */
	public
	void
	removeRoutePoint( RoutePoint rtePt )
	{
		if (this.routePoints.remove( rtePt ) )
			this.isChanged = true ;
	}


    public
    void
    clearRoutePoints( )
    {
    	if (!this.routePoints.isEmpty())
    	{
    		this.routePoints.clear( ) ;
    		this.isChanged = true ;
    	}
    }

    /**
     * Setter for the list of waypoints of this route.
     * @param rtePts an ArrayList of {@link RoutePoint} representing the points of the route.
     */
    public
    void
    setRoutePoints( List<? extends RoutePoint> rtePts )
    {
    	if (this.routePoints != rtePts)
    	{
            clearRoutePoints( ) ;
            addRoutePoints( rtePts ) ;
    	}

    	this.isChanged = true ;
    }


    @Override
	public
    boolean
    isChanged( )
    {
    	if (this.isChanged)
    		return true ;

    	for (RoutePoint rtePt: this.routePoints)
    		if (rtePt.isChanged( ))
    			return true ;

    	return false ;
    }


    @Override
	public
    void
    resetIsChanged( )
    {
    	this.isChanged = false ;

    	for (RoutePoint rtePt: this.routePoints)
    		rtePt.resetIsChanged( ) ;
    }


    /**
     * Returns a String representation of this track.
     */
    @Override
    public
    String
    toString( )
    {
		return new StringBuffer()
				   .append("rte[name:" )
				   .append( name )
				   .append( " points:" )
				   .append( this.routePoints.size() )
				   .append( " ]")
		           .toString()
		           ;
    }
}

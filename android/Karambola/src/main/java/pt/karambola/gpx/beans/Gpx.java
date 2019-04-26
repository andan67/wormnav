/*
 * Gpx.java
 * 
 * Copyright (c) 2012, AlternativeVision. All rights reserved.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */

package pt.karambola.gpx.beans;

import java.util.ArrayList;
import java.util.List;

import pt.karambola.commons.collections.CollectionUtils;


/**
 * This class holds gpx information from a &lt;gpx&gt; node.
 * <br>
 * <p>
 * GPX specification for this tag:
 * </p>
 * <code>
 * &lt;gpx version="1.1" creator=""xsd:string [1]"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;metadata&gt; xsd:string &lt;/metadata&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;wpt&gt; xsd:string &lt;/wpt&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;rte&gt; xsd:string &lt;/rte&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;trk&gt; xsd:string &lt;/trk&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;extensions&gt; extensionsType &lt;/extensions&gt; [0..1]<br>
 * &lt;/gpx&gt;<br>
 *</code>
 */
public class
Gpx
	extends Extension
{
    private 		String		version ;
    private 		String		creator ;
    private final	List<Point>	points	= new ArrayList<>( ) ;
    private final	List<Route>	routes	= new ArrayList<>( ) ;
    private final	List<Track>	tracks	= new ArrayList<>( ) ;
    
    private boolean isChanged = false ;

    /**
     * Returns the version of a gpx object
     *
     * @return A String representing the version of this gpx object
     */
    public
    String
    getVersion( )
    {
    	return this.version ;
    }

    /**
     * Setter for gpx version property. This maps to <i>version</i> attribute
     * value.
     *
     * @param version
     *            A String representing the version of a gpx file.
     */
    public
    void
    setVersion( String version )
    {
    	this.version = version ;
		this.isChanged = true ;
    }

    /**
     * Returns the creator of this gpx object
     *
     * @return A String representing the creator of a gpx object
     */
    public
    String
    getCreator( )
    {
    	return this.creator ;
    }

    /**
     * Setter for gpx creator property. This maps to <i>creator</i> attribute
     * value.
     *
     * @param creator
     *            A String representing the creator of a gpx file.
     */
    public
    void
    setCreator( String creator )
    {
    	this.creator = creator ;
		this.isChanged = true ;
    }


    /**
     * Getter for the list of waypoints from a gpx objecty
     *
     * @return a ArrayList of {@link Point}
     */
    public
    List<Point>
    getPoints( )
    {
    	return this.points ;
    }


    /**
     * Adds a new point to a gpx object
     *
     * @param pt
     *            a {@link Point}
     */
    public
    void
    addPoint( Point pt )
    {
    	if (CollectionUtils.addIgnoreNull( this.points, pt ))
    		this.isChanged = true ;
    }
    

    public
    void
    removePoint( Point pt )
    {
    	this.points.remove( pt ) ;
		this.isChanged = true ;
    }

    /**
     * Adds new points to a gpx object
     *
     * @param pts
     *            a {@link Point}
     */
    public
    void
    addPoints( Iterable<? extends Point> pts )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.points, pts ))
    		this.isChanged = true ;
    }


    public
    void
    clearPoints( )
    {
    	if (!this.points.isEmpty( ))
   		{
    		this.points.clear( ) ;
        	this.isChanged = true ;
   		}
    }


    /**
     * Setter for the list of waypoints from a gpx object
     *
     * @param wpts
     *            a ArrayList of {@link Point}
     */
    public
    void
    setPoints( Iterable<? extends Point> pts )
    {
    	if (this.points != pts)
    	{
            clearPoints( ) ;
        	addPoints( pts ) ;
    	}
    }


    /**
     * Getter for the list of routes from a gpx object
     *
     * @return a ArrayList of {@link Route}
     */
    public
    List<Route>
    getRoutes( )
    {
    	return this.routes ;
    }


    /**
     * Adds a new route to a gpx object
     *
     * @param rte
     *            a {@link Route}
     */
    public
    void
    addRoute( Route rte )
    {
    	if (CollectionUtils.addIgnoreNull( this.routes, rte ))
        	this.isChanged = true ;
    }

    
    public
    void
    removeRoute( Route rte )
    {
    	this.routes.remove( rte ) ;
    	this.isChanged = true ;
    }

    /**
     * Adds new routes to a gpx object
     *
     * @param rtes
     *            a {@link Route}
     */
    public
    void
    addRoutes( Iterable<? extends Route> rtes )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.routes, rtes ))
        	this.isChanged = true ;
    }

    
    public
    void
    clearRoutes( )
    {
    	if (!this.routes.isEmpty())
   		{
    		this.routes.clear( ) ;
        	this.isChanged = true ;
   		}
    }


    /**
     * Setter for the list of routes from a gpx object
     *
     * @param rtes
     *            a ArrayList of {@link Route}
     */
    public
    void
    setRoutes( Iterable<? extends Route> rtes )
    {
    	if (this.routes != rtes)
    	{
            clearRoutes( ) ;
        	addRoutes( rtes ) ;
    	}
    }


    /**
     * Getter for the list of Tracks from a gpx objecty
     *
     * @return a ArrayList of {@link Track}
     */
    public
    List<Track>
    getTracks( )
    {
    	return this.tracks ;
    }


    /**
     * Adds a new track to a gpx object
     *
     * @param trk
     *            a {@link Track}
     */
    public
    void
    addTrack( Track trk )
    {
    	if (CollectionUtils.addIgnoreNull( this.tracks, trk ))
        	this.isChanged = true ;
    }

    /**
     * Adds new tracks to a gpx object
     *
     * @param track
     *            a {@link Track}
     */
    public
    void
    addTracks( Iterable<? extends Track> trks )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.tracks, trks ))
        	this.isChanged = true ;
    }


    public
    void
    clearTracks( )
    {
    	if (!this.tracks.isEmpty())
   		{
    		this.tracks.clear( ) ;
        	this.isChanged = true ;
   		}
    }
    
    
    /**
     * Setter for the list of tracks from a gpx object
     *
     * @param trks
     *            a ArrayList of {@link Track}
     */
    public
    void
    setTracks( Iterable<? extends Track> trks )
    {
    	if (this.tracks != trks)
    	{
            clearTracks( ) ;
            addTracks( trks ) ;
    	}
    }


    public
    boolean
    isChanged( )
    {
    	if (this.isChanged)  return true ;

    	for (Point pt: this.points)
    		if (pt.isChanged( ))  return true ;
 
    	for (Route rte: this.routes)
    		if (rte.isChanged( ))  return true ;

    	for (Track trk: this.tracks)
    		if (trk.isChanged( ))  return true ;

    	return false ;
    }


    public
    boolean
    isEmpty( )
    {
    	return this.points.isEmpty( )  &&  this.routes.isEmpty( )  &&  this.tracks.isEmpty( ) ;
    }


    public
    void
    resetIsChanged( )
    {
    	this.isChanged = false ;

    	for (Point pt: this.points)
    		pt.resetIsChanged( ) ;
 
    	for (Route rte: this.routes)
    		rte.resetIsChanged( ) ;

    	for (Track trk: this.tracks)
    		trk.resetIsChanged( ) ;
    }
}

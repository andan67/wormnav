/*
 * Track.java
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
 * This class holds track information from a &lt;trk&gt; node.
 * <br>
 * <p>GPX specification for this tag:</p>
 * <code>
 * &lt;trk&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;name&gt; xsd:string &lt;/name&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;cmt&gt; xsd:string &lt;/cmt&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;desc&gt; xsd:string &lt;/desc&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;src&gt; xsd:string &lt;/src&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;link&gt; linkType &lt;/link&gt; [0..*]<br>
 * &nbsp;&nbsp;&nbsp;&lt;number&gt; xsd:nonNegativeInteger &lt;/number&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;type&gt; xsd:string &lt;/type&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;extensions&gt; extensionsType &lt;/extensions&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;trkseg&gt; trksegType &lt;/trkseg&gt; [0..*]<br>
 * &lt;/trk&gt;<br>
 *</code>
 */
public class
Track
	extends		Extension
	implements	Named, Typed
{
    /*
<name> xsd:string </name> [0..1] ?
<cmt> xsd:string </cmt> [0..1] ?
<desc> xsd:string </desc> [0..1] ?
<src> xsd:string </src> [0..1] ?
<link> linkType </link> [0..*] ?
<number> xsd:nonNegativeInteger </number> [0..1] ?
<type> xsd:string </type> [0..1] ?
<extensions> extensionsType </extensions> [0..1] ?
<trkseg> trksegType </trkseg> [0..*] ?
     */
    private 		String				name ;
    private 		String				comment ;
    private 		String				description ;
    private 		String				src ;
    private 		Integer				number ;
    private 		String				type ;
    private final	List<TrackSegment>	trackSegments = new ArrayList<>( ) ;

    /**
     * Returns the name of this track.
     * @return A String representing the name of this track.
     */
    public
    String
    getName( )
    {
    	return this.name ;
    }

    /**
     * Setter for track name property. This maps to &lt;name&gt; tag value.
     * @param name A String representing the name of this track.
     */
    public
    void
    setName( String name )
    {
    	this.name = name ;
		this.isChanged = true ;
    }

    /**
     * Returns the comment of this track.
     * @return A String representing the comment of this track.
     */
    public
    String
    getComment( )
    {
    	return this.comment ;
    }

    /**
     * Setter for track comment property. This maps to &lt;comment&gt; tag value.
     * @param comment A String representing the comment of this track.
     */
    public
    void
    setComment( String comment )
    {
    	this.comment = comment ;
		this.isChanged = true ;
    }

    /**
     * Returns the description of this track.
     * @return A String representing the description of this track.
     */
    public
    String
    getDescription( )
    {
    	return this.description ;
    }

    /**
     * Setter for track description property. This maps to &lt;description&gt; tag value.
     * @param description A String representing the description of this track.
     */
    public
    void
    setDescription( String description )
    {
    	this.description = description ;
		this.isChanged = true ;
    }

    /**
     * Returns the src of this track.
     * @return A String representing the src of this track.
     */
    public
    String
    getSrc( )
    {
    	return this.src ;
    }

    /**
     * Setter for src type property. This maps to &lt;src&gt; tag value.
     * @param src A String representing the src of this track.
     */
    public
    void
    setSrc( String src )
    {
    	this.src = src ;
		this.isChanged = true ;
    }

    /**
     * Returns the number of this track.
     * @return A String representing the number of this track.
     */
    public
    Integer
    getNumber( )
    {
    	return this.number ;
    }

    /**
     * Setter for track number property. This maps to &lt;number&gt; tag value.
     * @param number An Integer representing the number of this track.
     */
    public
    void
    setNumber( Integer number )
    {
    	this.number = number ;
		this.isChanged = true ;
    }

    /**
     * Returns the type of this track.
     * @return A String representing the type of this track.
     */
    public
    String
    getType( )
    {
    	return this.type ;
    }

    /**
     * Setter for track type property. This maps to &lt;type&gt; tag value.
     * @param type A String representing the type of this track.
     */
    public
    void
    setType( String type )
    {
    	this.type = type ;
		this.isChanged = true ;
    }

    /**
     * Getter for the list of trackSegments of a track.
     * @return an ArrayList of {@link TrackSegment} representing the segments of the track.
     */
    public
    List<TrackSegment>
    getTrackSegments( )
    {
    	return this.trackSegments ;
    }

    
    public
	List<TrackPoint>
	getTrackPoints( )
	{
		List<TrackPoint> trkPts = new ArrayList<>( ) ;

		for (TrackSegment trkSeg: this.trackSegments)
				trkPts.addAll(trkSeg.getTrackPoints()) ;

		return trkPts ;
	}


    /**
     * Adds this new track segment to this track.
     * @param trkSgmt a {@link TrackSegment}.
     */
    public
    void
    addTrackSegment( TrackSegment trkSgmt )
    {
    	if (CollectionUtils.addIgnoreNull( this.trackSegments, trkSgmt ))
    		this.isChanged = true ;
    }


    public
    void
    addTrackSegments( List<? extends TrackSegment> trkSgmts )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.trackSegments, trkSgmts ))
        	this.isChanged = true ;
    }


    /**
     * Clearer for the list of trackSegments of a track.
     */
    public
    void
    clearTrackSegments( )
    {
    	if (!this.trackSegments.isEmpty())
    	{
    		this.trackSegments.clear( ) ;
        	this.isChanged = true ;
    	}
    }

    /**
     * Setter for the list of trackSegments of a track.
     * @param trkSgmts an ArrayList of {@link TrackSegment} representing the segments of the track.
     */
    public
    void
    setTrackSegments( List<? extends TrackSegment> trkSgmts )
    {
    	if (this.trackSegments != trkSgmts)
    	{
            clearTrackSegments( ) ;
            addTrackSegments( trkSgmts ) ;
    	}
    }


    public
    boolean
    isChanged( )
    {
    	if (this.isChanged)  return true ;

    	for (TrackSegment trkSgmt: this.trackSegments)
    		if (trkSgmt.isChanged( ))  return true ;

    	return false ;
    }


    public
    void
    resetIsChanged( )
    {
    	this.isChanged = false ;

    	for (TrackSegment trkSgmt: this.trackSegments)
    		trkSgmt.resetIsChanged( ) ;
    }


    /**
     * Returns a String representation of this segment.
     */
    @Override
    public
    String
    toString( )
    {
		StringBuffer sb = new StringBuffer( ) ;
		sb.append( "trk[" ) ;
		sb.append( "name:" + this.name +" " ) ;
		sb.append( "segments:" + this.trackSegments.size( ) + " " ) ;
		sb.append( "]" ) ;

		return sb.toString( ) ;
    }
}

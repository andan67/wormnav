/*
 * TrackSegment.java
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
/**
 * This class holds track information from a &lt;trkseg&gt; node.
 * <br>
 * <p>GPX specification for this tag:</p>
 * <code>
 * &lt;trkseg&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;trkpt&gt;      wptType        &lt;/trkpt&gt; [0..*]<br>
 * &nbsp;&nbsp;&nbsp;&lt;extensions&gt; extensionsType &lt;/extensions&gt; [0..1]<br>
 * &lt;/trkseg&gt;<br>
 *</code>
 */
public class
TrackSegment
	extends Extension
{
    private final	List<TrackPoint> trackPoints = new ArrayList<>( ) ;

    /**
     * Getter for the list of points of a track.
     * @return an List of {@link TrackPoint} representing the points of the track.
     */
    public
    List<TrackPoint>
    getTrackPoints( )
    {
    	return this.trackPoints ;
    }

    /**
     * Adds this new track point to this track.
     * @param trkPt a {@link TrackPoint}.
     */
    public void
    addTrackPoint( TrackPoint trkPt )
    {
    	if (CollectionUtils.addIgnoreNull( this.trackPoints, trkPt ))
    		this.isChanged = true ;
    }

    public void
    addTrackPoints( List<? extends TrackPoint> trkPts )
    {
    	if (CollectionUtils.addAllIgnoreNull( this.trackPoints, trkPts ))
    		this.isChanged = true ;
	}

    /**
     * Clearer for the list of points of a track.
     */
    public
    void
    clearTrackPoints( )
    {
    	if (!this.trackPoints.isEmpty( ))
   		{
    		this.trackPoints.clear( ) ;
    		this.isChanged = true ;
   		}
    }

    /**
     * Setter for the list of points of a track.
     * @param trkPts an List of {@link TrackPoint} representing the points of the track.
     */
    public
    void
    setTrackPoints( List<? extends TrackPoint> trkPts )
    {
    	if (this.trackPoints != trkPts)
    	{
            clearTrackPoints( ) ;
            addTrackPoints( trkPts ) ;
    	}
    }


    public
    boolean
    isChanged( )
    {
    	if (this.isChanged)  return true ;

    	for (TrackPoint trkPt: this.trackPoints)
    		if (trkPt.isChanged( ))  return true ;

    	return false ;
    }


    public
    void
    resetIsChanged( )
    {
    	this.isChanged = false ;

    	for (TrackPoint trkPt: this.trackPoints)
    		trkPt.resetIsChanged( ) ;
    }


    /**
     * Returns a String representation of this track.
     */
    @Override
    public
    String
    toString( )
    {
		StringBuffer sb = new StringBuffer( ) ;
		sb.append( "trksgm[" ) ;
		sb.append( "points:" + this.trackPoints.size( ) +" " ) ;
		sb.append( "]" ) ;

		return sb.toString( ) ;
    }
}

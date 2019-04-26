/*
 * Units.java
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


package pt.karambola.geo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum
Units
{
	METRIC(0), IMPERIAL(1), NAUTICAL(2) ;

	private int code ;

	private static final Map<Integer,Units> codeLookup = new HashMap<Integer,Units>( ) ;

	static
	{
        for(Units w : EnumSet.allOf(Units.class))
        	codeLookup.put( w.getCode(), w ) ;
    }

	private
	Units( int code )
	{
		this.code = code ;
	}


	public
	int
	getCode( )
	{
		return this.code ;
	}

    
	public
    static
    Units
    get( int code )
    {
        return codeLookup.get( code ) ;
    }

	
	/**
	 * String formats a value.
	 * 
	 * @param value value to format
	 * @param fmtE0 format to be used if value < 1x10^0
	 * @param fmtE1 format to be used if value ] 1x10^0,  1x10^1]
	 * @param fmtE2 format to be used if value ] 1x10^1,  1x10^2]
	 * @param fmtE3 format to be used if value ] 1x10^2,  1x10^3]
	 * @param unitsSuffix units suffix/token
	 * @return [0] formated value, [1] units suffix
	 * 
	 * @author Afonso Santos
	 */
	private static
	String[]
	formatValue( final double value, String fmtE0, String fmtE1, String fmtE2, String fmtE3, String unitsSuffix )
	{
		String[] formated	= new String[2] ;
		String	 format		= null ;

		if      (value < 10f   )  format = fmtE0 ;		//                 less than 10
		else if (value < 100f  )  format = fmtE1 ;		// More than 10  , less than 100
		else if (value < 1000f )  format = fmtE2 ;		// More than 100 , less than 1000
		else                      format = fmtE3 ;		// More than 1000

		formated[0] = String.format( Locale.ENGLISH, format, value ) ;
		formated[1] = unitsSuffix ;

		return formated ;
	}


	/* Distance conversion factors */
	public final static double MTR2FT 	= 3.2808399f ;
	public final static double MTR2KM	= 0.001f ;
	public final static double MTR2MI	= 0.000621371192f ;
	public final static double MTR2NM	= 0.000539956803f ;
	
	/**
	 * String formats a distance value.
	 * 
	 * @param distanceMtr distance value in meters
	 * @param units type of units to be used [METRIC, IMPERIAL, NAUTICAL]
	 * @return [0] formated distance, [1] units suffix
	 * 
	 * @author Afonso Santos
	 */
    public static
	String[]
	formatDistance( final double distanceMtr, Units units )
	{
		switch (units)
		{
			case METRIC:
				if (distanceMtr < 500)
					return formatValue( distanceMtr, "%.0f", "%.0f", "%.0f", "%.0f", "m" ) ;

				return formatValue( distanceMtr * MTR2KM, "%.2f", "%.1f", "%.1f", "%.0f", "km" ) ;

			case IMPERIAL:
				double distanceFt = distanceMtr * MTR2FT ;

				if (distanceFt < 500)
					return formatValue( distanceFt, "%.0f", "%.0f", "%.0f", "%.0f", "ft" ) ;

				return formatValue( distanceMtr * MTR2MI, "%.2f", "%.1f", "%.1f", "%.0f", "mi" ) ;

			case NAUTICAL:
				return formatValue( distanceMtr * MTR2NM, "%.2f", "%.1f", "%.1f", "%.0f", "nm" ) ;
		}

		return null;
	}


	/* Speed conversion factors */
	public final static double MS2FS 	= 3.2808398950f ;
	public final static double MS2KPH 	= 3.6f ;
	public final static double MS2MPH 	= 2.2369362912f ;
	public final static double MS2KTS 	= 1.9438444924f ;

	/**
	 * String formats a speed value.
	 * 
	 * @param speedMtrSec speed value in meters per second
	 * @param units type of units to be used [METRIC, IMPERIAL, NAUTICAL]
	 * @return [0] formated speed, [1] units suffix
	 * 
	 * @author Afonso Santos
	 */
    public static
	String[]
	formatSpeed( final double speedMtrSec, Units units )
	{
		switch (units)
		{
			case METRIC:
				double speedKph = speedMtrSec * MS2KPH ;

				if (speedKph < 5)
					return formatValue( speedMtrSec, "%.1f", "%.1f", "%.0f", "%.0f", "m/s" ) ;

				return formatValue( speedKph, "%.1f", "%.1f", "%.0f", "%.0f", "km/h" ) ;

			case IMPERIAL:
				double speedMph = speedMtrSec * MS2MPH ;

				if (speedMph < 5)
					return formatValue( speedMtrSec * MS2FS, "%.1f", "%.1f", "%.0f", "%.0f", "ft/s" ) ;

				return formatValue( speedMph, "%.1f", "%.1f", "%.0f", "%.0f", "mi/h" ) ;

			case NAUTICAL:
				return formatValue( speedMtrSec * MS2KTS, "%.1f", "%.1f", "%.0f", "%.0f", "kts" ) ;
		}

		return null;
	}


	public static
	double
	speedAdjustedDistance( final double minorDistanceMtr
						 , final double minorTraversalTimeSec
						 , final double speedMtrPerSec
						 )
	{
		final double adjustedDistanceMtr = speedMtrPerSec * minorTraversalTimeSec ;

		return adjustedDistanceMtr < minorDistanceMtr  ?  minorDistanceMtr  :  adjustedDistanceMtr ;
	}
}

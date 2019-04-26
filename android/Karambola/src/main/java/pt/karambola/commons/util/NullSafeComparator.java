/*
 * NullSafeComparator.java
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


package pt.karambola.commons.util;

import java.util.Date;


public class
NullSafeComparator
{
	public static
	int
	compare( String v1, String v2 )
	{
		if (v1 == v2)    return  0 ;
		if (v1 == null)  return -1 ;
		if (v2 == null)  return  1 ;

		return v1.compareTo( v2 ) ;
	}


	public static
	int
	compare( Integer v1, Integer v2 )
	{
		if (v1 == v2)    return  0 ;
		if (v1 == null)  return -1 ;
		if (v2 == null)  return  1 ;

		return v1.compareTo( v2 ) ;
	}


	public static
	int
	compare( Double v1, Double v2 )
	{
		if (v1 == v2)    return  0 ;
		if (v1 == null)  return -1 ;
		if (v2 == null)  return  1 ;

		return v1.compareTo( v2 ) ;
	}


	public static
	int
	compare( Date v1, Date v2 )
	{
		if (v1 == v2)    return  0 ;
		if (v1 == null)  return -1 ;
		if (v2 == null)  return  1 ;

		return v1.compareTo( v2 ) ;
	}


	public static
	boolean
	equals( String v1, String v2 )
	{
		if (v1 == v2)                    return true ;
		if (v1 == null  &&  v2 != null)  return false ;
		if (v1 != null  &&  v2 == null)  return false ;

		return v1.equals( v2 ) ;
	}
}
/*
 * TypedUtils.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class
TypedUtils
{
	public static <T extends Typed>
	List<String>
	getTypes( final Iterable<T> items )
	{
		List<String> types = new ArrayList<>( ) ;

		if (items != null)
			for (Typed item: items)
			{
				String type = item.getType( ) ;

				if (type != null  &&  !types.contains( type ))
					types.add( type ) ;
			}

		Collections.sort( types ) ;

		return types ;
	}
}

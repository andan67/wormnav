/*
 * NamedUtils.java
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
import java.util.Comparator;
import java.util.List;

import pt.karambola.commons.collections.CollectionUtils;
import pt.karambola.commons.collections.ListUtils;

public class
NamedUtils
{
	public static
	List<String>
	getNames( final Iterable<? extends Named> items )
	{
		List<String> names = new ArrayList<>( ) ;

		if (items != null)
			for (Named item: items)
			{
				String name = item.getName( ) ;
				if (name != null  &&  !names.contains(name)) names.add( name ) ;
			}

		return names ;
	}


	public static
	List<String>
	getDuplicatedNames( Iterable<? extends Named> items1, Iterable<? extends Named> items2 )
	{
		return ListUtils.intersection( getNames( items1 ), getNames( items2 ) ) ;
	}


	public static <I extends T, T>
	List<String>
	getNamesSortedDecorated( final Iterable<I>			items
	                       , final Comparator<T>		comparator
	                       , final StringDecorator<T>	decorator		// Optional decorator to append "(more info)" suffix to the name.
	                       , List<I>					itemsSorted		// Optional output sorted item list.
			               )
	{
		List<String> names = new ArrayList<>() ;

		if (itemsSorted != null)
			itemsSorted.clear( ) ;

		if (items != null)
		{
			List<I> sortedItems = new ArrayList<>() ;
			CollectionUtils.addAllIgnoreNull( sortedItems, items ) ;
			Collections.sort( sortedItems, comparator ) ;

			int unnamedCount = 0 ;

			for (I item: sortedItems)
			{
				String name = (item instanceof Named) ? ((Named)item).getName( ) : null ;

				if (name == null)
					name = "Unnamed#" + ++unnamedCount ;

				if (decorator != null)
					name += " (" + decorator.getStringDecoration( item ) + ")" ;

				names.add( name ) ;

				if (itemsSorted != null)
					itemsSorted.add( item ) ;
			}
		}

		return names ;
	}
}

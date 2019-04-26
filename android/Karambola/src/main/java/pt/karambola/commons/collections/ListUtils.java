/*
 * ListUtils.java
 *
 * Copyright (c) 2016 Karambola. All rights reserved.
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file; if not, write to 
 * the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package pt.karambola.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class
ListUtils
{
    /**
     * Returns a new list containing all elements that are contained in
     * both given lists.
     *
     * @param <E> the element type
     * @param list1  the first list
     * @param list2  the second list
     * @return  the intersection of those two lists
     * @throws NullPointerException if either list is null
     */
    public static <E>
    List<E>
    intersection( final List<? extends E> list1, final List<? extends E> list2 )
    {
        final List<E> result = new ArrayList<E>() ;

        List<? extends E> smaller = list1 ;
        List<? extends E> larger  = list2 ;

        if (list1.size() > list2.size())
        {
            smaller = list2 ;
            larger  = list1 ;
        }

        final Set<E> set = new HashSet<E>(smaller) ;

        for (final E e : larger)
            if (set.contains(e))
            {
                result.add(e);
                set.remove(e);
            }

        return result ;
    }


	public static	<T>
	List<T>
	getDistinctItems( final List<Pair<T,T>>	pairs )
	{
		List<T>	distinctItems	= new ArrayList<>( ) ;

		for (Pair<T, T> pair: pairs)
		{
			if (!distinctItems.contains( pair.first ))   distinctItems.add( pair.first ) ;
			if (!distinctItems.contains( pair.second ))  distinctItems.add( pair.second ) ;
		}

		return 	distinctItems ;
	}


	public static	<T>
	List<T>
	filter( final Collection<? extends T> items, final Predicate<? super T> criteria )
	{
		List<T> filteredItems = new ArrayList<T>(items) ;			// MUST make a new collection because filtering is destructive.
		CollectionUtils.filter( filteredItems, criteria ) ;

		return filteredItems ;
	}


	public static	<T>
	List<Pair<T,T>>
	getConflictingPairs( final List<T>								items
					   , final BiPredicate<? super T, ? super T>	conflictEvaluator
					   )
	{
		List<Pair<T,T>> conflictingPairs	= new ArrayList<>( ) ;
		int				itemsSize			= items.size( ) ;

		for (int i = 0  ;  i < itemsSize-1  ;  ++i)
		{
			T itemI = items.get(i) ;

			for (int j = i+1  ;  j < itemsSize  ;  ++j)
			{
				T itemJ = items.get(j) ;

				if (conflictEvaluator.test( itemI, itemJ ))
					conflictingPairs.add( new Pair<T, T>(itemI,itemJ) ) ;
			}
		}

		return conflictingPairs ;
	}


	public static	<T>
	List<T>
	getConflictingItems( final List<T>								items
					   , final BiPredicate<? super T, ? super T>	conflictEvaluator
					   )
	{
		return getDistinctItems( getConflictingPairs( items, conflictEvaluator ) ) ;
	}


	public static	<T>
	List<T>
	purge( final List<T>							items
		 , final BiPredicate<? super T, ? super T>	conflictEvaluator
		 , final Comparator<? super T>				conflictSolver
		 )
	{
		List<Pair<T,T>> conflictingPairs = getConflictingPairs( items, conflictEvaluator ) ;

		if (conflictingPairs.size( ) == 0)
			return items ;					// No conflicts detected, original list is therefore already purged. (base of recursive call)

		// Conflicts exist. Must calculate their smallest possible redux-transitive-closure set.
		List<T> conflictingItems 	= getDistinctItems( conflictingPairs ) ;
		List<T> purgedItems			= new ArrayList<>( ) ;

		// Add unconflicting items.
		for (T item: items)
			if (!conflictingItems.contains( item )  &&  !purgedItems.contains( item ))
				purgedItems.add( item ) ;

		// Initial conflictingItems shrinking reduction based on previously detected conflicting pairs.
		int previousConflictingItemsSize = conflictingItems.size( ) ;
		conflictingItems.clear() ;

		// Redux conflicting pairs.
		for (Pair<T, T> conflictingPair: conflictingPairs)
		{
			T pairWinner = null ;

			switch (conflictSolver.compare( conflictingPair.first, conflictingPair.second ))
			{
				case -1:	// second is bigger/stronger.
					pairWinner = conflictingPair.second ;
					break ;

				case 1:		// first is bigger/stronger.
				case 0:		// They have same strength, keep one of them.
					pairWinner = conflictingPair.first ;
					break ;
			}

			if (!conflictingItems.contains( pairWinner ))  conflictingItems.add( pairWinner ) ;
		}

		// Seek transitive closure: loop until no further shrinking of conflictingPts is achievable.
		while (previousConflictingItemsSize != conflictingItems.size( ))
		{
			previousConflictingItemsSize = conflictingItems.size( ) ;
			conflictingItems = purge( conflictingItems, conflictEvaluator, conflictSolver ) ;		// Recursive call !!!
		}

		// Add surviving redux-transitive-closure deconflicted items.
		for (T conflictingItem: conflictingItems)
			if (!purgedItems.contains( conflictingItem ))
				purgedItems.add( conflictingItem ) ;

		return 	purgedItems ;
	}


	public static	<T>
	List<T>
	reverse( final List<T>	items )
	{
		final List<T>	reversed	= new ArrayList<>() ;

		for (int itemIdx = items.size() - 1  ;  itemIdx >= 0  ;  --itemIdx )
			reversed.add( items.get(itemIdx) ) ;

		return reversed ;
	}
}

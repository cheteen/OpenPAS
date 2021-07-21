//    Copyright (c) 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

package fopas.basics;

import java.util.List;
import java.util.Set;

// We're only interested in enumarable sets in computations, so let's assume a set to be iterable up front - though this will change.
public interface FOSet<T extends FOElement>
{
	public int size(); // shared with Set, may return infinite (Integer.MAX_VALUE).
	
	public String getName();
	
	public boolean contains(Object o);

	// TODO: I think the best is to have the regular complement as withing the same type.
	// Then have another complementAcross that takes any types.
	// Finally have an implementation that uses the 6x methods below to implement the above.
	
	//TODO: Do 6x of these and remove the reverses:
	// complementOut, complementIn // the default version that does it within the same type.
	// complementSuperOut, complementSuperIn // complement against a super set
	// complementExtendOut, complementExtendIn // complement against a descendant set.
	
	// Do I need the complementIn/Out when I have the extendIn/Out?
	
	// Return the complement set: relativeSet \ this
	public default FOSet<? super T> complementSuperOut(FOSet<? super T> relativeSet) { return null;}
	// Return the complement set: this \ relativeSet
	public default FOSet<? extends T> complementSuperIn(FOSet<? super T> relativeSet) { return null;} 

	// Return the complement set: relativeSet \ this
	public default FOSet<? extends T> complementExtendOut(FOSet<? extends T> relativeSet) { return null;}
	// Return the complement set: this \ relativeSet
	public default FOSet<? super T> complementExtendIn(FOSet<? extends T> relativeSet) { return null;} 
	
	// Return the complement set: relativeSet \ this
	public default FOSet<T> complementOut(FOSet<T> relativeSet) { return null;}
	// Return the complement set: this \ relativeSet
	public default FOSet<T> complementIn(FOSet<T> relativeSet) { return null;} 

	/**
	 * This is to give one of the complement set:
	 * relativeSet \ this (reverse = False)
	 * 
	 * This method is a restricted version of complementAcross, and it works only with 
	 * 
	 * @param relativeSet
	 * @return A complement set as described above.
	 */
	public default FOSet<T> complement(FOSet<T> relativeSet)
	{
		FOSet<? extends FOElement> complementSet = complementAcross(relativeSet);
		if(complementSet == null)
			return null;
			
		if(complementSet.getType().equals(getType()))
		{
			@SuppressWarnings("unchecked")
			FOSet<T> matchingComplementSet = (FOSet<T>) complementSet;
			return matchingComplementSet;
		}
		else
			return null; // we have a complement set, but it's of the wrong type, so ignore it.
	}

	/**
	 * This is to give one of the complement set:
	 * relativeSet \ this (reverse = False)
	 * 
	 * This method looks at the runtime types and tries to figure out a way to perform the complement
	 * across the two classes involved.
	 * 
	 * @param relativeSet
	 * @return A complement set as described above.
	 */
	public default FOSet<? extends FOElement> complementAcross(FOSet<? extends FOElement> relativeSet)
	{
		FOSet<? extends FOElement> complementSet;

		if(relativeSet.getType().equals(getType()))
		{
			@SuppressWarnings("unchecked")
			FOSet<T> relativeMatchingSet = (FOSet<T>) relativeSet;
			
			complementSet = complementOut(relativeMatchingSet);
			if(complementSet != null)
				return complementSet;
			
			complementSet = relativeMatchingSet.complementIn(this);
			if(complementSet != null)
				return complementSet;
		}
		// Let the equal type fall through here as both of the following will still apply to the equal type, and may produce a result.

		// Relative set contains super type of own type.
		if(relativeSet.getType().isAssignableFrom(getType()))
		{
			@SuppressWarnings("unchecked")
			FOSet<? super T> relativeMatchingSet = (FOSet<? super T>) relativeSet;

			complementSet = complementSuperOut(relativeMatchingSet);
			if(complementSet != null)
				return complementSet;
			
			return relativeMatchingSet.complementExtendIn(this); // may still be null
		}
		// Relative set type descends from own type
		if(getType().isAssignableFrom(relativeSet.getType()))
		{
			@SuppressWarnings("unchecked")
			FOSet<? extends T> relativeMatchingSet = (FOSet<? extends T>) relativeSet;

			complementSet = complementExtendOut(relativeMatchingSet);
			if(complementSet != null)
				return complementSet;

			return relativeMatchingSet.complementSuperIn(this); // may still be null
		}
		
		return null;
	}

	Class<T> getType();

	public default FOSet<T> complementIf(boolean condition, FOSet<T> relativeSet)
	{
		if(condition)
			return this.complement(relativeSet);
		return this;
	}
}

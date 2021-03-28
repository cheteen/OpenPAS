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
	
	/**
	 * This is to give the complement set: relativeSet \ this
	 * @param relativeSet
	 * @return A set containing elements that are in the relativeSet but not in this set.
	 */
	public FOSet<T> complement(FOSet<T> relativeSet);
	public default FOSet<T> complement(FOSet<T> relativeSet, boolean isComplement)
	{
		if(isComplement)
			return complement(relativeSet);
		else
			return this;
	}
	
	Class<T> getType();
}

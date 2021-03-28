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
import java.util.Map;

public interface FORelation <T extends FOElement>
{
	String getName();
	boolean satisfies(FOElement ... args);
	String getInfix();
	
	/**
	 * Get cardinality of the relation.
	 * @return -1 for any cardinality, >0 otherwise.
	 */
	int getCardinality();
	
	int getPrecedence() throws FOConstructionException;
	
	/**
	 * This will return either itself or a new FOSet thats a subset of it. It'll consider the variable and the (partially assigned)
	 * terms it's given and find out what subset of the universe is needed.
	 * @param var
	 * @param universeSubset
	 * @param terms
	 * @param isComplemented Whether the returned set if a relative complement of the universeSubset (useful when relation is used negated).
	 * @return
	 */
	<TI extends T> FOSet<? extends TI> tryConstrain(FOVariable var, FOSet<TI> universeSubset,  List<FOTerm> terms, boolean isComplemented);
	
	Class<T> getType();
}

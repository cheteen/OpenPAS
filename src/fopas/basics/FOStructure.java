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

import java.util.Map;

import fopas.FORuntime;

public interface FOStructure
{
	FOSet<FOElement> getUniverse();
	FOElement getConstantMapping(FOConstant foconst);
	FOElement setConstantMapping(FOConstant foconst, FOElement elt);
	Iterable<FORelation<FOElement>> getRelations();
	Iterable<FOFunction> getFunctions();
	Iterable<FOConstant> getConstants();

	Iterable<String> getAliases();
	FOFormula getAlias(String name);
	void addAlias(FOAlias formAlias) throws FOConstructionException;
	
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 * @throws FOConstructionException 
	 */
	boolean models(FOFormula form) throws FOConstructionException;
	
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getAssignments(FOFormula form) throws FOConstructionException;
	
	FORuntime getRuntime(); // This will come out of here later when I implement a proper runtime system, it's left here for convenience for now.
}

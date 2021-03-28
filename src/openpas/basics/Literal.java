//    Copyright (c) 2017, 2021 Burak Cetin
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

package openpas.basics;

import openpas.StringOps;

/**
 * A literal is the "atomic" building block of all propositional sentences. The Literal class encapsulates
 * the logical literal for OpenPAS.
 * A Literal instance is identified by its unique index number. It can represent the negated logical literal
 * if the negation flag is set.
 * Any instance of Literal with the same index number should be considered to be the same literal.
 * 
 * We define three types of literals: <br>
 * 1) proposition: A PAS proposition. An application-defined literal to correspond a proposition in the application domain. <br>
 * 2) assumption: A PAS assumption. An extended proposition that has also a probability associated with it. <br>
 * 3) special: A special type of proposition with pre-allocated index numbers to express falsity (False) and tautology (True). <br>
 * 
 * There's no limitation for the uniqueness of the name for object creation, but the {@link PAS} interface imposes name uniqueness
 * in a given PAS instance.
 * 
 * Special literals are used to construct True or False expressions and sentences. Depending on the expression type
 * an empty expression may be True or False. A special literal isn't needed to represent this. But to represent the opposite
 * a special literal is used. For example, an empty term is logically True. To create a term that's false we insert the special
 * False literal into the term that makes it False. See {@link StringOps} for more on this.
 */
public interface Literal {
	/**
	 * The type of this literal (see the interface documentation {@link Literal} for more).
	 */
	public enum LiteralType
	{
		Proposition,
		Assumption,
		Special,
	}
	
	/**
	 * Returns the given name for this literal.
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns true if this is a negated Literal, false otherwise.
	 * @return
	 */
	public boolean getNeg();
	
	/**
	 * Returns the index for this Literal. For a given factory this should be unique across
	 * all literals such that two Literal instance with the same index number represent the same literal.
	 * @return
	 */
	public Integer getIndex();
	
	/**
	 * Returns the type of this literal (see the interface documentation {@link Literal} for more).
	 * @return
	 */
	public LiteralType getType();
	
	/**
	 * Returns true is this Literal is an assumption (ie. the type is Assumption).
	 * @return
	 */
	public boolean isAssumption();
	
	/**
	 * Returns true is this Literal is a proposition (ie. the type is Proposition).
	 * @return
	 */
	public boolean isProposition();
	
	/**
	 * Returns true is this Literal is a special literal (ie. the type is Special). For example the literals
	 * True and False are special literals which are not propositions or assumptions.
	 * @return
	 */
	public boolean isSpecial();

	/**
	 * Return the same literal but negated.
	 * @return
	 */
	public Literal getNegated();
}

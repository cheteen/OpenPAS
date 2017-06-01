// OpenPAS
//
// Copyright (c) 2017 Burak Cetin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package openpas.basics;

import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;

/**
 * This is a class that encapsulates a PAS instance.
 * It's essentially a convenience container class that supports many
 * string parsing and consistency checking functions. It's a good
 * high-level interface to using OpenPAS as it distills and offers the needed functionality
 * from any various other classes into this single interface.
 * 
 * It's possible to do all the PAS related calculations without using the PAS class.
 * 
 * The special literals True and False are implicitly defined in any PAS instance (and so won't be accepted as
 * user-defined literals), but they are not returned by the get* functions.
 */
public interface PAS 
{
	/**
	 * This is the error reporting mechanism for PAS. Most methods allow for throwing this exception
	 * and errors should be reported by throwing exceptions with meaningful messages that allow
	 * identifying problems.
	 */
	public static class KBException extends Exception { 
		private static final long serialVersionUID = 1L;
		public KBException(String why) { super(why); }
	}

	/**
	 * Returns the embedded knowledgebase (KB) for the PAS instance. This KB should not be
	 * externally modified once inside a PAS instance.
	 * @return
	 */
	SimpleSentence<LogicalAnd, LogicalOr> getKB();
	
	/**
	 * Returns the assumptions contained in this PAS instance. It will return a positive and negated
	 * instance of each and every assumption known.
	 * @return
	 */
	Iterable<Assumption> getAssumptions();
	
	/**
	 * Returns the assumptions contained in this PAS instance same as {@link #getAssumptions()}. But also takes a
	 * parameter to specify whether the positive or negated versions of the assumptions are required only.
	 * @param positive
	 * @return
	 */
	Iterable<Assumption> getAssumptions(boolean positive);
	
	/**
	 * Returns the named assumption if it exists in this PAS instance.
	 * @param name
	 * @return Will return an assumption or null if there's not any.
	 */
	Assumption getAssumption(String name, boolean positive);
	
	/**
	 * Returns the propositions contained in this PAS instance. It will return a positive and negated
	 * instance of each and every proposition known.
	 * @return
	 */
	Iterable<Proposition> getPropositions();
	
	/**
	 * Returns the propositions contained in this PAS instance same as {@link #getPropositions()}. But also takes a
	 * parameter to specify whether the positive or negated versions of the propositions are required only.
	 * @param positive
	 * @return
	 */
	Iterable<Proposition> getPropositions(boolean positive);
	
	/**
	 * Returns the named proposition if it exists in this PAS instance.
	 * @param name
	 * @return Will return a proposition or null if there's not any.
	 */
	Proposition getProposition(String name, boolean positive);

	/***
	 * Create a proposition with the given name and negation.
	 * @param name A unique name (inside this PAS instance).
	 * @param neg true if the returned proposition instance is to be negated, false otherwise. Both the negated and positive 
	 * versions of the proposition will exist in the PAS instance regardless once the proposition is created.
	 * @return The newly created proposition. If the named proposition already exists in this PAS instance, it should be returned.
	 * @throws KBException If the specified name exists as a literal type other than proposition, this method will throw.
	 */
	Proposition createProposition(String name, boolean neg) throws KBException;
	
	/**
	 * Create an assumption with the given name, probability, and negation.
	 * @param name A unique name (inside this PAS instance).
	 * @param neg true if the returned assumption instance is to be negated, false otherwise. Both the negated and positive 
	 * versions of the assumption will exist in the PAS instance regardless once the assumption is created.
	 * @param probability A value x \in [0,1] that's the probability of the desired assumption.
	 * @return The newly created assumption if created, or if an assumption exactly matching this exists, the existing assumption.
	 * @throws KBException If the specified name exists as a literal type other than assumption, or if the probability specification
	 * doesn't exactly match an existing assumption this method will throw.
	 */
	Assumption createAssumption(String name, boolean neg, double probability) throws KBException;
	
	/**
	 * Adds an existing literal to this PAS instance. The type of the literal will be handled automatically by the PAS instance.
	 * @param lit
	 * @throws KBException This method may throw if there's already a literal with the same name but different characteristics.
	 */
	void addLiteral(Literal lit) throws KBException;
	
	/**
	 * Adds the specified horn clause to the PAS instance. The literals used must already be defined.
	 * @param body
	 * @param head
	 * @return
	 * @throws KBException May throw if the used literals aren't present this PAS instance.
	 */
	boolean addHornClause(Iterable<? extends Literal> body, Literal head) throws KBException;
	
	/**
	 * Parses a given string description for a horn clause and adds it to this PAS instance. The literals
	 * used must already be defined.
	 * @param hc
	 * @return Will return true if the PAS instance was modified (ie. the horn clauses wasn't already present), false otherwise.
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	boolean addHornClause(String hc) throws KBException;
	
	/**
	 * Constructs the specified horn clause but doesn't add it to the PAS instance, only returns it to the caller.
	 * This can be useful for constructing a hypothesis or any other needed propositional expression.
	 * This is not a strict horn clause such that we do not require the literals in the body or head to be non-negated.
	 * @param hcDesc
	 * @return The returned value is a clause, but being a horn clause it should preserve its body and head definitions (e.g. not re-order
	 * its literals).
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	Expression<LogicalOr> constructHornClause(String hcDesc) throws KBException;
	
	/**
	 * Constructs the specified clause using literals defined in the PAS instance, but doesn't add it to the PAS instance.
	 * This can be useful for constructing a hypothesis or any other needed propositional expression.
	 * @param clauseDesc
	 * @return
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	Expression<LogicalOr> constructClause(String clauseDesc) throws KBException;
	
	/**
	 * Constructs the specified term using literals defined in the PAS instance, but doesn't modify the PAS instance in any way.
	 * This can be useful for constructing any needed propositional expression.
	 * @param termDesc
	 * @return
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	Expression<LogicalAnd> constructTerm(String termDesc) throws KBException;

	/**
	 * Constructs the specified conjunctive normal form (CNF) propositional sentence using literals defined in the PAS instance.
	 * @param cnf
	 * @return
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	SimpleSentence<LogicalAnd, LogicalOr> constructCNF(String cnf) throws KBException;
	
	/**
	 * Constructs the specified disjunctive normal form (DNF) propositional sentence using literals defined in the PAS instance.
	 * @param dnf
	 * @return
	 * @throws KBException May throw for various reasons such as parsing problems, or literals not being defined.
	 */
	SimpleSentence<LogicalOr, LogicalAnd> constructDNF(String dnf) throws KBException;
	
	/**
	 * Return the factory that this PAS instance was created with. It helps avoid passing a factory reference
	 * everywhere a PAS instance is given where the receiver may have to create further objects or refer to special objects.
	 * @return The PropFactory used to create the PAS instance.
	 */
	PropFactory getFactory();
}

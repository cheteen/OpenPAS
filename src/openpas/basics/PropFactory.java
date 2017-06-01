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

import java.util.Comparator;
import java.util.regex.Pattern;

import openpas.StringOps;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalConverter;
import openpas.basics.LogicalOps.LogicalOp;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.LogicalOps.Negation;
import openpas.basics.LogicalOps.OperatorException;

/**
 * ProbFactory is the main way to acquire new objects and access singletons that implement the various interfaces in OpenPAS.
 * The objects vary but include: operators, literals, expressions, sentences, and utility objects such as stringers.
 */
public interface PropFactory 
{
	// Operations
	/**
	 * Returns the "and" operator.
	 * @return
	 */
	LogicalAnd getAnd();
	
	/**
	 * Returns the "or" operator.
	 * @return
	 */
	LogicalOr getOr();
	
	/**
	 * Returns the "negation" operator.
	 * @return
	 */
	Negation getNegation();
	
	/**
	 * Returns a comparator that sorts the literals in their natural sorting order
	 * which is their unique index value.
	 * @return
	 */
	Comparator<Literal> getLiteralSorter();
	
	/**
	 * Returns an operator that implements the required class.
	 * @param cls The class of the operator required.
	 * @return An instance of the operator with the specified class.
	 * @throws OperatorException May throw this exception if the operator is not supported.
	 */
	<Op extends LogicalOp> Op getOp(Class<Op> cls) throws OperatorException;
	
	// Literals
	/**
	 * Creates the named proposition. An index number is allocated for each new proposition. The way
	 * to access the negated version of an existing proposition is to {@link Literal#cloneNegated()} it.
	 * @param name A name to identify this proposition. There's no uniqueness constraint during the creation of literals, but when
	 * they're used inside a {@link PAS} instance. The name specified must conform to the regular expression as specified
	 * in {@link #getValidName()}.
	 * @param neg
	 * @return
	 */
	Proposition createProposition(String name, boolean neg);
	
	/**
	 * Creates the named assumption. An index number is allocated for each new assumption. The way
	 * to access the negated version of an existing assumption is to {@link Literal#cloneNegated()} it.
	 * @param name A name to identify this proposition. There's no uniqueness constraint during the creation of literals, but when
	 * they're used inside a {@link PAS} instance. The name specified must conform to the regular expression as specified
	 * in {@link #getValidName()}.
	 * @param neg
	 * @param probability A probability value $x \in [0,1]$.
	 * @return
	 */
	Assumption createAssumption(String name, boolean neg, double probability);
	
	/**
	 * Get the special literal for falsity.
	 * @return
	 */
	Literal getFalse();
	
	/**
	 * Get the special literal for tautology.
	 * @return
	 */
	Literal getTrue();
	
	/**
	 * Returns all known propositions created by this factory. A factory is not required to keep
	 * all the literals it creates if the consumer doesn't keep a reference to those objects such that
	 * the literals returned by this function may contain as subset of the literals that have been created.
	 * @return
	 */
	Iterable<Proposition> getPropositions();

	/**
	 * Returns all known assumptions created by this factory. A factory is not required to keep
	 * all the literals it creates if the consumer doesn't keep a reference to those objects such that
	 * the literals returned by this function may contain as subset of the literals that have been created.
	 * @return
	 */
	Iterable<Assumption> getAssumptions();
	
	/**
	 * Returns a regular expression that is used to validate the names of all new assumptions and propositions.
	 * @return
	 */
	Pattern getValidName();
	
	// Expressions
	/**
	 * Creates an empty clause.
	 * @return
	 */
	Expression<LogicalOr> createClause();
	
	/**
	 * Creates a new clause which contains the given literals.
	 * @param lits
	 * @return
	 */
	Expression<LogicalOr> createClause(Iterable<Literal> lits);

	/**
	 * Creates an empty clause. This may be an ordered clause if specified. A clause is ordered
	 * if it retains the insertion order of the literals it contains. This is useful for creating
	 * horn clauses where the body and head have special meanings.
	 * @param ordered True if the newly created clause will be ordered, false otherwise.
	 * @return
	 */
	Expression<LogicalOr> createClause(boolean ordered);
	
	/**
	 * Creates a new clause which contains the given literals. This may be an ordered clause if
	 * specified. See {@link #createClause()} for more.
	 * @param ordered True if the newly created clause will be ordered, false otherwise.
	 * @param lits
	 * @return
	 */
	Expression<LogicalOr> createClause(boolean ordered, Iterable<Literal> lits);
	
	/**
	 * Creates an empty term.
	 * @return
	 */
	Expression<LogicalAnd> createTerm();
	
	/**
	 * Creates a new term containing the given literals.
	 * @param lits
	 * @return
	 */
	Expression<LogicalAnd> createTerm(Iterable<Literal> lits);
	
	/**
	 * A generic way to create an expression by specifying an operator.
	 * @param op
	 * @return
	 */
	<OpE extends LogicalOp> Expression<OpE> createCustomExpression(OpE op);
	
	/**
	 * A generic way to create an expression by specifying an operator that contains the given literals.
	 * @param op
	 * @param lits
	 * @return
	 */
	<OpE extends LogicalOp> Expression<OpE> createCustomExpression(OpE op, Iterable<Literal> lits);
	
	// Sentences
	/**
	 * Create an empty CNF sentence.
	 * @return
	 */
	SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence();
	
	/**
	 * Creates a new CNF sentence containing the given expressions. For a CNF the expressions must be clauses.
	 * @param exps
	 * @return
	 */
	SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence(Iterable<Expression<LogicalOr>> exps);
	
	/**
	 * Returns an unmodifiable CNF that's True (empty).
	 * @return
	 */
	SimpleSentence<LogicalAnd, LogicalOr> getTrueCNF();
	
	/**
	 * Returns an unmodifiable CNF that's False.
	 * @return
	 */
	SimpleSentence<LogicalAnd, LogicalOr> getFalseCNF();

	/**
	 * Creates an empty DNF sentence.
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence();
	
	/**
	 * Creates an empty DNF sentences that contains the given expressions.
	 * @param exps
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence(Iterable<Expression<LogicalAnd>> exps);
	
	/**
	 * Returns an unmodifiable DNF that's True.
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> getTrueDNF();
	
	/**
	 * Returns an unmodifiable DNF that's False (empty).
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> getFalseDNF();

	/**
	 * Returns an empty sentence that's "similar" to the prototype given. How similar it will be may depend on the implementation
	 * but at the least it needs to match the operators used.
	 * @param protoype
	 * @return
	 */
	<OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentenceLike(SimpleSentence<OpS, OpE> protoype);

	/**
	 * Creates an empty sentence using the given operators.
	 * @param ops
	 * @param ope
	 * @return
	 */
	<OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentece(OpS ops, OpE ope);
	
	// Tools
	/**
	 * Returns an interface that's capable to performing some logical conversions such as converting a DNF -> CNF, or CNF -> DNF.
	 * @return
	 */
	LogicalConverter getConverter();
	
	// StringOps - these are here because there can be factory specific stringising requirements,
	// e.g. the ordering of the literals when stringising expressions.
	/**
	 * Returns the default symboliser. A symboliser is used to convert to string logical concepts such as the operators.
	 * @return
	 */
	
	StringOps.LogicalSmyboliser getDefaultSymboliser();
	/**
	 * Returns the default stringer. A stringer is a class that creates a string representation of logical expressions and sentences.
	 * @return
	 */
	StringOps.LogicalStringer getDefaultStringer();
	
	/**
	 * Returns a stringer that's guaranteed to return horn clause representations of a clause or a CNF. This will only work on 
	 * a clause or a CNF. The stringer may return null when given an expression that's not a clause, or a sentence that's not a CNF.
	 * @return
	 */
	StringOps.LogicalStringer getHornStringer();
}


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

public class Expressions {
	
	public static class UnsupportedConstructException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public UnsupportedConstructException(String why)
		{
			super(why);
		}
	}
	public static class SentenceNotUpdatedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public SentenceNotUpdatedException(String why)
		{
			super(why);
		}
	}
	public static class IllegalOperationException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public IllegalOperationException(String why)
		{
			super(why);
		}
	}
	public static class NonAssumptionLiteralForProbability extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		public NonAssumptionLiteralForProbability(String why)
		{
			super(why);
		}
	}

	/**
	 * Generic interface to represent all supported propositional expressions.
	 * An expression is construed to be a series of literals connected using an operator.
	 * For example, a series of literals connected using BinaryAnd is a term.
	 */
	public static interface Expression<Op extends LogicalOps.LogicalOp>
	{
		/**
		 * Get the operator of this expression.
		 * @return An instance of an operator deriving from LogicalOps.BinaryOp
		 */
		Op getOp();

		/**
		 * Returns a series of literals for this expression. The order may or may not be significant
		 * depending on the type of the expression.
		 * @return
		 */
		Iterable<Literal> getLiterals();
		
		/**
		 * Returns the number of literals in this expression.
		 * @return
		 */
		int getLength();
		
		/**
		 * Returns a clone of this expression. This should retain any characteristics
		 * of the cloned expression (e.g. ordering of literals).
		 * @return
		 */
		Expression<Op> cloneExpression(); // clone container not the literals contained
		
		/**
		 * Returns whether this expression evaluates to falsity, ie. it's always false.
		 * @return
		 */
		boolean isFalse();
		
		/**
		 * Returns whether this expression evaluates to a tautology, ie. it's always true.
		 * @return
		 */
		boolean isTrue();
		
		/**
		 * Returns true if this literal exists in this expression.
		 * @param lit
		 * @return
		 */
		boolean isContained(Literal lit);
		
		/**
		 * Add the given literal to this expression. After this operation the literal should exit in the expression
		 * such that {@link #isContained(Literal)} returns true.
		 * @param lit
		 * @return true if the literal wasn't already in the expression (but is now added). False if the literal is already in.
		 */
		boolean addLiteral(Literal lit);
		
		/**
		 * Remove the given literal from this expression such that it no longer exists in the expression (ie. {@link #isContained(Literal)}
		 * returns false).
		 * @param lit
		 * @return Returns true if the call made the change, False if is the literal already didn't exist in the expression.
		 */
		boolean removeLiteral(Literal lit);

		/**
		 * Adds the series of given literals to the expression, functionaly it's the same as calling addLiteral for each literal.
		 * @param lit
		 * @return Returns "this" for convenience.
		 */
		Expression<Op> addLiterals(Iterable<Literal> lit); // returns this		

		/**
		 * Returns the probability of this expression. This is only defined if all the literals in the expression
		 * are assumptions. If not, then it returns NaN.
		 * This is here for the following reasons:
		 * 1) It should always be a simple operation to computer the probability of an expression.
		 * 2) The probability is fully encapsulated in the expression even if not always defined.
		 * 3) There may be inherent efficiency gains to compute this inside its implementation class.
		 * @return A probability value x \in [0,1] if possible, NaN if not.
		 */
		double computeProbability(); // return NaN if any non-assumptions found
	}
	
	/**
	 * This is the generic interface for defining simple propositional sentences. A simple interface is one that represents
	 * a series of expressions associated using an operator. For example, when a number of terms (expression with BinaryAnd)
	 * are bound using BinaryOr this is a disjunctive normal form (DNF) sentence. When the operators are swapped,
	 * it becomes a conjunctive normal form (CNF) sentence.
	 * 
	 * In a SimpleSentence all the expression contained should be expressions of the same type logically 
	 * and use the same class of operator. For example, if a SimpleSentence contains terms, then all elements contained in it 
	 * should be terms using an operator with the same class (or the identical operator object).
	 */
	public static interface SimpleSentence<OpS extends LogicalOps.LogicalOp, OpE extends LogicalOps.LogicalOp>
	{
		/**
		 * Returns the operator for this sentence.
		 * @return
		 */
		OpS getOp();

		/**
		 * Returns the series of expressions contained in this sentence.
		 * @return
		 * @throws SentenceNotUpdatedException This expression should be thrown if the object recognises that the elements contained
		 * have been modified externally and an accessor method is called before {@link #update()} was called. An accessor
		 * method is any function that provides a result about the state of the object such as 
		 * {@link #hasElement(Expression)}. There's no guarantee that an exception is thrown, and this should not be used
		 * to discover update problems. In general, it's not a good idea to modify elements, but if it has to be done
		 * an {@link #update()} should follow as quickly as possible to update the state of the sentence.
		 */
		Iterable<Expression<OpE>> getElements() throws SentenceNotUpdatedException;
		
		/**
		 * Returns the number of expressions in this sentence.
		 * @return
		 */
		int getLength();

		/**
		 * Returns true if this sentence is a falsity, ie. always evaluates to False.
		 * @return
		 */
		boolean isFalse();
		
		/**
		 * Returns true if this sentence is a tautology, ie. always evaluates to True.
		 * @return
		 */
		boolean isTrue();
		
		/**
		 * Returns true if this sentence has the given expression.
		 * @param el
		 * @return
		 */
		boolean hasElement(Expression<OpE> el);
		
		/**
		 * Adds this expression to this sentence such that {@link #hasElement(Expression)} will return true after this.
		 * @param el
		 * @return Returns true if this method call has changed this sentence, false if the sentence already had the expression.
		 */
		boolean addElement(Expression<OpE> el);
		
		/**
		 * Remove this element from this sentence such as {@link #hasElement(Expression)} will return false after this.
		 * @param el
		 * @return Returns true if the expression was removed, false if the sentence already didn't have the expression.
		 */
		boolean removeElement(Expression<OpE> el);
		
		/**
		 * This is a function that re-constructs the internal state of the sentence from its elements. This should be called
		 * when the expressions contained in the sentence are modified externally.
		 */
		void update();

		/**
		 * Clones this sentence. This should be a deep clone such that all the expressions contained are also cloned.
		 * @return The new cloned sentence.
		 */
		SimpleSentence<OpS, OpE> cloneSimpleSentence();		
	}	
}

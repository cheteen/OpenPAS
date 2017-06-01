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

import java.util.Iterator;

import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;

/**
 * This class defines the logical operators used in OpenPAS.
 * Some of the code here is not tested well and not used much, but two of the fundamental operators BinaryAnd and BinaryOr
 * are defined here.
 */
public abstract class LogicalOps {

	public static class OperatorException extends Exception
	{
		private static final long serialVersionUID = -4740094998937899252L;

		public OperatorException(String error)
		{
			super(error);
		}
	}
	
	/**
	 * Widest definition of a binary logical op. <br>
	 * operate(...) will allocate and return a new expression/sentence, <br>
	 * operateWith(...) will try to use the base expression/sentence and extend it, but may create a new one. <br>
	 */
	public interface LogicalOp
	{
		Expression<? extends LogicalOp> operate(Literal lit1, Literal lit2) throws OperatorException;				

		// Expressions
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operateWith(Expression<? extends LogicalOp> opbase, Expression<? extends LogicalOp> opext)
			throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operate(Expression<? extends LogicalOp> op1, Expression<? extends LogicalOp> op2)
			throws OperatorException;
		
		// Expression vs. sentence
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operateWith(Expression<? extends LogicalOp> expext, SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase)
			throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operate(Expression<? extends LogicalOp> exp, SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
			throws OperatorException;

		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operateWith(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase, Expression<? extends LogicalOp> expext)
			throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operate(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen, Expression<? extends LogicalOp> exp)
			throws OperatorException;

		// Sentences
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
				operate(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen1, 
						SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen2)
						throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
				operateWith(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase, 
							SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senext)
							throws OperatorException;		
	}
	
	public interface BinaryDistributive<OpS extends LogicalOp, OpE extends LogicalOp>
	{
		SimpleSentence<OpS, OpE> distribute(SimpleSentence<OpS, OpE> sen1, SimpleSentence<OpS, OpE> sen2);
	}
	
	public interface LogicalAnd extends LogicalOp, BinaryDistributive<LogicalAnd, LogicalOr>
	{
		// Literals
		Expression<LogicalAnd> and(Literal lit1, Literal lit2);				

		// Expressions
		Expression<LogicalAnd> 
				and(Expression<LogicalAnd> op1, Expression<LogicalAnd> op2);
		Expression<LogicalAnd> 
			andWith(Expression<LogicalAnd> opbase, Expression<LogicalAnd> opext);
		
		// Sentences
		SimpleSentence<LogicalAnd, LogicalOr> 
				and(	SimpleSentence<LogicalAnd, LogicalOr> sen1, 
						SimpleSentence<LogicalAnd, LogicalOr> sen2);
		SimpleSentence<LogicalAnd, LogicalOr> 
			andWith(	SimpleSentence<LogicalAnd, LogicalOr> sen1, 
						SimpleSentence<LogicalAnd, LogicalOr> sen2);
	}

	public interface LogicalOr extends LogicalOp, BinaryDistributive<LogicalOr, LogicalAnd>
	{
		// Literals
		Expression<LogicalOr> or(Literal lit1, Literal lit2);				

		// Expressions
		Expression<LogicalOr> 
				or(Expression<LogicalOr> op1, Expression<LogicalOr> op2);
		Expression<LogicalOr> 
			orWith(Expression<LogicalOr> opbase, Expression<LogicalOr> opext);

		// Sentences
		SimpleSentence<LogicalOr, LogicalAnd> 
				or(SimpleSentence<LogicalOr, LogicalAnd> sen1, 
					SimpleSentence<LogicalOr, LogicalAnd> sen2);
		SimpleSentence<LogicalOr, LogicalAnd> 
			orWith(SimpleSentence<LogicalOr, LogicalAnd> sen1, 
					SimpleSentence<LogicalOr, LogicalAnd> sen2);
	}
	
	public interface UnaryOp
	{
		// Literals
		Literal operate(Literal lit);
		Literal operateWith(Literal lit);
		
		// Expressions
		Expression<? extends LogicalOp> operate(Expression<? extends LogicalOp> exp)
			throws OperatorException;
		/**
		 * May operate on the operand if possible, or will create a new (negated) sentence.
		 */
		Expression<? extends LogicalOp> operateWith(Expression<? extends LogicalOp> exp)
			throws OperatorException;
		
		// Sentences
		/**
		 * Will create a new sentence negating sen.
		 */
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operate(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
			throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> 
			operateWith(SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
			throws OperatorException;
	}
	
	public interface Negation extends UnaryOp	
	{
		// Expressions
		Expression<LogicalAnd> negateClause(Expression<LogicalOr> exp);
		Expression<LogicalAnd> negateWithClause(Expression<LogicalOr> exp);
		
		Expression<LogicalOr> negateTerm(Expression<LogicalAnd> exp);
		Expression<LogicalOr> negateWithTerm(Expression<LogicalAnd> exp);
		
		// Sentences
		SimpleSentence<LogicalOr, LogicalAnd> 
			negateCNF(SimpleSentence<LogicalAnd, LogicalOr> sen);
		SimpleSentence<LogicalOr, LogicalAnd> 
			negateWithCNF(SimpleSentence<LogicalAnd, LogicalOr> sen);
		SimpleSentence<LogicalAnd, LogicalOr> 
			negateDNF(SimpleSentence<LogicalOr, LogicalAnd> sen);
		SimpleSentence<LogicalAnd, LogicalOr> 
			negateWithDNF(SimpleSentence<LogicalOr, LogicalAnd> sen);
	}
	
	// TODO: Need to have a string parsing system to use a factory and the generic ops to create stuff.
	// TODO: Need to make these as part of the factory
	public interface MaterialImplication extends LogicalOp
	{
		// Specific interface
		Expression<LogicalOr> imply(Literal implicant, Literal implicate); // implicant -> implicate
		Expression<LogicalOr> imply(Expression<LogicalAnd> implicant, Expression<LogicalOr> implicate);
		SimpleSentence<LogicalOr, LogicalAnd> imply(SimpleSentence<LogicalAnd, LogicalOr> implicant, SimpleSentence<LogicalOr, LogicalAnd> implicate);
	}
	
	public interface Biconditional extends LogicalOp
	{
		// Specific interface
		SimpleSentence<LogicalAnd, LogicalOr> bicond(Literal l1, Literal l2);
		// Uses DNF for no particular reason, other than having to do it some way.
		SimpleSentence<LogicalOr, LogicalAnd> bicond(
				SimpleSentence<LogicalOr, LogicalAnd> dnf1, 
				SimpleSentence<LogicalOr, LogicalAnd> dnf2);
	}
	
	public interface LogicalConverter
	{
		// Specific interface
		SimpleSentence<LogicalAnd, LogicalOr> convertTerm(Expression<LogicalAnd> term);
		SimpleSentence<LogicalOr, LogicalAnd> convertClause(Expression<LogicalOr> clause);
		
		SimpleSentence<LogicalAnd, LogicalOr> convertDNF(SimpleSentence<LogicalOr, LogicalAnd> dnf);
		SimpleSentence<LogicalOr, LogicalAnd> convertCNF(SimpleSentence<LogicalAnd, LogicalOr> cnf);
	
		// Generic interface
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> convert(LogicalOp op2S, LogicalOp op2E, Expression<? extends LogicalOp> exp)
			throws OperatorException;
		SimpleSentence<? extends LogicalOp, ? extends LogicalOp> convert(LogicalOp op2S, LogicalOp op2E, SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
			throws OperatorException;
	}
	
	// Distribution of an op over an op, e.g. CNF OpS=And, OpE=Or, (a + b)(c + d) + (d + e) = (a + b + d + e)(c + d + e)
	public static class DefaultDistributor<OpS extends LogicalOp, OpE extends LogicalOp> implements BinaryDistributive<OpS, OpE>
	{
		protected PropFactory mFac;
		protected DefaultDistributor(PropFactory fac)
		{
			mFac = fac;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<OpS, OpE> distribute(
				SimpleSentence<OpS, OpE> sen1, SimpleSentence<OpS, OpE> sen2) {
			
			LogicalOp ops = sen1.getOp();
			LogicalOp ope = sen1.getElements().iterator().next().getOp();
			
			SimpleSentence<OpS, OpE> sen = (SimpleSentence<OpS, OpE>) mFac.createSentece(ops, ope);
			Iterator<Expression<OpE>> it1 = sen1.getElements().iterator();			
			while(it1.hasNext())
			{
				Iterator<Expression<OpE>> it2 = sen2.getElements().iterator();
				Expression<OpE> exp1 = it1.next();
				while(it2.hasNext())
					sen.addElement(exp1.cloneExpression().addLiterals(it2.next().getLiterals()));
			}
			return sen;
		}
	}
	
	public static class DefaultConverter implements LogicalConverter
	{
		PropFactory mFac;
		public DefaultConverter(PropFactory fac)
		{
			mFac = fac;
		}
		
		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> convertClause(
				Expression<LogicalOr> clause) {
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			for(Literal lit : clause.getLiterals())
			{
				Expression<LogicalAnd> term = mFac.createTerm();
				term.addLiteral(lit);
				dnf.addElement(term);
			}
			return dnf;
		}
		
		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> convertTerm(
				Expression<LogicalAnd> term) {
			SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
			for(Literal lit : term.getLiterals())
			{
				Expression<LogicalOr> clause = mFac.createClause();
				clause.addLiteral(lit);
				cnf.addElement(clause);
			}
			return cnf;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> convertCNF(
				SimpleSentence<LogicalAnd, LogicalOr> cnf) {
			Iterator<Expression<LogicalOr>> it = cnf.getElements().iterator();
			LogicalOr bor = mFac.getOr();
			SimpleSentence<LogicalOr, LogicalAnd> dnf = convertClause(it.next());
			while(it.hasNext())
			{
				SimpleSentence<LogicalOr, LogicalAnd> dnf2 = convertClause(it.next());
				dnf = bor.distribute(dnf, dnf2);
			}
			return dnf;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> convertDNF(
				SimpleSentence<LogicalOr, LogicalAnd> dnf) {
			Iterator<Expression<LogicalAnd>> it = dnf.getElements().iterator();
			LogicalAnd band = mFac.getAnd();
			SimpleSentence<LogicalAnd, LogicalOr> cnf = convertTerm(it.next());
			while(it.hasNext())
			{
				SimpleSentence<LogicalAnd, LogicalOr> cnf2 = convertTerm(it.next());
				cnf = band.distribute(cnf, cnf2);
			}
			return cnf;
		}

		/**
		 * Will convert an expression to a sentence:
		 * - term -> CNF
		 * - clause -> DNF
		 * - matching expression -> return same
		 */
		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> convert(
				LogicalOp op2S, LogicalOp op2E, Expression<? extends LogicalOp> exp)
				throws OperatorException {
			if(op2E.equals(exp))
			{
				// Nothing to convert, embed expression in appropriate sentence.
				@SuppressWarnings("rawtypes")
				SimpleSentence res = mFac.createSentece(op2S, op2E);
				res.addElement(exp);
				return res;
			}
			if(mFac.getAnd().equals(exp.getOp()) && mFac.getAnd().equals(op2S)
					&& mFac.getOr().equals(op2E))
				return convertTerm((Expression<LogicalAnd>) exp);
			else if(mFac.getOr().equals(exp.getOp()) && mFac.getOr().equals(op2S)
					&& mFac.getAnd().equals(op2E))
				return convertClause((Expression<LogicalOr>) exp);
			else
				throw new OperatorException("Unsupported convert operation.");
				
		}

		/**
		 * Will convert a DNF to CNF, or a CNF to DNF, and return the same sentence if the ops match.
		 */
		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> convert(
				LogicalOp op2S, LogicalOp op2E,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
				throws OperatorException {
			LogicalOp ops = sen.getOp();
			LogicalOp ope = sen.getElements().iterator().next().getOp();
			if(ops.equals(op2S) && ope.equals(op2E))
				return sen; // return same sentence, important in use in xWith methods
			else if(mFac.getAnd().equals(ops) && mFac.getOr().equals(ope) 
					&& mFac.getOr().equals(op2S) && mFac.getAnd().equals(op2E))
				return convertCNF((SimpleSentence<LogicalAnd, LogicalOr>) sen);
			else if(mFac.getOr().equals(ops) && mFac.getAnd().equals(ope)
					&& mFac.getAnd().equals(op2S) && mFac.getOr().equals(op2E))
				return convertDNF((SimpleSentence<LogicalOr, LogicalAnd>) sen);
			else
				throw new OperatorException("Unsupported convert operation.");			
		}
	}	
}

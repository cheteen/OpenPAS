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

package openpas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;

import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOp;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.LogicalOps.MaterialImplication;

/***
 * This currently supports only DNF and CNF sentences properly.
 * May add support for Horn clauses later on.
 * Here's an enumeration of what's what: <br>

		_what_		_logical value_		string form			<br>
		"empty clause" 	false			"(False)" 			<br>
		"empty term"	true			"True"				<br>
															<br>
		"empty CNF"	 	true			"[(True)]"			<br>
		"empty DNF"		false			"[False]"			<br>
															<br>
		"false CNF"		false			"[(False)]"			<br>
		"true DNF"		true			"[True]"			<br>
															<br>
		"regular CNF"	-				"[(a + b)(c)]		<br>
		"regular DNF"	-				"[a b + c]			<br>

* The empty ones could've been left empty and the surrounding various brackets would tell what it is,
* but this way it's quicker to remember which form defaults to what logically.
*/
public class StringOps {
	final static private int DEFAULT_MAX_STR_LEN = 1000;
	
	public static LogicalSmyboliser createLogicalSymboliser()
	{
		return new DefaultSymboliser();
	}
	public static LogicalStringer createStringer(LogicalSmyboliser logSym, Comparator<Literal> literalComparator, int maxStrLen)
	{
		return new DefaultStringerant(logSym, literalComparator, maxStrLen);
	}
	public static LogicalStringer createHornStringer(LogicalSmyboliser logSym, Comparator<Literal> literalComparator, int maxStrLen)
	{
		return new HornStringer(logSym, literalComparator, maxStrLen);
	}

	public static class UnknownOpException extends Exception
	{
		private static final long serialVersionUID = 1L;
		public UnknownOpException(String message) {
			super(message);
		}
	}
	
	static public interface LogicalSmyboliser
	{
		String getSymbol(LogicalOp op) throws UnknownOpException;
		String getNegation();
		String getTrue();
		String getFalse();
		String getImplication();
		void symboliseLiteral(StringBuffer buf, Literal l, boolean negate);
	}
	
	static public interface LogicalStringer
	{
		<OpE extends LogicalOp> String stringise(Expression<OpE> exp);
		<OpS extends LogicalOp, OpE extends LogicalOp> String stringise(SimpleSentence<OpS, OpE> sen);
		long getMaxSize();
	}
	
	static class DefaultSymboliser implements LogicalSmyboliser
	{
		@Override
		public String getSymbol(LogicalOp op) throws UnknownOpException
		{
			if(op instanceof LogicalAnd)
				return "";
			else if(op instanceof LogicalOr)
				return "+";
			else if(op instanceof MaterialImplication)
				return getImplication();
			throw new UnknownOpException("Unknown exception " + op);
		}

		@Override
		public String getNegation() {
			return "¬";
		}

		@Override
		public String getTrue() {
			return "True";
		}

		@Override
		public String getFalse() {
			return "False";
		}

		@Override
		public void symboliseLiteral(StringBuffer buf, Literal l, boolean negate) {
			if(negate ^ l.getNeg())
				buf.append(getNegation());
			buf.append(l.getName());
		}

		@Override
		public String getImplication() {
			return "->";
		}
	}
	
	static class DefaultStringerant implements LogicalStringer
	{
		LogicalSmyboliser mSyer;
		Comparator<Literal> mOrderLits;
		int mMaxStringLen;

		
		public DefaultStringerant(LogicalSmyboliser syer, Comparator<Literal> orderLiterals)
		{
			this(syer, orderLiterals, DEFAULT_MAX_STR_LEN);
		}
		
		/**
		 * Converts a given expression or sentence to string.
		 * @param syer
		 * @param orderLiterals This will be used if given, otherwise the class will attempt to use the default one if set.
		 */
		public DefaultStringerant(LogicalSmyboliser syer, Comparator<Literal> orderLiterals, int maxStringLen)
		{
			mSyer = syer;
			mOrderLits = orderLiterals;
			mMaxStringLen = maxStringLen;
		}
		
		@Override
		public <OpE extends LogicalOp> String stringise(Expression<OpE> exp)
		{
			StringBuffer sb = new StringBuffer();
			
			// The following surrounds clauses with a "(". This emphasises the lower precedence of OR
			// compared to AND. In doing so, it also allows us spot the difference between an term and a clause.
			if(exp.getOp() instanceof LogicalOr)
				sb.append("(");
			if(exp.isTrue())
				sb.append(mSyer.getTrue());
			else if(exp.isFalse())
				sb.append(mSyer.getFalse());
			else
			{
				String sym;
				try 
				{
					sym = mSyer.getSymbol(exp.getOp());
				} catch (UnknownOpException e) {
					sym = "?";
				}
				
				boolean first = true;
				Iterable<Literal> literals;
				if(mOrderLits == null)
					literals = exp.getLiterals();
				else
				{
					List<Literal> lits = new ArrayList<Literal>();
					for(Literal lit : exp.getLiterals())
						lits.add(lit);
					Collections.sort(lits, mOrderLits);
					literals = lits;
				}
				
				for(Literal lit : literals)
				{
					if(first)
						first = false;
					else
					{
						sb.append(" ");
						sb.append(sym);
						if(sym.length() > 0)
							sb.append(" ");
					}

					mSyer.symboliseLiteral(sb, lit, false);
					
					if(sb.length() > mMaxStringLen)
					{
						sb.append("...");
						break;
					}
				}				
			}
			
			if(exp.getOp() instanceof LogicalOr)
				sb.append(")");
			
			return sb.toString();
		}
		
		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> String stringise(SimpleSentence<OpS, OpE> sen)
		{
			StringBuffer sb = new StringBuffer();
			
			sb.append("["); // this helps distinguish between an expression and a sentence easily.
			if(sen.isFalse())
			{
				if(sen.getOp() instanceof LogicalAnd) //adding special clause
					sb.append("(");
				sb.append("False");
				if(sen.getOp() instanceof LogicalAnd)
					sb.append(")");
			}
			else if(sen.isTrue())
			{
				if(sen.getOp() instanceof LogicalAnd) //adding special clause
					sb.append("(");
				sb.append("True");
				if(sen.getOp() instanceof LogicalAnd)
					sb.append(")");				
			}
			else
			{
				String sym;
				try 
				{
					sym = mSyer.getSymbol(sen.getOp());
				} catch (UnknownOpException e) 
				{
					sym = "?";
				}
				
				// pad with extra space for OR since it has the lower precedence
				// AND based expressions won't have brackets.
				if(sen.getOp() instanceof LogicalOr)
					sym = String.format(" %s ", sym);
				
				boolean first = true;
				for(Expression<OpE> exp : sen.getElements())
				{				
					if(first)
						first = false;
					else
						sb.append(sym);
					
					sb.append(stringise(exp));
					
					if(sb.length() > mMaxStringLen - 4)
					{
						sb.append("...");
						break;
					}
				}				
			}
			sb.append("]");
			return sb.toString();
		}

		@Override
		public long getMaxSize() {
			return mMaxStringLen;
		}
	}
	
	static class HornStringer implements LogicalStringer
	{
		LogicalSmyboliser mSyer;
		int mMaxStringLen;
		Comparator<Literal> mOrderLits;

		public HornStringer(LogicalSmyboliser syer, Comparator<Literal> orderLiterals, int maxStringLen)
		{
			mSyer = syer;
			mOrderLits = orderLiterals;
			mMaxStringLen = maxStringLen;
		}

		@Override
		public <OpE extends LogicalOp> String stringise(Expression<OpE> exp) {
			if(!(exp.getOp() instanceof LogicalOr))
				return null; // can only process a clause
			StringBuffer sb = new StringBuffer();

			sb.append("("); // clauses always start with (
			
			if(exp.getLength() > 1)
			{ // We have at least two literals to deal with in the clause.
				
				// We'll iterate over the raw iterable or a sorted one depending on whether we have an order.
				Iterator<Literal> itAll;

				if(mOrderLits != null)
				{
					// If we have an ordering, we'll need to sort the body of the clause, and pick the head from the end:
					List<Literal> listLiterals = FluentIterable.from(exp.getLiterals()).copyInto(new ArrayList<>(exp.getLength()));

					// Sort the body part of the clause
					Collections.sort(listLiterals.subList(0, listLiterals.size() - 1), mOrderLits);
					itAll = listLiterals.iterator();
				}
				else
					itAll = exp.getLiterals().iterator(); // unordered

				// Iterate first over the body.
				Iterator<Literal> itBody = Iterators.limit(itAll, exp.getLength() - 1);
				while(itBody.hasNext())
				{
					Literal l = itBody.next();
					// Negate the body of the clause when symbolising
					mSyer.symboliseLiteral(sb, l, true);
					
					if(sb.length() > mMaxStringLen - 4)
					{
						sb.append("...");
						break;
					}
					if(itBody.hasNext())
						sb.append(" ");
				}
				sb.append(" ");
				sb.append(mSyer.getImplication());
				sb.append(" ");
				// Get the head in the head which won't be negated:
				Literal litHead = itAll.next(); // this should never fail
				mSyer.symboliseLiteral(sb, litHead, false);
			}
			else if(exp.getLength() == 1)
			{
				// We insert the single literal as a free standing head.
				mSyer.symboliseLiteral(sb, exp.getLiterals().iterator().next(), false);
			}
			else
			{ // empty
				assert exp.isFalse(); // empty clause is falsity
				sb.append(mSyer.getFalse());
			}
			sb.append(")");
			return sb.toString();
		}

		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> String stringise(SimpleSentence<OpS, OpE> sen) {
			if(!(sen.getOp() instanceof LogicalAnd))
				return null; // only CNF supported
			if(sen.isFalse())
				return "[(" + mSyer.getFalse() + ")]";
			if(sen.isTrue())
				return "[(" + mSyer.getTrue() + ")]";
			
			// Check the first element for CNF
			if(!(sen.getElements().iterator().next().getOp() instanceof LogicalOr))
				return null;
			
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			
			Iterator<Expression<OpE>> it = sen.getElements().iterator();
			while(it.hasNext())
			{
				Expression<OpE> expr = it.next();
				sb.append(stringise(expr));
				
				if(it.hasNext())
					sb.append(",");
			}
			sb.append("]");
			return sb.toString();
		}

		@Override
		public long getMaxSize() {
			return mMaxStringLen;
		}
	}
}

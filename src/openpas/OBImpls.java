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

package openpas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import openpas.LBImpls.LiteralLBImpl;
import openpas.StringOps.LogicalSmyboliser;
import openpas.StringOps.LogicalStringer;
import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalConverter;
import openpas.basics.LogicalOps.LogicalOp;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.LogicalOps.Negation;
import openpas.basics.LogicalOps.OperatorException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;

public class OBImpls {
	
	private static Comparator<Literal> mLiteralSorter;
	static Comparator<Literal> getOrderLiterals()
	{
		if(mLiteralSorter == null)
			mLiteralSorter = new Comparator<Literal>()
			{
				@Override
				public int compare(Literal o1, Literal o2) {
					LiteralLBImpl lit1 = (LiteralLBImpl) o1;
					LiteralLBImpl lit2 = (LiteralLBImpl) o2;
					return lit1.mIndex - lit2.mIndex; //first come first appear
				}	
			};
		return mLiteralSorter;
	}
	
	static class OBFactory implements PropFactory
	{
		static abstract class OBLiteral implements Literal
		{
			protected int mIndex;
			protected boolean mNeg;
			OBFactory mFac;
			OBLiteral(OBFactory fac, int index, boolean neg) {
				mIndex = index;
				mNeg = neg;
				mFac = fac;
			}
			@Override
			public boolean getNeg() {
				return mNeg;
			}
			@Override
			public Integer getIndex() {
				return mIndex;
			}
		}
		static class OBAssumption extends OBLiteral implements Assumption
		{
			OBAssumption(OBFactory fac, int index, boolean neg) {
				super(fac, index, neg);
			}
			@Override
			public String getName() {
				return mFac.mAsmNames.get(mIndex);
			}
			@Override
			public LiteralType getType() {
				return LiteralType.Assumption;
			}
			@Override
			public boolean isAssumption() {
				return true;
			}
			@Override
			public boolean isProposition() {
				return false;
			}
			@Override
			public boolean isSpecial() {
				return false;
			}
			@Override
			public Literal getNegated() {
				return new OBAssumption(mFac, mIndex, !mNeg);
			}
			@Override
			public double getProbability() {
				return mFac.mAsmProbs.get(mIndex);
			}
		}
		static class OBProposition extends OBLiteral implements Proposition
		{

			OBProposition(OBFactory fac, int index, boolean neg) {
				super(fac, index, neg);
			}
			@Override
			public String getName() {
				return mFac.mPrpNames.get(mIndex);
			}
			@Override
			public LiteralType getType() {
				return LiteralType.Proposition;
			}
			@Override
			public boolean isAssumption() {
				return false;
			}
			@Override
			public boolean isProposition() {
				return true;
			}
			@Override
			public boolean isSpecial() {
				return false;
			}
			@Override
			public Literal getNegated() {
				return new OBProposition(mFac, mIndex, !mNeg);
			}
		}
		static class OBSpecialLiteral extends OBLiteral
		{
			protected String mName;
			protected OBSpecialLiteral mNegation;
			OBSpecialLiteral(OBFactory fac, int index, boolean neg, String name) {
				super(fac, index, neg);
				mName = name;
			}
			@Override
			public String getName() {
				return mName;
			}
			@Override
			public LiteralType getType() {
				return LiteralType.Special;
			}
			@Override
			public boolean isAssumption() {
				return false;
			}
			@Override
			public boolean isProposition() {
				return false;
			}
			@Override
			public boolean isSpecial() {
				return true;
			}
			@Override
			public Literal getNegated() {
				return mNegation;
			}
			
			void setNegation(OBSpecialLiteral negation) {
				mNegation = negation;
			}
		}
		
		// Put these together so they're accessed together in caches
		protected ArrayList<Double> mAsmProbs= new ArrayList<>();
		protected ArrayList<String> mPrpNames = new ArrayList<>();
		protected ArrayList<String> mAsmNames = new ArrayList<>();
		
		protected OBSpecialLiteral mLitTrue;
		protected OBSpecialLiteral mLitFalse;
		
    	private static int DEFAULT_STRINGING_SIZE = 1000;
		protected LogicalSmyboliser mLogSymboliser;
		protected LogicalStringer mLogStringiser;
		protected LogicalStringer mHornStringer;
		
		public OBFactory() {
        	mLogSymboliser = StringOps.createLogicalSymboliser();
        	mLogStringiser = StringOps.createStringer(mLogSymboliser, getOrderLiterals(), DEFAULT_STRINGING_SIZE);
        	mHornStringer = StringOps.createHornStringer(mLogSymboliser, getOrderLiterals(), DEFAULT_STRINGING_SIZE);

			mLitTrue = new OBSpecialLiteral(this, -1, false, mLogSymboliser.getTrue());
			mLitFalse = new OBSpecialLiteral(this, -1, true, mLogSymboliser.getFalse());			
			mLitTrue.setNegation(mLitFalse);
			mLitFalse.setNegation(mLitTrue);
		}
	
		@Override
		public Comparator<Literal> getLiteralSorter() {
			return getOrderLiterals();
		}

		@Override
		public LogicalSmyboliser getDefaultSymboliser() {
			return mLogSymboliser;
		}

		@Override
		public LogicalStringer getDefaultStringer() {
			return mLogStringiser;
		}

		@Override
		public LogicalStringer getHornStringer() {
			return mHornStringer;
		}

		@Override
		public Proposition createProposition(String name, boolean neg) {
			Proposition prp = new OBProposition(this, mPrpNames.size(), neg);
			mPrpNames.add(name);
			return prp;
		}

		@Override
		public Assumption createAssumption(String name, boolean neg, double probability) {
			Assumption asm = new OBAssumption(this, mAsmNames.size(), neg);
			mAsmNames.add(name);
			mAsmProbs.add(probability);
			return asm;
		}

		@Override
		public Literal getFalse() {
			return mLitFalse;
		}

		@Override
		public Literal getTrue() {
			return mLitTrue;
		}

		@Override
		public Iterable<Proposition> getPropositions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<Assumption> getAssumptions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Pattern getValidName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LogicalAnd getAnd() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LogicalOr getOr() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Negation getNegation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <Op extends LogicalOp> Op getOp(Class<Op> cls) throws OperatorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalOr> createClause() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalOr> createClause(Iterable<Literal> lits) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalOr> createClause(boolean ordered) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalOr> createClause(boolean ordered, Iterable<Literal> lits) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalAnd> createTerm() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Expression<LogicalAnd> createTerm(Iterable<Literal> lits) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <OpE extends LogicalOp> Expression<OpE> createCustomExpression(OpE op) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <OpE extends LogicalOp> Expression<OpE> createCustomExpression(OpE op, Iterable<Literal> lits) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence(Iterable<Expression<LogicalOr>> exps) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> getTrueCNF() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> getFalseCNF() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence(Iterable<Expression<LogicalAnd>> exps) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> getTrueDNF() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> getFalseDNF() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentenceLike(
				SimpleSentence<OpS, OpE> protoype) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentece(OpS ops, OpE ope) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LogicalConverter getConverter() {
			// TODO Auto-generated method stub
			return null;
		}		
	}
}

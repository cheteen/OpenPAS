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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import openpas.StringOps.LogicalSmyboliser;
import openpas.StringOps.LogicalStringer;
import openpas.basics.Assumption;
import openpas.basics.Expressions;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SentenceNotUpdatedException;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalConverter;
import openpas.basics.LogicalOps.LogicalOp;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.LogicalOps.Negation;
import openpas.basics.LogicalOps.OperatorException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.utils.ArrayIterable;
import openpas.utils.WeakIterator;

/**
 * This file contains an implementation of a PropFactory and everything created from it which together
 * constitute the propositional engine of OpenPAS.
 *
 * This is a somewhat naive implementation which makes use of Java classes to represent PAS objects with no
 * special (propositional) optimisations. I hope to create another such implementation based on BDDs in
 * the near future which should have better performance.
 */
class LBImpls {
			
	private static LBImplFactory sInstance;	
	static public LBImplFactory getFactory()
	{
		if(sInstance == null)
			sInstance = new LBImplFactory();
		return sInstance;
	}
	
	// Should only be used for testing.
	static LBImplFactory setTestFactory(PropFactory testFac)
	{
		LBImplFactory prev = sInstance;
		sInstance = (LBImplFactory) testFac;
		return prev;
	}
		
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
					return lit1.mIndex - lit2.mIndex;
				}	
			};
		return mLiteralSorter;
	}
	
	static class LBImplFactory implements PropFactory
	{
		private int mLiteralsIndex = 0;
		private NegationImpl mNeg = new NegationImpl(this);
		private BinaryOrImpl mOr = new BinaryOrImpl(this);
		private LogicalAndImpl mAnd = new LogicalAndImpl(this);
				
		List<WeakReference<Assumption>> mAssumptions = new LinkedList<WeakReference<Assumption>>();
		List<WeakReference<Proposition>> mPropositions = new LinkedList<WeakReference<Proposition>>();

    	Map<Integer, Literal> mFalseLiterals;
    	Map<Integer, Literal> mTrueLiterals;
    	
    	private Literal mLitTrue;
    	private Literal mLitFalse;

    	private List<Expression<LogicalAnd>> mTrueTerms;
    	private List<Expression<LogicalOr>> mFalseClauses;
    	
    	private SimpleSentence<LogicalOr, LogicalAnd> mTrueDNF;
    	private SimpleSentence<LogicalOr, LogicalAnd> mFalseDNF;
    	private SimpleSentence<LogicalAnd, LogicalOr> mTrueCNF;
    	private SimpleSentence<LogicalAnd, LogicalOr> mFalseCNF;
    	
    	private static Pattern sValidLiteralName = Pattern.compile("[A-Za-z_0-9#\\.\\;\\:@\\{\\}\\,]+");

    	LogicalConverter mConverter = new LogicalOps.DefaultConverter(this);
    	
    	// This and similar may be read from a config file or a config source of some kind in the future.
    	// No good giving arbitrary params to factory creation - at least for now.
    	private static int DEFAULT_STRINGING_SIZE = 1000;
    	private LogicalSmyboliser mLogSymboliser;
    	private LogicalStringer mLogStringiser;
    	private LogicalStringer mHornStringer;

    	public LBImplFactory()
    	{
        	mLogSymboliser = StringOps.createLogicalSymboliser();
        	mLogStringiser = StringOps.createStringer(mLogSymboliser, getOrderLiterals(), DEFAULT_STRINGING_SIZE);
        	mHornStringer = StringOps.createHornStringer(mLogSymboliser, getOrderLiterals(), DEFAULT_STRINGING_SIZE);

        	mLitTrue = new SpecialLBImpl(this, mLogSymboliser.getTrue(), false, -1);
        	mLitFalse = new SpecialLBImpl(this, mLogSymboliser.getFalse(), true, -1);

        	{
            	Map<Integer, Literal> mapFalseLiterals = new HashMap<Integer, Literal>(1);
            	mapFalseLiterals.put(getFalse().getIndex(), getFalse());
            	mFalseLiterals = Collections.unmodifiableMap(mapFalseLiterals);    		
        	}
        	{
            	Map<Integer, Literal> mapTrueLiterals = new HashMap<Integer, Literal>(1);
            	mapTrueLiterals.put(getTrue().getIndex(), getTrue());
            	mTrueLiterals = Collections.unmodifiableMap(mapTrueLiterals);    		
        	}
        	{
        		List<Expression<LogicalAnd>> listed = new ArrayList<Expression<LogicalAnd>>(1);
        		listed.add(createTerm(new ArrayIterable<Literal>(new Literal[] {getTrue()})));
        		mTrueTerms = Collections.unmodifiableList(listed);
        	}
        	{
        		List<Expression<LogicalOr>> listed = new ArrayList<Expression<LogicalOr>>(1);
        		listed.add(createClause(new ArrayIterable<Literal>(new Literal[] {getFalse()})));
        		mFalseClauses = Collections.unmodifiableList(listed);
        	}
        	
        	// mTrueDNF is not created as unmodifiable because the underlying mTrueTerms is already unmodifiable.
        	mTrueDNF = new DNFImpl(mOr, mTrueTerms, this, false);
        	mFalseDNF = new DNFImpl(mOr, new ArrayList<Expression<LogicalAnd>>(), this, true);

        	// mFalseCNF is not created as unmodifiable because the underlying mFalseClauses is already unmodifiable.
        	mFalseCNF = new CNFImpl(mAnd, mFalseClauses, this, false);
        	mTrueCNF = new CNFImpl(mAnd, new ArrayList<Expression<LogicalOr>>(), this, true);        	
    	}

    	@Override
		public LogicalAnd getAnd() {
			return mAnd;
		}

		@Override
		public LogicalOr getOr() {
			return mOr;
		}

		@Override
		public Negation getNegation() {
			return mNeg;
		}

		@Override
		public Proposition createProposition(String name, boolean neg) {
			if(!sValidLiteralName.matcher(name).matches()) // this really throw an exception
				return null; // refuse to create with invalid name
			Proposition prop = new PropositionLBImpl(name, neg, mLiteralsIndex++);
			mPropositions.add(new WeakReference<Proposition>(prop));
			return prop;
		}

		@Override
		public Assumption createAssumption(String name, boolean neg,
				double probability) {
			if(!sValidLiteralName.matcher(name).matches()) // this really throw an exception
				return null; // refuse to create with invalid name
			Assumption asmt = new AssumptionLBImpl(name, neg, mLiteralsIndex++, probability);
			mAssumptions.add(new WeakReference<Assumption>(asmt));
			return asmt;
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
			return new Iterable<Proposition>() {
				@Override
				public Iterator<Proposition> iterator() 
				{
					return new WeakIterator<Proposition>(mPropositions);
				}
			};
		}

		@Override
		public Iterable<Assumption> getAssumptions() {
			return new Iterable<Assumption> () {
				@Override
				public Iterator<Assumption> iterator() {
					return new WeakIterator<Assumption>(mAssumptions);
				}				
			};
		}

		@Override
		public Expression<LogicalOr> createClause() {
			return new ClauseImpl(getOr(), false, this);
		}

		@Override
		public Expression<LogicalOr> createClause(Iterable<Literal> lits) {
			return new ClauseImpl(getOr(), false, lits, this);
		}

		@Override
		public Expression<LogicalOr> createClause(boolean ordered) {
			return new ClauseImpl(getOr(), ordered, this);
		}

		@Override
		public Expression<LogicalOr> createClause(boolean ordered, Iterable<Literal> lits) {
			return new ClauseImpl(getOr(), ordered, lits, this);
		}

		@Override
		public Expression<LogicalAnd> createTerm() {
			return new TermImpl(getAnd(), false, this);
		}

		@Override
		public Expression<LogicalAnd> createTerm(Iterable<Literal> lits) {
			return new TermImpl(getAnd(), false, lits, this);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <OpE extends LogicalOp> Expression<OpE> createCustomExpression(
				OpE op) {
			if(getAnd().equals(op))
				return (Expression<OpE>) new TermImpl(getAnd(), false, this);
			if(getOr().equals(op))
				return (Expression<OpE>) new ClauseImpl(getOr(), false, this);
			throw new Expressions.UnsupportedConstructException("Unsupported custom expression.");
		}

		@SuppressWarnings("unchecked")
		@Override
		public <OpE extends LogicalOp> Expression<OpE> createCustomExpression(
				OpE op, Iterable<Literal> lits) {
			if(getAnd().equals(op))
				return (Expression<OpE>) new TermImpl(getAnd(), false, lits, this);
			if(getOr().equals(op))
				return (Expression<OpE>) new ClauseImpl(getOr(), false, lits, this);
			throw new Expressions.UnsupportedConstructException("Unsupported custom expression.");			
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence() {
			return new CNFImpl(mAnd, this);
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> createCNFSentence(
				Iterable<Expression<LogicalOr>> exps) {
			return new CNFImpl(mAnd, exps, this, false);
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence() {
			return new DNFImpl(mOr, this);
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> createDNFSentence(
				Iterable<Expression<LogicalAnd>> exps) {
			return new DNFImpl(mOr, exps, this, false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentece(
				OpS ops, OpE ope) {
			if(getAnd().equals(ops) && getOr().equals(ope))
				return (SimpleSentence<OpS, OpE>) createCNFSentence();			
			if(getOr().equals(ops) && getAnd().equals(ope))
				return (SimpleSentence<OpS, OpE>) createDNFSentence();
			
			throw new Expressions.UnsupportedConstructException("Unsupported custom sentence.");
		}

		@Override
		public LogicalConverter getConverter() {
			return mConverter;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <Op extends LogicalOp> Op getOp(Class<Op> cls) throws OperatorException {
			if(cls.equals(LogicalOp.class))
				throw new OperatorException("Must specify specific operator");
			else if(cls.isAssignableFrom(getAnd().getClass()))
				return (Op) getAnd();
			else if(cls.isAssignableFrom(getOr().getClass()))
				return (Op) getOr();
				
			throw new OperatorException("Unsupported operation");				
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> getFalseCNF() {
			return mFalseCNF;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> getTrueDNF() {
			return mTrueDNF;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> getTrueCNF() {
			return mTrueCNF;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> getFalseDNF() {
			return mFalseDNF;
		}

		@Override
		public Pattern getValidName() {
			return sValidLiteralName;
		}

		@Override
		public <OpS extends LogicalOp, OpE extends LogicalOp> SimpleSentence<OpS, OpE> createSentenceLike(
				SimpleSentence<OpS, OpE> protoype) {
			if(protoype.getOp().equals(mAnd)) {
				@SuppressWarnings("unchecked")
				SimpleSentence<OpS, OpE> createDNFSentence = (SimpleSentence<OpS, OpE>) createDNFSentence();
				return createDNFSentence;
			} else if(protoype.getOp().equals(mOr)) {
				@SuppressWarnings("unchecked")
				SimpleSentence<OpS, OpE> createCNFSentence = (SimpleSentence<OpS, OpE>) createCNFSentence();
				return createCNFSentence;
			}
			throw new Expressions.UnsupportedConstructException("Unsupported simple sentence.");
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
		public Comparator<Literal> getLiteralSorter() {
			return getOrderLiterals();
		}
	}
	
	private static class OpImpl<OpS extends LogicalOp, OpE extends LogicalOp> extends LogicalOps.DefaultDistributor<OpS, OpE>
	{
		public OpImpl(PropFactory fac)
		{
			super(fac);
		}
	}

	private static class LogicalAndImpl extends OpImpl<LogicalAnd, LogicalOr> implements LogicalAnd 
	{
		public LogicalAndImpl(PropFactory fac) {
			super(fac);
		}

		// Straight case is 
		// Op = and
		// -- CNF --
		// OpS = and
		// OpE = or

		// Generic methods
		@Override
		public Expression<? extends LogicalOp> operate(Literal lit1, Literal lit2)
				throws OperatorException {
			return and(lit1, lit2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				Expression<? extends LogicalOp> op1,
				Expression<? extends LogicalOp> op2) throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnf1 = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), op1);
			SimpleSentence<LogicalAnd, LogicalOr> cnf2 =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), op2);
			
			return and(cnf1, cnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				Expression<? extends LogicalOp> opbase,
				Expression<? extends LogicalOp> opext) throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnfBase = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), opbase);
			SimpleSentence<LogicalAnd, LogicalOr> cnfExt =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), opext);
			
			return andWith(cnfBase, cnfExt);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen1,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen2)
				throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnf1 = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), sen1);
			SimpleSentence<LogicalAnd, LogicalOr> cnf2 =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), sen2);

			return and(cnf1, cnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senext)
				throws OperatorException 
		{
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnf = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), senbase);
			SimpleSentence<LogicalAnd, LogicalOr> cnfExt =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), senext);

			return andWith(cnf, cnfExt);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				Expression<? extends LogicalOp> exp,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
				throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnf1 =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), sen);
			SimpleSentence<LogicalAnd, LogicalOr> cnf2 = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), exp);

			return andWith(cnf1, cnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				Expression<? extends LogicalOp> expext,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase)
				throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalAnd, LogicalOr> cnf = 
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), senbase);
			SimpleSentence<LogicalAnd, LogicalOr> cnfExt =
				(SimpleSentence<LogicalAnd, LogicalOr>) mFac.getConverter().convert(this, mFac.getOr(), expext);

			return andWith(cnf, cnfExt);
		}

		// Commutative methods
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen,
				Expression<? extends LogicalOp> exp) throws OperatorException {
			return operate(exp, sen);
		}

		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase,
				Expression<? extends LogicalOp> expext) throws OperatorException {
			return operateWith(expext, senbase);
		}

		// Specific methods
		@Override
		public Expression<LogicalAnd> and(Literal lit1, Literal lit2) {
			return mFac.createTerm(new ArrayIterable<Literal>(new Literal[] {lit1, lit2}));
		}

		@Override
		public Expression<LogicalAnd> and(Expression<LogicalAnd> op1,
				Expression<LogicalAnd> op2) {
			Expression<LogicalAnd> newexp = mFac.createTerm();
			for(Literal lit : op1.getLiterals())
				newexp.addLiteral(lit);
			for(Literal lit : op2.getLiterals())
				newexp.addLiteral(lit);
			return newexp;
		}

		@Override
		public Expression<LogicalAnd> andWith(Expression<LogicalAnd> opbase,
				Expression<LogicalAnd> opext) {
			for(Literal lit : opext.getLiterals())
				opbase.addLiteral(lit);
			return opbase;			
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> and(
				SimpleSentence<LogicalAnd, LogicalOr> sen1,
				SimpleSentence<LogicalAnd, LogicalOr> sen2) {
			SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
			for(Expression<LogicalOr> exp : sen1.getElements())
				cnf.addElement(exp);
			for(Expression<LogicalOr> exp : sen2.getElements())
				cnf.addElement(exp);
			return cnf;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> andWith(
				SimpleSentence<LogicalAnd, LogicalOr> sen1,
				SimpleSentence<LogicalAnd, LogicalOr> sen2) {
			for(Expression<LogicalOr> exp : sen2.getElements())
				sen1.addElement(exp);
			return sen1;
		}
	}

	private static class BinaryOrImpl extends OpImpl<LogicalOr, LogicalAnd> implements LogicalOr
	{
		public BinaryOrImpl(PropFactory fac) {
			super(fac);
		}

		// Straight case
		// Op = or
		// -- DNF --
		// OpS = or
		// OpE = and

		// Generic methods		
		@Override
		public Expression<? extends LogicalOp> operate(Literal lit1, Literal lit2)
				throws OperatorException {
			return or(lit1, lit2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				Expression<? extends LogicalOp> op1,
				Expression<? extends LogicalOp> op2) throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnf1 = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), op1);
			SimpleSentence<LogicalOr, LogicalAnd> dnf2 =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), op2);
			
			return or(dnf1, dnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				Expression<? extends LogicalOp> opbase,
				Expression<? extends LogicalOp> opext) throws OperatorException {

			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnfBase = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), opbase);
			SimpleSentence<LogicalOr, LogicalAnd> dnfExt =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), opext);
			
			return orWith(dnfBase, dnfExt);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen,
				Expression<? extends LogicalOp> exp) throws OperatorException {

			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnf1 = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), sen);
			SimpleSentence<LogicalOr, LogicalAnd> dnf2 =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), exp);
			
			return or(dnf1, dnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase,
				Expression<? extends LogicalOp> expext) throws OperatorException {

			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnfBase = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), senbase);
			SimpleSentence<LogicalOr, LogicalAnd> dnfExt =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), expext);

			return orWith(dnfBase, dnfExt);
		}

		// Commutative methods
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				Expression<? extends LogicalOp> exp,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen)
				throws OperatorException {
			return operate(sen, exp);
		}

		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				Expression<? extends LogicalOp> expext,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase)
				throws OperatorException {
			return operate(senbase, expext);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen1,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen2)
				throws OperatorException {
			
			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnf1 = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), sen1);
			SimpleSentence<LogicalOr, LogicalAnd> dnf2 =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), sen2);

			return or(dnf1, dnf2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senbase,
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> senext)
				throws OperatorException {

			// convert to straight case as needed
			SimpleSentence<LogicalOr, LogicalAnd> dnfbase = 
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), senbase);
			SimpleSentence<LogicalOr, LogicalAnd> dnfext =
				(SimpleSentence<LogicalOr, LogicalAnd>) mFac.getConverter().convert(this, mFac.getAnd(), senext);
			
			return orWith(dnfbase, dnfext);
		}

		// Specific methods
		@Override
		public Expression<LogicalOr> or(Literal lit1, Literal lit2) {
			return mFac.createClause(new ArrayIterable<Literal>(new Literal[] {lit1, lit2}));			
		}

		@Override
		public Expression<LogicalOr> or(Expression<LogicalOr> op1,
				Expression<LogicalOr> op2) {
			Expression<LogicalOr> newexp = mFac.createClause();
			for(Literal lit : op1.getLiterals())
				newexp.addLiteral(lit);
			for(Literal lit : op2.getLiterals())
				newexp.addLiteral(lit);
			return newexp;
		}

		@Override
		public Expression<LogicalOr> orWith(Expression<LogicalOr> opbase,
				Expression<LogicalOr> opext) {
			for(Literal lit : opext.getLiterals())
				opbase.addLiteral(lit);
			return opbase;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> or(
				SimpleSentence<LogicalOr, LogicalAnd> sen1,
				SimpleSentence<LogicalOr, LogicalAnd> sen2) {
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			for(Expression<LogicalAnd> exp : sen1.getElements())
				dnf.addElement(exp);
			for(Expression<LogicalAnd> exp : sen2.getElements())
				dnf.addElement(exp);
			return dnf;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> orWith(
				SimpleSentence<LogicalOr, LogicalAnd> senbase,
				SimpleSentence<LogicalOr, LogicalAnd> senext) {
			for(Expression<LogicalAnd> exp : senext.getElements())
				senbase.addElement(exp);
			return senbase;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static class NegationImpl extends OpImpl implements Negation
	{
		public NegationImpl(PropFactory fac) {
			super(fac);
		}

		@Override
		public Literal operate(Literal lit) {
			return lit.getNegated();
		}

		@Override
		public Literal operateWith(Literal lit) {
			return lit.getNegated();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<? extends LogicalOp> operate(
				Expression<? extends LogicalOp> exp) 
				throws OperatorException {
			if(mFac.getAnd().equals(exp.getOp()))
			{
				return negateClause((Expression<LogicalOr>) exp);
			}
			else if(mFac.getOr().equals(exp.getOp()))
			{
				return negateTerm((Expression<LogicalAnd>) exp);
			}
			throw new OperatorException("Unsupported negation");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Expression<? extends LogicalOp> operateWith(
				Expression<? extends LogicalOp> exp) 
				throws OperatorException
		{
			if(mFac.getAnd().equals(exp.getOp()))
			{
				return negateWithClause((Expression<LogicalOr>) exp);
			}
			else if(mFac.getOr().equals(exp.getOp()))
			{
				return negateWithTerm((Expression<LogicalAnd>) exp);
			}
			throw new OperatorException("Unsupported negation");
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operate(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen) throws OperatorException
		{
			if(mFac.getAnd().equals(sen.getOp()))
			{
				return negateDNF((SimpleSentence<LogicalOr, LogicalAnd>) sen);
			}
			else if(mFac.getOr().equals(sen.getOp()))
			{
				return negateCNF((SimpleSentence<LogicalAnd, LogicalOr>) sen);
			}
			throw new OperatorException("Unsupported negation");
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleSentence<? extends LogicalOp, ? extends LogicalOp> operateWith(
				SimpleSentence<? extends LogicalOp, ? extends LogicalOp> sen) throws OperatorException
		{
			if(mFac.getAnd().equals(sen.getOp()))
			{
				return negateWithDNF((SimpleSentence<LogicalOr, LogicalAnd>) sen);
			}
			else if(mFac.getOr().equals(sen.getOp()))
			{
				return negateWithCNF((SimpleSentence<LogicalAnd, LogicalOr>) sen);
			}
			throw new OperatorException("Unsupported negation");
		}

		@Override
		public Expression<LogicalAnd> negateClause(Expression<LogicalOr> exp) {
			Expression<LogicalAnd> trm = mFac.createTerm();
			for(Literal lit : exp.getLiterals())
				trm.addLiteral(lit.getNegated());
			return trm;				
		}

		@Override
		public Expression<LogicalAnd> negateWithClause(Expression<LogicalOr> exp) {
			return negateClause(exp);
		}

		@Override
		public Expression<LogicalOr> negateTerm(Expression<LogicalAnd> exp) {
			Expression<LogicalOr> cla = mFac.createClause();
			for(Literal lit : exp.getLiterals())
				cla.addLiteral(lit.getNegated());
			return cla;
		}

		@Override
		public Expression<LogicalOr> negateWithTerm(Expression<LogicalAnd> exp) {
			return negateTerm(exp);
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> negateCNF(
				SimpleSentence<LogicalAnd, LogicalOr> sen) {
			SimpleSentence<LogicalOr, LogicalAnd> negSen = mFac.createDNFSentence();
			for(Expression<LogicalOr> exp : sen.getElements())
				negSen.addElement(negateClause(exp));
			return negSen;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> negateDNF(
				SimpleSentence<LogicalOr, LogicalAnd> sen) {
			SimpleSentence<LogicalAnd, LogicalOr> negSen = mFac.createCNFSentence();
			for(Expression<LogicalAnd> exp : sen.getElements())
				negSen.addElement(negateTerm(exp));
			return negSen;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> negateWithCNF(
				SimpleSentence<LogicalAnd, LogicalOr> sen) {
			return negateCNF(sen);
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> negateWithDNF(
				SimpleSentence<LogicalOr, LogicalAnd> sen) {
			return negateDNF(sen);
		}
		
	}
	
	protected static abstract class LiteralLBImpl implements Literal
	{		
		protected String mName;
		protected boolean mNeg;
		protected Integer mIndex; // TODO: Convert this to be int, this is quite confusing like this.
		
		protected LiteralLBImpl(String name, boolean neg, int index)
		{
			mName = name;
			mNeg = neg;
			mIndex = index;
		}
		@Override
		public String getName() { return mName; }
		@Override
		public boolean getNeg() { return mNeg; }
		@Override
		public Integer getIndex() { return mIndex; }
		@Override
		public abstract LiteralType getType();		
		@Override
		public boolean isAssumption() { return getType() == LiteralType.Assumption; }
		@Override
		public boolean isProposition() { return getType() == LiteralType.Proposition; }
		@Override
		public boolean isSpecial() { return getType() == LiteralType.Special; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((mIndex == null) ? 0 : mIndex.hashCode());
			result = prime * result + (mNeg ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LiteralLBImpl other = (LiteralLBImpl) obj;
			if (mIndex == null) {
				if (other.mIndex != null)
					return false;
			} else if (!mIndex.equals(other.mIndex))
				return false;
			if (mNeg != other.mNeg)
				return false;
			return true;
		}
	}
	
	private static class SpecialLBImpl extends LiteralLBImpl
	{
		LBImplFactory mFac; // Specials needs access to the factory to compare against singletons.
		protected SpecialLBImpl(LBImplFactory fac, String name, boolean neg, int index)
		{
			super(name, neg, index);
			mFac = fac;
		}

		@Override
		public Literal getNegated() {
			if(this == mFac.getTrue())
				return mFac.getFalse();
			if(this == mFac.getFalse())
				return mFac.getTrue();
			return null;// should probably throw runtime error, should never happen.
		}

		@Override
		public LiteralType getType() {
			return LiteralType.Special;
		}
	}

	private static class AssumptionLBImpl extends LiteralLBImpl implements Assumption
	{
		protected double mProbability;
		
		protected AssumptionLBImpl(String name, boolean neg, int index, double probability)
		{
			super(name, neg, index);
			mProbability = probability;
		}
	
		@Override
		public LiteralType getType() {
			return LiteralType.Assumption;
		}		

		@Override
		public double getProbability() { return mProbability; }
		
		// TODO: Need to test 1-p stuff below
		@Override
		public Literal getNegated() {
			return new AssumptionLBImpl(mName, !mNeg, mIndex, 1 - mProbability);
		} 		
	}

	private static class PropositionLBImpl extends LiteralLBImpl implements Proposition
	{
		protected PropositionLBImpl(String name, boolean neg, int index)
		{
			super(name, neg, index);
		}
		
		@Override
		public LiteralType getType() {
			return LiteralType.Proposition;
		}
		
		@Override
		public Literal getNegated() {
			return new PropositionLBImpl(mName, !mNeg, mIndex);
		}
	}
	
	protected static abstract class CIExpressionLBImpl<OpE extends LogicalOp> implements Expression<OpE>
	{
		protected Map<Integer, Literal> mLiterals;
		protected final OpE mOp;
		protected LBImplFactory mFac;
		protected boolean mOrdered;
		
		public CIExpressionLBImpl(OpE op, boolean ordered, LBImplFactory fac)
		{
			mOp = op;
			mOrdered = ordered;
			if(ordered) // this is optional because it adds unnecessary tracking where not needed
				mLiterals = new LinkedHashMap<Integer, Literal>();
			else
				mLiterals = new HashMap<Integer, Literal>();
			mFac = fac;
		}
		
		public CIExpressionLBImpl(OpE op, boolean ordered, Iterable<Literal> literals, LBImplFactory fac)
		{
			this(op, ordered, fac);
			for(Literal lit : literals)
				addLiteral(lit);
		}
		
		@Override
		public String toString() {
			return mFac.getDefaultStringer().stringise(this);
		}

		@Override
		public boolean isContained(Literal lit) {
			return mLiterals.containsValue(lit);
		}

		@Override
		public boolean removeLiteral(Literal lit) {
			return mLiterals.remove(lit.getIndex()) != null;
		}

		public Iterable<Literal> getLiterals() 
		{
			if(mLiterals.isEmpty())
			{
				if(isTrue())
					return mFac.mTrueLiterals.values();
				else
				{
					assert isFalse();
					return mFac.mFalseLiterals.values();
				}
			}
			return mLiterals.values(); 
		} 
		public OpE getOp() { return mOp; }

		@Override
		public Expression<OpE> addLiterals(Iterable<Literal> lit) {
			for(Literal li : lit)
				addLiteral(li);
			return this;
		}

		@Override
		public int getLength() {
			return mLiterals.size();
		}

		@Override
		public boolean isFalse() {
			return mLiterals == mFac.mFalseLiterals;
		}

		@Override
		public boolean isTrue() {
			return mLiterals == mFac.mTrueLiterals;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((mLiterals == null) ? 0 : mLiterals.hashCode());
			result = prime * result + ((mOp == null) ? 0 : mOp.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CIExpressionLBImpl<OpE> other = (CIExpressionLBImpl<OpE>) obj;
			if (mLiterals == null) {
				if (other.mLiterals != null)
					return false;
			} else if (!mLiterals.equals(other.mLiterals))
				return false;
			if (mOp == null) {
				if (other.mOp != null)
					return false;
			} else if (!mOp.equals(other.mOp))
				return false;
			return true;
		}
	}
	
	private static class TermImpl extends CIExpressionLBImpl<LogicalAnd>
	{
		public TermImpl(LogicalAnd op, boolean ordered, Iterable<Literal> literals, LBImplFactory fac)
		{
			super(op, ordered, literals, fac);
		}
		
		public TermImpl(LogicalAnd op, boolean ordered, LBImplFactory fac)
		{
			super(op, ordered, fac);
		}
		
		@Override
		public boolean isTrue()
		{
			return mLiterals.isEmpty();
		}
		
		@Override
		public Expression<LogicalAnd> cloneExpression() 
		{
			return new TermImpl(mOp, mOrdered, mLiterals.values(), mFac);
		}

		@Override
		public boolean addLiteral(Literal lit) 
		{
			if(isFalse())
				return false;
			else if(lit.equals(mFac.getFalse()))
			{
				mLiterals = mFac.mFalseLiterals; // point of no return
				return true;
			}
			else if(lit.equals(mFac.getTrue()))
				return false; // no effect
			else
			{
				Literal exst = mLiterals.get(lit.getIndex());
				if(exst != null)
				{
					// If we already have this literal can't add it.
					if(exst.getNeg() == lit.getNeg())
						return false;
					else // This is the negation of an existing literal, which falsifies the term.
						return addLiteral(mFac.getFalse());
				}				
			}
			
			Literal prev = mLiterals.put(lit.getIndex(), lit);
			assert prev == null;
			return true;
		}
		
		@Override
		public double computeProbability() {
			// Handle special cases first
			if(isFalse())
				return 0;
			if(isTrue())
				return 1;
			double p = 1;
			for(Literal l : mLiterals.values())
			{
				if(l.getType() != Literal.LiteralType.Assumption)
					return Double.NaN; // undefined when expression contains proposals
				p *= ((Assumption) l).getProbability();
			}
			return p;
		}
	}
	
	private static class ClauseImpl extends CIExpressionLBImpl<LogicalOr>
	{
		public ClauseImpl(LogicalOr op, boolean ordered, Iterable<Literal> literals, LBImplFactory fac)
		{
			super(op, ordered, literals, fac);
		}
		
		public ClauseImpl(LogicalOr op, boolean ordered, LBImplFactory fac)
		{
			super(op, ordered, fac);
		}
		
		@Override
		public boolean isFalse() {
			return mLiterals.isEmpty();
		}

		@Override
		public Expression<LogicalOr> cloneExpression() 
		{
			return new ClauseImpl(mOp, mOrdered, mLiterals.values(), mFac);
		}

		@Override
		public boolean addLiteral(Literal lit) {
			if(isTrue())
				return false;
			else if(lit.equals(mFac.getFalse()))
				return false;
			else if(lit.equals(mFac.getTrue()))
			{
				mLiterals = mFac.mTrueLiterals; // point of no return
				return true;
			}
			else
			{
				Literal exst = mLiterals.get(lit.getIndex());
				if(exst != null)
				{
					if(exst.getNeg() == lit.getNeg())
						return false;
					else
						return addLiteral(mFac.getTrue()); // TODO: Need to confirm that this is the correct behaviour.
				}				
			}
			
			Literal prev = mLiterals.put(lit.getIndex(), lit);
			assert prev == null;
			return true;
		}
		
		@Override
		public double computeProbability() {
			// Handle special cases first
			if(isFalse())
				return 0;
			if(isTrue())
				return 1;
			double p = 0;
			for(Literal l : mLiterals.values())
			{
				if(l.getType() != Literal.LiteralType.Assumption)
					return Double.NaN; // undefined when expression contains proposals
				// Use inclusion-exclusion rule on disjunct expressions since all
				// assumptions are assumed to be disjunct (probabilistically independent).
				p = 1 - (1 - p) * (1 - ((Assumption) l).getProbability());
			}
			return p;
		}
	}

	private static abstract class SimpleSentenceImpl<OpS extends LogicalOps.LogicalOp, OpE extends LogicalOps.LogicalOp> 
							implements Expressions.SimpleSentence<OpS, OpE>
	{
		protected List<Expression<OpE>> mElements;
		OpS mOp;
		LBImplFactory mFac;

		transient Set<Expression<OpE>> mBagElements; // cache
		transient int mLastHash; // to notice undated sentences
		
		public SimpleSentenceImpl(OpS op, LBImplFactory fac)
		{
			mOp = op;
			mElements = new LinkedList<Expressions.Expression<OpE>>();
			mFac = fac;
		}

		public SimpleSentenceImpl(OpS op, Iterable<Expressions.Expression<OpE>> elements, LBImplFactory fac, boolean isUnmodifiable)
		{
			this(op, fac);
			for(Expressions.Expression<OpE> el : elements)
				addElement(el);

			if(isUnmodifiable)
				mElements = Collections.unmodifiableList(mElements);
		}
		
		@Override
		public String toString() {
			return mFac.getDefaultStringer().stringise(this);
		}

		@Override
		public int hashCode() {
			// This will return the same hashcode with sentences with the same elements.
			// This means the set is treated like a bag of elements.
			// This is so that we optimise against adding/removing elements to a sentence
			// but don't use a set, and so don't get the overhead of keeping a sorted list or a hash table.
			int result = 0;
			for(Expression<OpE> elt : mElements)
				result += elt.hashCode();
			
			// Opportunistcally check if the underlying sentence has been modified with out telling us.
			if(mLastHash != 0 && mLastHash != result)
				throw new SentenceNotUpdatedException("Sentence not updated after expressions are modified.");
			mLastHash = result;
			
			return result;
		}

		// We don't expect to get a lot of .equals checks, so this is not very optimised.
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			// mElements is never null - so don't waste time checking.
			SimpleSentenceImpl<OpS, OpE> other = (SimpleSentenceImpl<OpS, OpE>) obj;
			// Try the easy ones first.
			if(mBagElements != null && other.mBagElements != null)
				return mBagElements.equals(other.mBagElements);
			else if (hashCode() != obj.hashCode())
				return false;
			else
			{// Ok so we have two sentences with matching hashcodes, these are very likely the same object,
			 // but we need compare them as a bag of elements to make sure.	
				Set<Expression<OpE>> cache;
				List<Expression<OpE>> target;
				// If the other object already has a cache we can make use of it.
				if(mBagElements != null)
				{
					cache = mBagElements;
					target = other.mElements;
				}
				else if(other.mBagElements != null)
				{
					cache = other.mBagElements;
					target = mElements;
				}
				else
				{
					cache = new HashSet<>();
					for(Expression<OpE> elt : mElements)
						cache.add(elt);
		 			// If end up creating a set of our elements, try to hold on to it as long as possible
					// because it's expensive to create it.
					cache = Collections.unmodifiableSet(cache);
					mBagElements = cache;
					target = other.mElements;					
				}
				
				for(Expression<OpE> elt : target)
					if(!cache.contains(elt))
						return false;

				// The two sentences are equal, let's share the cache as much as possible.
				if(mBagElements == null)
					mBagElements = cache;
				if(other.mBagElements == null)
					other.mBagElements = cache;
			}
			return true;
		}

		@Override
		public OpS getOp() {
			return mOp;
		}

		@Override
		public int getLength() {
			return mElements.size();
		}		

		@Override
		public boolean hasElement(Expression<OpE> el) {
			return mElements.contains(el);
		}
		
		private void clearCache()
		{
			if(mBagElements != null)
				mBagElements = null; // clear cache when internal state changes
			if(mLastHash != 0)
				mLastHash = 0; // let's use 0 as a special value
		}

		@Override
		public boolean removeElement(Expression<OpE> el) {
			boolean updated = removeElement_Internal(el);
			if(updated)
				clearCache();
			return updated;
		}
		protected boolean removeElement_Internal(Expression<OpE> el)
		{
			return mElements.remove(el);			
		}
		
		@Override
		public boolean addElement(Expression<OpE> el) 
		{
			boolean updated = addElement_Internal(el);
			if(updated)
				clearCache();
			return updated;
		}
		abstract protected boolean addElement_Internal(Expression<OpE> el);

		/**
		 * This is to update the sentence for logical consistency. In particular, it should only be 
		 * needed if a contained expression is emptied after it's been inserted into the sentence.
		 * This is an expensive operation.
		 */
		@Override
		public void update() {
			List<Expressions.Expression<OpE>> currentElts = mElements;
			mElements = new LinkedList<Expressions.Expression<OpE>>();
			for(Iterator<Expression<OpE>> it = currentElts.iterator(); it.hasNext(); )
			{
				Expression<OpE> exp = it.next();
				it.remove();
				addElement(exp);
			}
		}
		
		public static class ExpsIterable<OpE extends LogicalOp> implements Iterable<Expression<OpE>>
		{
			Iterable<Expression<OpE>> mElements;
			ExpsIterable(Iterable<Expression<OpE>> elements)
			{
				mElements = elements;
			}
			
			@Override
			public Iterator<Expression<OpE>> iterator() {
				return new ExpsIterator<OpE>(mElements);
			}	
		}
		public static class ExpsIterator<OpE extends LogicalOp> implements Iterator<Expression<OpE>>
		{
			Iterable<Expression<OpE>> mElements;
			Iterator<Expression<OpE>> mIteElements;
			
			public ExpsIterator(Iterable<Expression<OpE>> elements)
			{
				mElements = elements;
				mIteElements = mElements.iterator();
			}
			
			@Override
			public boolean hasNext() {
				return mIteElements.hasNext();
			}

			@Override
			public Expression<OpE> next() {
				return mIteElements.next();
			}

			/***
			 * Throws Expressions.IllegalOperationExceptionl, operation not supported.
			 */
			@Override
			public void remove() {
				throw new Expressions.IllegalOperationException("You can't remove an element using an iterator. Use removeElement()");
			}
		}
	}
		
	private static class CNFImpl extends SimpleSentenceImpl<LogicalAnd, LogicalOr>
	{
		public CNFImpl(LogicalAnd op, LBImplFactory fac) {
			super(op, fac);
		}

		public CNFImpl(LogicalAnd op, Iterable<Expression<LogicalOr>> elements, LBImplFactory fac, boolean isUnmodifiable) {
			super(op, elements, fac, isUnmodifiable);
		}

		protected boolean addElement_Internal(Expression<LogicalOr> el) {
			// Handle special cases
			if(isFalse()) // already at point of no return
				return false;
			else if(el.isFalse())
			{
				mElements = mFac.mFalseClauses; // point of no return
				return true;
			}
			else if(el.isTrue()) // short-cut to avoid \mu below
				return false;

			// \mu operation - keep only minimal clauses
			CIExpressionLBImpl<LogicalOr> lbel = (CIExpressionLBImpl<LogicalOr>) el;
			int lenEl = el.getLength();
			
			for(Iterator<Expression<LogicalOr>> it = mElements.iterator(); it.hasNext(); )
			{
				Expression<LogicalOr> elin = it.next();
				CIExpressionLBImpl<LogicalOr> lbelin = (CIExpressionLBImpl<LogicalOr>) elin;

				// Check if remove existing clause is longer, then remove.
				if(elin.getLength() >= lenEl 
						&& lbelin.mLiterals.values()
							.containsAll(lbel.mLiterals.values()))
				{
					it.remove(); 
					continue;
				}
				
				if(lenEl >= elin.getLength()
						&& lbel.mLiterals.values()
							.containsAll(lbelin.mLiterals.values()))
				{
					return false;
				}
			}
			
			// Ready to add the new clause
			mElements.add(el);
			return true;
		}

		@Override
		public SimpleSentence<LogicalAnd, LogicalOr> cloneSimpleSentence() {
			List<Expression<LogicalOr>> clonedElts = new ArrayList<Expressions.Expression<LogicalOr>>(mElements.size());
			for(Expression<LogicalOr> elt : mElements)
				clonedElts.add(elt.cloneExpression());
			return new CNFImpl(mOp, clonedElts, mFac, false);
		}

		@Override
		public boolean isFalse() {
			return mElements == mFac.mFalseClauses; // clause with special literal \bot
		}

		@Override
		public boolean isTrue() {
			return mElements.isEmpty(); // this returns false for the false case which has the special literal \bot
		}
		
		@Override
		public Iterable<Expression<LogicalOr>> getElements() {
			// TODO: Would be nice to cache these. iterator() creates a new iterator in any case.
			return new ExpsIterable<LogicalOr>(mElements); 
		}
	}
	
	private static class DNFImpl extends SimpleSentenceImpl<LogicalOr, LogicalAnd>
	{
		public DNFImpl(LogicalOr op, LBImplFactory fac) {
			super(op, fac);
		}

		public DNFImpl(LogicalOr op, Iterable<Expression<LogicalAnd>> elements, LBImplFactory fac, boolean isUnmodifiable) {
			super(op, elements, fac, isUnmodifiable);
		}
	
		protected boolean addElement_Internal(Expression<LogicalAnd> el) 
		{	
			if(isTrue()) // already at point of no return
				return false;
			if(el.isTrue())
			{
				mElements = mFac.mTrueTerms; // point of no return
				return true;
			}
			else if(el.isFalse()) //short-cut to avoid \mu below
				return false;
			
			// \mu operation - keep only minimal term
			CIExpressionLBImpl<LogicalAnd> lbel = (CIExpressionLBImpl<LogicalAnd>) el;
			int lenEl = el.getLength();
			
			for(Iterator<Expression<LogicalAnd>> it = mElements.iterator(); it.hasNext(); )
			{
				Expression<LogicalAnd> elin = it.next();
				CIExpressionLBImpl<LogicalAnd> lbelin = (CIExpressionLBImpl<LogicalAnd>) elin;
	
				// Check if el is subset of this sentence
				// Shorter clause coming in, remove the longer clause
				if(elin.getLength() >= lenEl 
						&& lbelin.mLiterals.values()
							.containsAll(lbel.mLiterals.values()))
				{
					it.remove();
					continue;
				}
				
				if(lenEl >= elin.getLength()
						&& lbel.mLiterals.values()
							.containsAll(lbelin.mLiterals.values()))
				{
					return false; // shorter clause already in
				}
			}
			
			// Ready to add the new clause
			mElements.add(el);
			return true;
		}

		@Override
		public SimpleSentence<LogicalOr, LogicalAnd> cloneSimpleSentence() {
			List<Expression<LogicalAnd>> clonedElts = new ArrayList<Expression<LogicalAnd>>(mElements.size());
			for(Expression<LogicalAnd> elt : mElements)
				clonedElts.add(elt.cloneExpression());
			return new DNFImpl(mOp, clonedElts, mFac, false);
		}

		@Override
		public boolean isFalse() {
			return mElements.isEmpty(); // this returns false when true which contains the special literal \top
		}

		@Override
		public boolean isTrue() {
			return mElements == mFac.mTrueTerms; // term with special literal \top
		}
		
		@Override
		public Iterable<Expression<LogicalAnd>> getElements() {
			// TODO: Would be nice to cache these. iterator() creates a new iterator in any case.
			return new ExpsIterable<LogicalAnd>(mElements); 
		}		
	}
}

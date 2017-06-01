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


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.FluentIterable;

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOp;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.basics.SymbolicResolver;

class PASImpl implements PAS {
	
	protected PropFactory mFac;
	protected SimpleSentence<LogicalAnd, LogicalOr> mCNF;

	protected Map<String, Proposition> mProps;
	protected Map<String, Assumption> mAsmts;
	protected Map<String, Literal> mSpecials;
	
	protected volatile SymbolicResolver mSR;
	protected volatile NumericResolver mNR;
	protected volatile ProbabilityComputer mPC;
	protected volatile Map<SimpleSentence<LogicalAnd, LogicalOr>, SimpleSentence<LogicalOr, LogicalAnd>> mCachedSupport;
	
	private Pattern mParseTerm;
	private Pattern mValidTerm;
	private Pattern mParseDNF;
	private Pattern mValidDNF;
	
	private Pattern mParseClause;
	private Pattern mValidClause;
	private Pattern mParseCNF;
	private Pattern mValidCNF;
	
	private Pattern mParseHC;
	private Pattern mValidHC;
	
	// Common construction
	{
		mProps = new LinkedHashMap<>(); // use linkedhashmap to ease debugging stuff
		mAsmts = new LinkedHashMap<>(); // use linkedhashmap to ease debugging stuff
		mSpecials = new HashMap<>(2); // Of course we don't need this, but it's the cleanest way of handling it.

		mCachedSupport = new HashMap<>();
	}
	
	public PASImpl(PropFactory fac) throws KBException
	{
		this(null, null, fac);
	}

	public PASImpl(Iterable<Assumption> asmts, Iterable<Proposition> prps, PropFactory fac) throws KBException
	{
		this(null, asmts, prps, fac);
	}
	
	public PASImpl(SimpleSentence<LogicalAnd, LogicalOr> cnf, PropFactory fac) throws KBException
	{
		this(cnf, null, null, fac);
	}
	
	public PASImpl(SimpleSentence<LogicalAnd, LogicalOr> cnf, Iterable<Assumption> asmts, Iterable<Proposition> prps, PropFactory fac) throws KBException
	{
		mFac = fac;
		compileRegexes(fac);
		addSpecialLiterals();
		
		if(cnf == null)
			mCNF = mFac.createCNFSentence();
		else
		{
			mCNF = cnf;
			
			for(Expression<LogicalOr> exp : mCNF.getElements())
				for(Literal lit : exp.getLiterals())
					addLiteral(lit);					
		}

		if(asmts != null)
			for(Assumption a : asmts)
				addAssumption(a);
		if(prps != null)
			for(Proposition p : prps)
				addProposition(p);
	}
	
	protected String getNegation()
	{
		return mFac.getDefaultSymboliser().getNegation();		
	}

	private void addSpecialLiterals()
	{
		mSpecials.put(mFac.getFalse().getName(), mFac.getFalse());
		mSpecials.put(mFac.getTrue().getName(), mFac.getTrue());
		
		// now add the negated versions
		mSpecials.put(getNegation() + mFac.getFalse().getName(), mFac.getTrue());
		mSpecials.put(getNegation() + mFac.getTrue().getName(), mFac.getFalse());
	}
	
	public void addLiteral(Literal lit) throws KBException
	{
		if(lit.isProposition())
			addProposition(lit);
		else if(lit.isAssumption()) // check to avoid storing "specials"
			addAssumption(lit);		
	}
	
	protected void addAssumption(Literal lit) throws KBException {
		if(!lit.isAssumption())
			throw new KBException("Unexpected literal given for assumption.");
		if(mProps.containsKey(lit.getName()))
			throw new KBException("Assumption vs. proposition name collision.");
		if(lit.isAssumption())
		{
			Literal inLit = mAsmts.get(lit.getName());
			if(inLit == null)
			{
				Literal posLit = lit.getNeg() ? lit.cloneNegated() : lit;
				Literal negLit = lit.getNeg() ? lit : lit.cloneNegated();
				mAsmts.put(lit.getName(), (Assumption) posLit);
				mAsmts.put(getNegation() + lit.getName(), (Assumption) negLit);
			}
			else
			{
				// In a KB we can't have to literals with the same name but different index.
				if(!inLit.getIndex().equals(lit.getIndex()))
					throw new KBException("Assumption collision: " + inLit.getName() + " vs. " + lit.getName());
			}
		}
	}

	protected void addProposition(Literal lit) throws KBException {
		if(!lit.isProposition())
			throw new KBException("Unexpected literal given for proposition.");
		if(mAsmts.containsKey(lit.getName()))
			throw new KBException("Proposition vs. assumption name collision.");
		Literal inLit = mProps.get(lit.getName());
		if(inLit == null)
		{
			Literal posLit = lit.getNeg() ? lit.cloneNegated() : lit;
			Literal negLit = lit.getNeg() ? lit : lit.cloneNegated();
			mProps.put(lit.getName(), (Proposition) posLit);
			mProps.put(getNegation() + lit.getName(), (Proposition) negLit);
		}
		else
		{
			// In a KB we can't have two literals with the same name but different index.
			if(inLit.getIndex() != lit.getIndex())
				throw new KBException("Proposition collision :" + inLit);
		}
	}

	protected Literal getLiteralFromDesc(String name)
	{
		Literal inLit;
		inLit = mProps.get(name);
		if(inLit != null)
			return inLit;
		inLit = mAsmts.get(name);
		if(inLit != null)
			return inLit;
		inLit = mSpecials.get(name);
		return inLit;
	}
	
	@Override
	public Iterable<Assumption> getAssumptions() {
		return mAsmts.values();
	}
	@Override
	public Iterable<Assumption> getAssumptions(boolean positive) {
		return FluentIterable.from(mAsmts.values()).filter(a -> positive ^ a.getNeg());
	}

	@Override
	public Iterable<Proposition> getPropositions() {
		return mProps.values();
	}
	@Override
	public Iterable<Proposition> getPropositions(boolean positive) {
		return FluentIterable.from(mProps.values()).filter(p -> positive ^ p.getNeg());
	}

	@Override
	public SimpleSentence<LogicalAnd, LogicalOr> getKB() 
	{
		// This could've returned an "unmodifiable" sentence. But the tentacles of such a wrapper class would have to go very
		// deep and affect every created expression, and every iterable-based operation on them so that I'd rather trust the developer
		// then enforce it and take a hit.
		return mCNF;
	}

	private void compileRegexes(PropFactory fac)
	{
		String valName = getNegation() + "?" + fac.getValidName().pattern();
		mParseTerm = Pattern.compile("[ ]*(" + valName +  ")[ ]*");
		// For some reason the non capturing group "?:" isn't good enough here to stop infinite zero-length backtracking.
		// So use atomic group instead "?>".
		mValidTerm = Pattern.compile("[ ]*(?>" + valName +  "[ ]*)+"); 
		mParseDNF = Pattern.compile("\\[?(" + mValidTerm.pattern() + ")\\+?");
		String nakedDNF = mValidTerm + "(?>\\+" + mValidTerm + ")*" ;
		mValidDNF = Pattern.compile(nakedDNF + "|[ ]*\\[" + nakedDNF + "\\][ ]*");

		mParseClause = Pattern.compile("[ ]*\\(?[ ]*([^\\+ \\)]+)[ ]*\\+?[ ]*\\)?");
		String nakedClause = "[ ]*" + valName + "[ ]*" + "(?:\\+[ ]*" + valName + "[ ]*)*";
		mValidClause = Pattern.compile("[ ]*\\(" + nakedClause + "\\)[ ]*|" + nakedClause);
		mParseCNF = Pattern.compile("\\[?(" + mValidClause.pattern() + ")");
		String nakedCNF = "[ ]*(?>\\(" + nakedClause + "\\)[ ]*)+";
		mValidCNF = Pattern.compile(nakedCNF + "|[ ]*\\[" + nakedCNF + "\\][ ]*");
		
		String nakedHC = "(" + mValidTerm + ")\\->[ ]*(" + valName + ")[ ]*";
		mParseHC = Pattern.compile("[ ]*\\(?" + nakedHC);
		mValidHC = Pattern.compile(nakedHC + "|[ ]*\\(" + nakedHC + "\\)[ ]*");
	}

	// TODO: This is untested, test this.
	// TODO: Also need to stop using clone here but get the cached version.
	// And with that, we shouldn't need to do string ops to access cache.
	@Override
	public boolean addHornClause(Iterable<? extends Literal> body, Literal head) throws KBException
	{
		for(Literal l : body)
			addLiteral(l);
		addLiteral(head);
		
		Expression<LogicalOr> cla = mFac.createClause(true);
		for(Literal l : body)
			cla.addLiteral(l.cloneNegated());
		cla.addLiteral(head);
		
		return mCNF.addElement(cla);
	}

	@Override
	public boolean addHornClause(String hcDesc) throws KBException 
	{
		Expression<LogicalOr> clause = constructHornClause(hcDesc);
		return mCNF.addElement(clause);
	}

	@Override
	public Expression<LogicalOr> constructHornClause(String hcDesc) throws KBException {
		if(!mValidHC.matcher(hcDesc).matches())
			throw new KBException("Invalid horn clause description: " + hcDesc);			
		
		Matcher hcmath = mParseHC.matcher(hcDesc);
		if(!hcmath.find() || hcmath.groupCount() != 2)
			throw new KBException("Can't parse horn clause: " + hcDesc);
		
		@SuppressWarnings("unchecked")
		Expression<LogicalOr> clause = (Expression<LogicalOr>) constructTerm_Internal(hcmath.group(1), true);
		String litDesc = hcmath.group(2);
		Literal litHead = getLiteralFromDesc(litDesc);
		if(litHead == null)
			throw new KBException("Literal head can't be found: " + litDesc);
		clause.addLiteral(litHead);
		return clause;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Expression<LogicalAnd> constructTerm(String termDesc) throws KBException {
		return (Expression<LogicalAnd>) constructTerm_Internal(termDesc, false);
	}

	/** This has a special negated mode to help construct Horn clauses. We read a term,
	 * but construct a clause that's negation of the term we're reading instead of the term we're reading.
	 */
	static private Pattern sEmptyTerm = Pattern.compile("[ ]*");
	public Expression<? extends LogicalOp> constructTerm_Internal(String termDesc, boolean createNegated) throws KBException {
		Expression<? extends LogicalOp> expr;
		if(createNegated)
			expr = mFac.createClause(true);//Create 'ordered' for Horn clauses.
		else
			expr = mFac.createTerm();

		if(sEmptyTerm.matcher(termDesc).matches())
			return expr;
		if(!mValidTerm.matcher(termDesc).matches())
			throw new KBException("Invalid term description.");

		Matcher mLits = mParseTerm.matcher(termDesc);
		while(mLits.find())
		{
			String litDesc = mLits.group(1);
			if(createNegated)
			{
				if(litDesc.startsWith(getNegation()))
					litDesc = litDesc.substring(1);
				else
					litDesc = getNegation() + litDesc;
			}

			Literal l = getLiteralFromDesc(litDesc);
			if(l == null)
				throw new KBException("Unrecognised literal in term: " + litDesc);
			expr.addLiteral(l);
		}
		return expr;
	}

	static private Pattern sEmptyClause = Pattern.compile("[ ]*\\([ ]*\\)[ ]*");
	@Override
	public Expression<LogicalOr> constructClause(String clauseDesc) throws KBException {
		Expression<LogicalOr> clause = mFac.createClause();
		if(sEmptyClause.matcher(clauseDesc).matches())
			return clause;
		if(!mValidClause.matcher(clauseDesc).matches())
			throw new KBException("Invalid clause description.");			
		Matcher mLits = mParseClause.matcher(clauseDesc);
		while(mLits.find())
		{
			String lDesc = mLits.group(1);
			Literal l = getLiteralFromDesc(lDesc);
			if(l == null)
				throw new KBException("Unrecognised literal in term: " + lDesc);
			clause.addLiteral(l);
		}
		return clause;
	}

	static private Pattern sEmptyCNF = Pattern.compile("[ ]*\\[" + sEmptyClause.pattern() + "\\][ ]*|" + sEmptyClause.pattern());
	@Override
	public SimpleSentence<LogicalAnd, LogicalOr> constructCNF(String cnfDesc) throws KBException 
	{
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		if(sEmptyCNF.matcher(cnfDesc).matches())
			return cnf;
		if(!mValidCNF.matcher(cnfDesc).matches())
			throw new KBException("Invalid CNF description: " + cnfDesc);			
		Matcher mClauses = mParseCNF.matcher(cnfDesc);
		while(mClauses.find())
		{
			String clauseDesc = mClauses.group(1);
			Expression<LogicalOr> clause = constructClause(clauseDesc);
			cnf.addElement(clause);
		}
		return cnf;
	}

	static private Pattern sEmptyDNF = Pattern.compile("[ ]*\\[" + sEmptyTerm.pattern() + "\\][ ]*|" + sEmptyTerm.pattern());
	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> constructDNF(String dnfDesc) throws KBException 
	{
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		if(sEmptyDNF.matcher(dnfDesc).matches())
			return dnf;
		if(!mValidDNF.matcher(dnfDesc).matches())
			throw new KBException("Invalid DNF description");
		Matcher mTerms = mParseDNF.matcher(dnfDesc);
		while(mTerms.find())
		{
			String termDesc = mTerms.group(1);
			Expression<LogicalAnd> term = constructTerm(termDesc);
			dnf.addElement(term);
		}
		return dnf;
	}

	// Short-hand function. This should only create a new proposition if needed.
	@Override
	public Proposition createProposition(String name, boolean neg) throws KBException {
		Literal existing = getLiteralFromDesc(name);
		if(existing != null)
		{
			if(existing.isProposition())
				return (Proposition) existing;
			else
				throw new KBException("Cannot add proposition, literal already exists for " + name);
		}
		Proposition prop = mFac.createProposition(name, neg);
		addLiteral(prop); // this may throw
		return prop;
	}

	// Short-hand function
	@Override
	public Assumption createAssumption(String name, boolean neg, double probability) throws KBException {
		Literal existing = getLiteralFromDesc(name);
		if(existing != null)
		{
			if(existing.isAssumption())
			{
				if(probability != ((Assumption) existing).getProbability())
					throw new KBException("Cannot add assumption, probability different to existing assumption: " + name);
				return (Assumption) existing;
			}
			else
				throw new KBException("Cannot add assumption, literal already exists for " + name);
		}
		if(Double.isNaN(probability))
			throw new KBException("NaN probability for assumption not allowed.");

		Assumption asm = mFac.createAssumption(name, neg, probability);
		addLiteral(asm);  // this may throw
		return asm;
	}

	@Override
	public Assumption getAssumption(String name, boolean positive) {
		if(positive)
			return mAsmts.get(name);
		else
			return mAsmts.get(getNegation() + name);
	}

	@Override
	public Proposition getProposition(String name, boolean positive) {
		if(positive)
			return mProps.get(name);
		else
			return mProps.get(getNegation() + name);
	}

	@Override
	public PropFactory getFactory() {
		return mFac;
	}
}

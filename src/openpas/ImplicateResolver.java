// OpenPAS
//
//Copyright (c) 2017 Burak Cetin
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package openpas;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.basics.SymbolicResolver;
import openpas.utils.Notifier;
import openpas.utils.Notifying;

// The algorithms used in here are based on the following article designated as HKL2000:
// R. Haenni, J. Kohlas, and N. Lehmann, 
// “Probabilistic Argumentation Systems" in
// Handbook of Defasbile Reasoning and Uncertainty Management Systems, Volume 5 : Algorithms for Uncertainty and Defeasible Reasoning,
// J. Kohlas and S. Morals, Eds. Kluwer, Dordrect, 2000, pp. 221-287
// (Was) available at: http://diuf.unifr.ch/tcs/publications/ps/hkl2000.pdf
// It's likely not possible to make sense of the methods here without having understood this article (or another source on PAS),
// but there are many references to the article in the code to make it easier to relate to this article.

public class ImplicateResolver implements SymbolicResolver, Notifying {
	SimpleSentence<LogicalAnd, LogicalOr> mKB; // CNF kb
	PropFactory mFac;
	Iterable<Proposition> mProps;
	Iterable<Assumption> mAsmts;

	Notifier mNotifier = Notifier.NULL_NOTIFIER;// to get verbose output

	/***
	 * Create new resolver
	 * 
	 * @param kb : will work on the given instance (ie. not clone it)
	 */
	public ImplicateResolver(SimpleSentence<LogicalAnd, LogicalOr> kb, Iterable<Proposition> propositions,
			Iterable<Assumption> assumptions, PropFactory fac) {
		mKB = kb;
		mFac = fac;

		mProps = propositions;
		mAsmts = assumptions;
	}

	public void setNotifier(Notifier mNotifier) {
		this.mNotifier = mNotifier;
	}

	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> findSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) {
		// Find QS(h) -- quasi-support
		mNotifier.printfln("findSP: Finding QS for h.");
		SimpleSentence<LogicalOr, LogicalAnd> qsH = findQS(hypothesis);

		// Find I_A -- contradictory scenarios
		mNotifier.printfln("findSP: Finding QS_I.");
		SimpleSentence<LogicalOr, LogicalAnd> qsI = findQS(mFac.createClause());

		// Calc C_A -- consistent scenarios
		mNotifier.printfln("findSP: Finding consistent support (complement).");
		SimpleSentence<LogicalOr, LogicalAnd> qsC = calcComplement(mFac, qsI);

		// Calc SP(h) -- consistent quasi-support
		mNotifier.printfln("findSP: Finding consistent support (intersection).");
		SimpleSentence<LogicalOr, LogicalAnd> spH = calcIntersection(mFac, qsH, qsC);

		mNotifier.printfln("findSP: Done.");
		return spH;
	}

	/**
	 * HKL2000 only defines a quasi-support discovery method for a hypothesis that's
	 * a clause but that is enough (HKL2000, p28). Because we can use Theorem 2.1
	 * property (3) which is (HKL2000, p17): QS_A(h_1 \wedge h_2, \xi) = QS_A(h_1,
	 * \xi) \cap QS_A(h_2, \xi) This means when we have a CNF as a hypothesis, we'll
	 * divide it into its component clauses, compute the QS for each of them
	 * individually, and get their intersection to get the resulting QA for the CNF.
	 * 
	 * @param hclause The hypothesis which is a CNF.
	 * @return The quasi-support for the hypothesis.
	 */
	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> findQS(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) {
		mNotifier.printfln("findQS: Finding QS for h.");
		SimpleSentence<LogicalOr, LogicalAnd> qs = mFac.getTrueDNF().cloneSimpleSentence();
		for (Expression<LogicalOr> cla : hypothesis.getElements()) {
			SimpleSentence<LogicalOr, LogicalAnd> inqs = findQS(cla);
			qs = calcIntersection(mFac, inqs, qs);
		}
		mNotifier.printfln("findQS: Finding QS for h - done.");
		return qs;
	}

	/**
	 * QS(h, \xi)= \{ \alpha \in \mathcal{C}_A: \alpha \wedge \xi \models h \} where
	 * \alpha is a term, \xi and h are propositional sentences representing the
	 * knowledge base and the hypothesis respectively. ref: HKL2000 Theorem 2.7 p24,
	 * and then also p28.
	 * 
	 * See the functions computeNegatedPrimeImplicatesSubsetOfD_A and computeSigmaH
	 * for how the above formulation is evaluated in this function.
	 * 
	 * This is needed as a public function since it's not possible to pass an empty
	 * clause otherwise (needed for finding the QS for inconsistent scenarios.)
	 * 
	 * @param hclause The hypothesis which is a clause.
	 * @return The \Sigma_H for the hypothesis.
	 */
	public SimpleSentence<LogicalOr, LogicalAnd> findQS(Expression<LogicalOr> hclause) {
		mNotifier.printfln("findQS: Finding QS for clause.");
		SimpleSentence<LogicalAnd, LogicalOr> sigmaH = computeSigmaH(hclause);
//		System.out.println("sigma_H: " + sigmaH);

		return computeNegatedPrimeImplicatesSubsetOfD_A(sigmaH);
	}

	/**
	 * This function returns the \Sigma_H for a hypothesis that is a clause.
	 * \Sigma_H = \mu ( \Sigma \cup \neg H ) = \mu ( \xi \wedge \neg h ) where: h
	 * \in \mathcal{D}_{A \cup P} \xi = \xi_1 \wedge ... \wedge \xi_r is the
	 * knowledge base in CNF form and: \Sigma=\{\xi,...,\xi_r\} is the clause
	 * representation of \xi. The return value is basically a CNF with each literal
	 * in h negated and added as extra clauses to the knowledgebase. \Sigma_H is an
	 * intermittent construct used to convert the quasi-support computation to an
	 * implicate finding problem. See computeNegatedPrimeImplicatesSubsetOfD_A for
	 * more on this. ref: HKL2000, p28
	 * 
	 * @param hclause The hypothesis which is a clause.
	 * @return Sigma_H for the hypothesis.
	 */
	protected SimpleSentence<LogicalAnd, LogicalOr> computeSigmaH(Expression<LogicalOr> hclause) {
		SimpleSentence<LogicalAnd, LogicalOr> sigmaH = mKB.cloneSimpleSentence();
		for (Literal lit : hclause.getLiterals()) {
			Expression<LogicalOr> cl = mFac.createClause();
			cl.addLiteral(lit.getNegated());
			sigmaH.addElement(cl);
		}
		return sigmaH;
	}

	/**
	 * This function finds the implicates of a given CNF where the implicates which
	 * aren't in D_A are dropped. Then it returns the negated sentence as a DNF.
	 * 
	 * It is passed as the cnf parameter \Sigma_H of a hypothesis where the
	 * hypothesis is a clause: h \in \mathcal{D}_{A \cup P}
	 * 
	 * The motivation for this is because: \alpha \models \neg \Sigma_H and so:
	 * \Sigma_H \models \neg \alpha where \alpha is a term: \alpha \in
	 * \mathcal{C}_A. This means the implicates of \Sigma_H are part of the QS when
	 * negated. This function therefore has an optimised algorithm to find the
	 * implicates of the passed CNF sentence, and then negates the result as
	 * outlined in HKL2000 p28-33. Specifically we use: \mu QA(h, \xi) = \neg
	 * Cons_A(Elimp_P(\Sigma_H)) HKL2000, p32
	 * 
	 * @param cnf A CNF sentence which is the \Sigma_H for the hypothesis h.
	 * @return A DNF sentence which is the quasi-support for the hypothesis h.
	 */
	protected SimpleSentence<LogicalOr, LogicalAnd> computeNegatedPrimeImplicatesSubsetOfD_A(
			SimpleSentence<LogicalAnd, LogicalOr> cnf) {

		// Elim_P
		for (Proposition prop : mProps) {
			cnf = elimX(cnf, prop);
//			System.out.println("*:" + cnf);
		}

		// Cons_A - this step is not necessary for probability computations (HKL2000,
		// p33)
		for (Assumption asm : mAsmts) {
			cnf = consX(cnf, asm);
//			System.out.println("*:" + cnf);
		}

		// We have the prime implicates of cnf subset of D_A by this point.

		// Negate the result before we return it.
		SimpleSentence<LogicalOr, LogicalAnd> qs = mFac.getNegation().negateCNF(cnf);

		return qs;
	}

	/***
	 * Acts on elements of sigma (ie. mutates) and eliminates x. Elim_x(\Sigma) =
	 * Del_x(Cons_x(\Sigma)) = \mu( \Sigma_{\dot{x}} \cup R_x ( \Sigma_x,
	 * \Sigma_{\bar{x}} ) ) where x \in P HKL2000, p31
	 */
	protected SimpleSentence<LogicalAnd, LogicalOr> elimX(SimpleSentence<LogicalAnd, LogicalOr> sigma, Literal lit) {
		if (!lit.isProposition())
			return null; // undefined operation
		List<Expression<LogicalOr>> sigmaXDot = null;
		List<Expression<LogicalOr>> sigmaXNeg = null;
		List<Expression<LogicalOr>> sigmaXPlu = null;

		sigmaXPlu = new ArrayList<Expression<LogicalOr>>(sigma.getLength());
		sigmaXNeg = new ArrayList<Expression<LogicalOr>>(sigma.getLength());
		sigmaXDot = new ArrayList<Expression<LogicalOr>>(sigma.getLength());

		Literal neglit = lit.getNegated();
		for (Expression<LogicalOr> sclause : sigma.getElements()) {
			if (sclause.isContained(lit)) {
				sclause.removeLiteral(lit);
				sigmaXPlu.add(sclause);
			} else if (sclause.isContained(neglit)) {
				sclause.removeLiteral(neglit);
				sigmaXNeg.add(sclause);
			} else {
				sigmaXDot.add(sclause);
			}
		}

		// Add R_xy
		SimpleSentence<LogicalAnd, LogicalOr> elim = mFac.createCNFSentence();
		for (Expression<LogicalOr> exPlu : sigmaXPlu)
			for (Expression<LogicalOr> exNeg : sigmaXNeg) {
				Expression<LogicalOr> rXY = exPlu.cloneExpression().addLiterals(exNeg.getLiterals());
				elim.addElement(rXY);
			}

		// Add \Sigma_{\dot{x}}
		for (Expression<LogicalOr> exDot : sigmaXDot)
			elim.addElement(exDot);

		return elim;
	}

	/***
	 * Acts on elements of sigma (ie. mutates) and returns consequence sentence.
	 * Cons_x(\Sigma)=\mu(\Sigma \cup R_x( \Sigma_x, \Sigma_{x^-})) where x \in P.
	 * This is defined for P, however it's later used also in A. HKL2000, p29
	 * 
	 * @param sigma
	 * @param lit
	 * @return
	 */
	protected SimpleSentence<LogicalAnd, LogicalOr> consX(SimpleSentence<LogicalAnd, LogicalOr> sigma, Literal lit) {
		if (lit.isSpecial())
			return null; // only defined for a proposition or assumption.
		SimpleSentence<LogicalAnd, LogicalOr> sigmaPrime = sigma.cloneSimpleSentence();

		List<Expression<LogicalOr>> sigmaXNeg = null;
		List<Expression<LogicalOr>> sigmaXPlu = null;

		sigmaXPlu = new ArrayList<Expression<LogicalOr>>();
		sigmaXNeg = new ArrayList<Expression<LogicalOr>>();

		Literal neglit = lit.getNegated();
		for (Expression<LogicalOr> sclause : sigmaPrime.getElements()) {
			if (sclause.isContained(lit)) {
				sclause.removeLiteral(lit);
				sigmaXPlu.add(sclause);
			} else if (sclause.isContained(neglit)) {
				sclause.removeLiteral(neglit);
				sigmaXNeg.add(sclause);
			}
		}

		for (Expression<LogicalOr> exPlu : sigmaXPlu)
			for (Expression<LogicalOr> exNeg : sigmaXNeg) {
				Expression<LogicalOr> rXY = exPlu.cloneExpression().addLiterals(exNeg.getLiterals());
				sigma.addElement(rXY);
			}

		return sigma;
	}

	// TODO: This is and'ing two DNFs, should probably go into the And operator.
	// Based on HKL2000, p20.
	// TODO: The intersection here misses "\cap \mathcal{C}_A" with the resulting
	// set, we have only:
	// \mu (\{\alpha_1 \wedge \alpha_2: \alpha_1 \in \mu T(S_1), \alpha_1 \in \mu
	// T(S_2)\})
	// instead of:
	// \mu (\{\alpha_1 \wedge \alpha_2: \alpha_1 \in \mu T(S_1), \alpha_1 \in \mu
	// T(S_2)\} \cap \mathcal{C}_A )
	// In the article this is defined as an operation on sets of scenarios which are
	// based on assumptions only.
	// This automatically happens when both sentences S_1 and S_2 are already in
	// L_A.
	// It is used like this for finding consistent scenarios in this file.
	// I don't see a reason to limit this artificially to C_A other than that since
	// it feels generic enough
	// as an operation (it's and'ing two DNFs), and we satisfy our main use-case w/o
	// problems.
	protected static SimpleSentence<LogicalOr, LogicalAnd> calcIntersection(PropFactory fac,
			SimpleSentence<LogicalOr, LogicalAnd> sen1, SimpleSentence<LogicalOr, LogicalAnd> sen2) {
		// Special cases first
		if (sen1.isTrue())
			return sen2;
		else if (sen2.isTrue())
			return sen1;

		LogicalAnd band = fac.getAnd();
		SimpleSentence<LogicalOr, LogicalAnd> insec = fac.createDNFSentence();

		for (Expression<LogicalAnd> exp1 : sen1.getElements())
			for (Expression<LogicalAnd> exp2 : sen2.getElements())
				insec.addElement(band.and(exp1, exp2));
		return insec;
	}

	// Based on HKL2000, p21.
	protected static SimpleSentence<LogicalOr, LogicalAnd> calcComplement(PropFactory fac,
			SimpleSentence<LogicalOr, LogicalAnd> sen) {
		// Special cases first
		if (sen.isFalse()) // This could have been avoided by starting comp with a true DNF
			return fac.getTrueDNF();
		if (sen.isTrue())
			return fac.getFalseDNF();

		// We get the intersection of the negated terms.
		// N_A - S = \cap_{\alpha \in \mu T(S)}\( N_A(\neg \alpha) \)
		// where \alpha is a term.
		// N_A(\neg \alpha) is found by inverting \alpha, which becomes a clause with
		// single
		// literals, and so a DNF sentence: negtermsen
		SimpleSentence<LogicalOr, LogicalAnd> comp = null;
		for (Expression<LogicalAnd> trm : sen.getElements()) {
			// The following simply complements a term, to get a clause,
			// which in turn we represent as a DNF.
			// TODO: Check - don't we already do this with operator Not?
			SimpleSentence<LogicalOr, LogicalAnd> negtermsen = fac.createDNFSentence();
			for (Literal lit : trm.getLiterals()) {
				Expression<LogicalAnd> intrm = fac.createTerm();
				intrm.addLiteral(lit.getNegated());
				negtermsen.addElement(intrm);
			}

			if (comp == null)
				comp = negtermsen;
			else
				comp = calcIntersection(fac, comp, negtermsen);
		}
		return comp;
	}

	@Override
	public void setNotifier(PrintStream ps) {
		mNotifier = new Notifier(ps);
	}
}

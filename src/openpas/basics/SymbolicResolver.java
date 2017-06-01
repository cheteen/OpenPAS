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
 * This defines operations that relate to finding symbolic solutions to hypotheses in PAS.
 * These symbolic solutions are needed in numerical calculations.
 */
public interface SymbolicResolver {
	
	/**
	 * Finds the quasi-support (QS) for a given CNF hypothesis. The QS alone is normally needed
	 * to calculate the various degrees of support (see {@link NumericResolver#calcDQS(SimpleSentence)} for more).
	 * @param hypothesis
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> findQS(SimpleSentence<LogicalAnd, LogicalOr> hypothesis);
	
	//TODO: Add method to return the QS_I. This can also help with caching this once it's calculated.
	
	/**
	 * Finds the support for a given CNF hypothesis. This is normally interesting only for symbolic
	 * analysis and not required for numerical calculations (see {@link NumericResolver#calcNormalisedDSP(SimpleSentence)} for more).
	 * @param hypothesis
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> findSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis);
	
	/**
	 * Similar to {@link #findQS(SimpleSentence)} but works on a clause instead of a CNF.
	 * @param hclause
	 * @return
	 */
	SimpleSentence<LogicalOr, LogicalAnd> findQS(Expression<LogicalOr> hclause);
}

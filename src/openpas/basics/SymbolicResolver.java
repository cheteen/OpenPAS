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

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

import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;

/**
 * Numeric resolver is a do-all interface for all analytical operations in a PAS system.
 * It achieves this by extending the two other fundamental interface for symbolic resolution
 * and probability computation. This is done mainly for the convenience of the developer
 * otherwise one would have to rely on three interfaces to get access to the analytics.
 * The numeric resolver itself defines the fundamental methods
 * to calculated the degrees of support for the various support definitions.
 */
public interface NumericResolver extends SymbolicResolver, ProbabilityComputer
{
	/**
	 * Calculate the degree of quasi-support (dqs) for the given hypothesis.
	 * @param hypothesis
	 * @return
	 */
	double calcDQS(SimpleSentence<LogicalAnd, LogicalOr> hypothesis);
	
	/**
	 * Calculate the degree of quasi-support for inconsistency. This is typically needed
	 * as an input to the degree of support calculations (e.g. {@link #calcNormalisedDSP(SimpleSentence)})
	 * as follows: <br>
	 * 
	 * $dqs(h, \xi) = \frac{dqs(h,\xi) - dqs(\bot, \xi)}{1 - dqs(\bot, \xi}$ <br>
	 * 
	 * where $\xi$ is the knowledgebase, h is the hypothesis, $\bot$ is falsity,
	 * and so $dqs(\bot, \xi}$ is the dqs for inconsistency (ie. the return value from this method). <br>
	 * 
	 * This implementation may cache the result for the return value here since it's a constant
	 * for a PAS instance, and doesn't change with varying hypotheses.
	 * 
	 * ref: HKL2000, p25
	 * @return
	 */
	double calcDQS_I();
	
	/**
	 * Calculate the unnormalised degree of support, such that: <br>
	 * 
	 * $dqs_u(h, \xi) = dqs(h,\xi) - dqs(\bot, \xi)$ <br>
	 * 
	 * where $\xi$ is the knowledgebase, h is the hypothesis, and $\bot$ is falsity.
	 * 
	 * This operation may involve first calculating the {@link #calcDQS_I()} and then using this value
	 * as above.
	 * 
	 * @param hypothesis
	 * @return
	 */
	double calcNonNormalisedDSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis);
	
	/**
	 * This calculates the degree of support for a given hypothesis. This is defined as follows:
	 * 
	 * $dqs(h, \xi) = \frac{dqs(h,\xi) - dqs(\bot, \xi)}{1 - dqs(\bot, \xi}$ <br>
	 * 
	 * where $\xi$ is the knowledgebase, h is the hypothesis, and $\bot$ is falsity.
	 * 
	 * ref: HKL2000, p25
	 * 
	 * This operation may involve first calculating the {@link #calcDQS_I()} and then using this value
	 * as above.
	 * 
	 * @param hypothesis
	 * @return
	 */
	double calcNormalisedDSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis);
}

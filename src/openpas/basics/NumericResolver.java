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

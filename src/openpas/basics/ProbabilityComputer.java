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
 * This is an interface that is about calculating the probability of a PAS expression.
 * This is currently defined only on a DNF, but any sentence can be converted to a DNF
 * (even though this conversion may be an NP-hard operation) in some cases.
 */
public interface ProbabilityComputer {
	/**
	 * Compute and return the probability of a DNF. This will accept a DNF with only
	 * assumptions in it. If not, the result will be NaN.
	 * @param dnf A valid DNF with only assumptions in it.
	 * @return The probability of the DNF, or NaN if {@code dnf} is not valid.
	 */
	double computeDNFProbability(SimpleSentence<LogicalOr, LogicalAnd> dnf);
}

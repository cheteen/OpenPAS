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
import java.util.List;

import com.google.common.collect.Iterables;

import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;

/**
 * This class implements the ProbabilityComputer interface using a brute-force approach. It follows a method known as
 * Sylvester-Poincare development or the Inclusion-Exclusion principle (see below). This defines how to obtain the probability
 * for a union of sets by creating an exponential number of terms. <br>
 * 
 * In our case, we apply this method to a DNF. Algorithmically, this boils down to picking various (n,m) combinations
 * of the expressions in the DNF sentence and summing their probabilities in a certain way alternating between + and -.
 * See the code comments for more. <br>
 * 
 * (I understand there's an algorithm in one of Donal Knuth's books for this, but this is my own take.) <br>
 * 
 * @see https://en.wikipedia.org/wiki/Inclusion%E2%80%93exclusion_principle#In_probability
 * @see https://en.wikipedia.org/wiki/Boole%27s_inequality#Bonferroni_inequalities
 */
class ProbabilityComputer_SPExpansion implements ProbabilityComputer 
{
	PropFactory mFac;
	
	//TODO: Allow creation of computer that allows approximate calculations using Bonferroni equalities.
	public ProbabilityComputer_SPExpansion(PropFactory fac)
	{
		mFac = fac;
	}
	
	private double pickProbabilityOfCombination(List<Expression<LogicalAnd>> terms, int size, int pick, Expression<LogicalAnd> existingTerm)
	{
		assert(size > 0);
		double result = 0;
		if(pick == 1) // leaf
		{
			for(Expression<LogicalAnd> term : terms)
			{
				if(term == null) // special message to say this element is removed
					continue;
				Expression<LogicalAnd> finalTerm = mFac.getAnd().and(existingTerm, term);
				//System.out.println("Adding term:" + finalTerm); // DEBUG
				result += finalTerm.computeProbability();
			}
		}
		else
		{   // Reduce the problem
			// For each term find all possible combinations including that term by asking the lesser order problem after removing
			// that term from consideration. Once a term is done, move on with what's remaining for the original question but now
			// all relevant possibilities for the processed terms already covered.
			// This _looks_ much nicer with linked list but runs a little slower.
			List<Expression<LogicalAnd>> localTerms = new ArrayList<>(terms);
			int localSize = size;
			for(int i = 0; i < localTerms.size(); ++i)
			{
				Expression<LogicalAnd> removed = localTerms.get(i);
				if(removed == null)
					continue; // already not present
				// Remove the term from the question from now on.
				localTerms.set(i, null); // This avoids moving many items in the list and is still faster than using a linked list.
				--localSize; // This is an optimisation to loop to (i-1)th real element in our list.
				if(localSize == 0)
					break;
				result += pickProbabilityOfCombination(localTerms, localSize, pick - 1, mFac.getAnd().and(existingTerm, removed));
			}
		}
		return result;
	}
	
	@Override
	public double computeDNFProbability(SimpleSentence<LogicalOr, LogicalAnd> dnf) 
	{
		// see Antoine2003A practical comparison of methods to assess sum-of-products, p3
		// Sylvester-Poincare expansion works as follows:
		// p(S) = \sum_{1<=i<=n} p(\pi_i) - \sum_{1<=i<j<=n} p(\pi_i \pi_j) + \sum_{1<=i<j<k<=n} p(\pi_i \pi_j \pi_k) -
		// 			... + (-1)^{m+1} p(\pi_i ... \pi_m)
		// where S is the DNF formula, and \pi_i are each of the terms of S.
		
		// We need random access to each element, let's create an array list.
		List<Expression<LogicalAnd>> elts = new ArrayList<Expression<LogicalAnd>>();
		Iterables.addAll(elts, dnf.getElements());
		
		Expression<LogicalAnd> emptyTerm = mFac.createTerm();
		
		double p_S = 0;
		double dir = 1;
		int n = elts.size();
		for(int m = 1; m < n + 1; ++m) // m for W_m
		{ 	// Compute each W_m
			
			//System.out.println("Get combinations of " + elts.size() + " with " + m); // DEBUG

			// This means we need to pick up a combination C(n,m) of the list of n terms
			double p_m = pickProbabilityOfCombination(elts, n, m, emptyTerm);
			
			p_S += dir * p_m;
			dir *= -1;
		}
		
		return p_S;
	}
}

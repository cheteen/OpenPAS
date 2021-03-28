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

package analytics;

import java.io.PrintStream;
import java.util.List;

import com.google.common.collect.Ordering;

import openpas.SentenceUtils;
import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PropFactory;

public class Scenarios 
{
	// Of course this should be inside the operators - but that's a bit of a mess right now,
	// and I'll have to sort that out at some point alongside what's below.
	static void andWith(SimpleSentence<LogicalOr, LogicalAnd> dnf, Literal lit)
	{
		for(Expression<LogicalAnd> ex : dnf.getElements())
			ex.addLiteral(lit);
	}
	
	public static void printScenarios(SimpleSentence<LogicalOr, LogicalAnd> dnf, PAS pas, NumericResolver nr, PrintStream out)
	{
		// TODO: Better documentation here to explain what's what.
		// TODO: Extend this to support partial assumption assignments (ie. by passing a list of assumptions)
		PropFactory fac = pas.getFactory();
		List<Assumption> la = SentenceUtils.extractAssumptionsFromDNF(dnf);
		if(la == null)
		{
			out.println("No assumptions in given DNF.");
			return;
		}
	
		// Get a sorted view of the assumptions by index - that's what will need to be presented.
		Ordering<Assumption> orderByIndex = Ordering.natural().onResultOf((asm) -> asm.getIndex());
		List<Assumption> laSorted = orderByIndex.immutableSortedCopy(la);
		
		// Find the contradiction
		SimpleSentence<LogicalOr, LogicalAnd> contra = nr.findQS(fac.getFalseCNF());
		
		final int numAsm = laSorted.size();
		
		out.println("Assumptions:");
		for(Assumption a : laSorted)
			out.println(String.format("%03d : %s (%.7f)", a.getIndex(), a.getName(), a.getProbability()));
		out.println();

		// Print the header
		out.print("Sce|");
		for(Assumption a : laSorted)
			out.print(String.format("%03d|", a.getIndex()));
		out.print("QS |");
		out.print(" I |");
		out.print("   p(s)  |");
		out.print("  p(exp) |");
		out.println();

		int numSat = 0;
		int numI = 0;
		int numPartialI = 0;
		double probExpr = 0;
		// Traverse through all possible scenarios.
		for(int scen = 0; scen < (1 << numAsm); ++scen)
		{
			out.print(String.format("%03d|", scen));
			
			SimpleSentence<LogicalOr, LogicalAnd> dnfEval = dnf.cloneSimpleSentence();
			SimpleSentence<LogicalOr, LogicalAnd> contraEval = contra.cloneSimpleSentence();
			Expression<LogicalAnd> termTried = fac.createTerm();
			double probScen = 1;
			int ixAsm = laSorted.size() - 1;
			for(Assumption a : laSorted)
			{
				boolean positive = (scen >> ixAsm) % 2 == 1;
				out.print(positive ? " T |" : " F |");
				--ixAsm; // this makes the first assumption the most significant digit
				Assumption sa = positive ? a : (Assumption) a.getNegated();
				andWith(dnfEval, sa);
				probScen *= sa.getProbability();
				andWith(contraEval, sa);
				termTried.addLiteral(sa);
			}

			boolean satMaybe = false;
			dnfEval.update();
			if(dnfEval.isFalse())
				out.print("   |");
			else if(dnfEval.getLength() == 1 && dnfEval.getElements().iterator().next().equals(termTried))
			{
				out.print(" Y |");
				++numSat;
			}
			else
			{
				out.print(" P |"); // partially
				satMaybe = true;
			}

			contraEval.update();
			// If what we're left with after assignment has only one term and it's the term we're evaluating for,
			// then this is contained in contradiction.
			// We could possibly express this more directly, but this works for now.
			if(contraEval.isFalse())
				out.print("   |");
			else if(contraEval.getLength() == 1 && contraEval.getElements().iterator().next().equals(termTried))
			{
				out.print(" Y |");
				++numI;
			}
			else
			{
				out.print(" P |"); // partially
				++numPartialI;
			}

			out.print(String.format("%.7f|", probScen));

			// Check if this was a full assigmnent for this DNF. If not, calculate what it actually is.
			double probAssn;
			if(satMaybe)
				probAssn = nr.computeDNFProbability(dnfEval);
			else if(dnfEval.isFalse())
				probAssn = 0;
			else
				probAssn = probScen;
			out.print(String.format("%.7f|", probAssn));
			probExpr += probAssn;
			
			out.println();
		}
		out.println();
		out.println("Probability of expression: " + probExpr + " (not normalised)");
		out.println("# satisfying (quasi-supporting): " + numSat);
		out.println("# inconsistent: " + numI);
		out.println("# partially inconsistent: " + numPartialI);
	}
}

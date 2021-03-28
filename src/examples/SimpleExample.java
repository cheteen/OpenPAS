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

package examples;

import java.util.Arrays;

import analytics.Scenarios;
import openpas.OpenPAS;
import openpas.basics.Assumption;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;

/**
 * This is an example that shows the bare-minimum you need to create and examine a PAS instance.
 * It implements the example PAS instance seen at the <a href="http://openpas.steweche.co.uk/">OpenPAS website</a>.
 * @param args
 */
public class SimpleExample {

	public static void main(String[] args) throws KBException {
		PropFactory fac = OpenPAS.getFactory();
		PAS pas = OpenPAS.createPAS();
		
		//Create the assumptions:
		Assumption aPersonSick = pas.createAssumption("Person_sick_on_train", false, 0.05);
		Assumption aHeavyRain = pas.createAssumption("Heavy_rain", false, 0.1);
		Assumption aRainTrainProblem = pas.createAssumption("Rain_causes_train_problem", false, 0.2);
		
		//Create the proposition(s):
		Proposition pTrainDelay = pas.createProposition("train_delay", false);
		
		//Construct the knowledgebase:
		pas.addHornClause(Arrays.asList(aHeavyRain, aRainTrainProblem), pTrainDelay);
		pas.addHornClause(Arrays.asList(aPersonSick), pTrainDelay);

		//Create the hypothesis:
		SimpleSentence<LogicalAnd, LogicalOr> hypothesis = fac.createCNFSentence();
		hypothesis.addElement(fac.createClause(Arrays.asList(pTrainDelay)));
		
		//Perform symbolic and numerical analysis:
		NumericResolver nr = OpenPAS.createNumericResolver(pas, OpenPAS.createImplicateResolver(pas),
				OpenPAS.createProbabilityComputerBDD(1024*1024));
		
		SimpleSentence<LogicalOr, LogicalAnd> qs = nr.findQS(hypothesis);
		
		System.out.println("Knowledgebase: " + pas.getKB());
		System.out.println("hypothesis: " + hypothesis);
		System.out.println("QS(h) = " + qs);
		System.out.println("dqs(h) = " + nr.calcDQS(hypothesis));
		System.out.println("dsp(h) = " + nr.calcNormalisedDSP(hypothesis));
		System.out.println();
		
		// Perform scenario analysis (useful if debugging is needed):
		System.out.println("Scenario analysis:");
		Scenarios.printScenarios(qs, pas, nr, System.out);
	}

}

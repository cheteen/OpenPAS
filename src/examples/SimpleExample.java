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

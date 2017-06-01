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

package openpas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;

public class SentenceUtils {

	public static List<Assumption> extractAssumptionsFromDNF(SimpleSentence<LogicalOr, LogicalAnd> dnf) {
		List<Assumption> asms = new ArrayList<>();
		HashSet<Integer> setAssumptions = new HashSet<Integer>();
		for(Expression<LogicalAnd> cla : dnf.getElements())
			for(Literal lit : cla.getLiterals())
			{
				if(!lit.isAssumption())
					return null;
				Assumption asm = (Assumption) lit;
				if(setAssumptions.add(asm.getIndex()))
					asms.add(asm.getNeg() ? (Assumption) asm.cloneNegated() : asm);
			}
		
		return asms;
	}

}

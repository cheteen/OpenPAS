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
					asms.add(asm.getNeg() ? (Assumption) asm.getNegated() : asm);
			}
		
		return asms;
	}

}

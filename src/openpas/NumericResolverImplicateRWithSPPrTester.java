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

import openpas.basics.PAS;
import openpas.basics.ProbabilityComputer;
import openpas.basics.SymbolicResolver;

public class NumericResolverImplicateRWithSPPrTester extends NumericResolverImpResAbstractTester {

	@Override
	SymbolicResolver createSymResolver(PAS pas) {
		return new ImplicateResolver(pas.getKB(), pas.getPropositions(), pas.getAssumptions(), mFac);
	}

	@Override
	ProbabilityComputer createProbabilityComputer() {
		return new ProbabilityComputer_SPExpansion(mFac);
	}

}

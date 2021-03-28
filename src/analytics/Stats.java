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

import com.google.common.collect.Iterables;

import openpas.basics.PAS;

public class Stats {

	public static void printPASStats(PAS pas, PrintStream out)
	{
		int numAsm = Iterables.size(pas.getAssumptions(true));
		out.println("# assumptions:" + numAsm);
		out.println(String.format("# of scenarios: %.2e", Math.pow(2, numAsm)));
		out.println("# propositions: " + Iterables.size(pas.getPropositions()));
		out.println("# clauses: " + pas.getKB().getLength());
	}
}

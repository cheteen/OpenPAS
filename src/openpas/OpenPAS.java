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

import openpas.basics.Assumption;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.basics.SymbolicResolver;

/**
 * This is a facade class into various OpenPAS interfaces/classes. The interfaces accessed from here are enough to do
 * almost everything OpenPAS can do. Most important ones are: <br>
 * - {@link PAS} via {@link #createPAS()} is a high-level class that encapsulates a PAS instance, does string parsing,
 * and provides access to further interfaces/classes as needed. <br>
 * - {@link PropFactory} via {@link #getFactory()} provides access to some special object and singletons, and objection
 * creation facitilies. Object creation via the factory should not be necessary if the string based PAS interfaces
 * are used, but some of special values (e.g. the true/false sentences such as {@link PropFactory#getTrueCNF()}, or literals
 * such as {@link PropFactory#getFalse()}) and singletons such as {@link PropFactory#getDefaultStringer()} can be
 * useful. <br>
 * - {@link NumericResolver} via {@link #createNumericResolver(PAS, SymbolicResolver, ProbabilityComputer)} should be created
 * once a PAS instance is ready, and provides access to all the analytical interfaces (symbolic or numerical).<br>
 */
public class OpenPAS {

	private static PropFactory sFactory = null;
	
	/**
	 * Returns the singleton factory instance.
	 * @return
	 */
	public static PropFactory getFactory()
	{
		if(sFactory == null)
			sFactory = LBImpls.getFactory();

		return sFactory;
	}
	
	/**
	 * This should be used for testing or in classes inheriting from OpenPAS.
	 * @param fac
	 * @return Returns the previous factory.
	 */
	protected static PropFactory setFactory(PropFactory fac)
	{
		PropFactory prev = sFactory;
		sFactory = fac;
		return prev;
	}
	
	/**
	 * Creates a new empty PAS instance.
	 * @return
	 * @throws KBException
	 */
	public static PAS createPAS() throws KBException
	{
		return new PASImpl(getFactory());
	}
	
	/**
	 * Creates a new PAS instance using as knowledge base the cnf given. This will also extract all the assumptions and
	 * propositions used in the given cnf and add to the PAS instance.
	 * @param cnf
	 * @return
	 * @throws KBException
	 */
	public static PAS createPAS(SimpleSentence<LogicalAnd, LogicalOr> cnf) throws KBException
	{
		return new PASImpl(cnf, getFactory());
	}
	
	/**
	 * Create a new PAS instance with an empty knowledgebase but having the given assumptions and propositions defined.
	 * @param assumptions
	 * @param propositions
	 * @return
	 * @throws KBException
	 */
	public static PAS createPAS(Iterable<Assumption> assumptions, Iterable<Proposition> propositions) throws KBException
	{
		return new PASImpl(assumptions, propositions, getFactory());
	}
	
	/**
	 * Create a symbolic resolver for the given PAS instance.
	 * @param pas
	 * @return
	 */
	public static SymbolicResolver createImplicateResolver(PAS pas)
	{
		return new ImplicateResolver(pas.getKB(), pas.getPropositions(), pas.getAssumptions(), getFactory());
	}
	
	/**
	 * Create a binary decision diagrams (BDD) based probability computer with a limit of numNodes. See
	 * {@link ProbabilityComputer_BDD} for more.
	 * @param numNodes
	 * @return
	 */
	public static ProbabilityComputer createProbabilityComputerBDD(int numNodes)
	{
		return createProbabilityComputerBDD(numNodes, null);
	}
	
	/**
	 * Create a probability BDD based probability computer that will write its BDD to the specified dotFilePath. This dot file
	 * is further processed to contain the actual assumption names used. See {@link #createProbabilityComputerBDD(int)} for more.
	 * @param numNodes
	 * @param dotFilePath
	 * @return
	 */
	public static ProbabilityComputer createProbabilityComputerBDD(int numNodes, String dotFilePath)
	{
		return new ProbabilityComputer_BDD(numNodes, dotFilePath);
	}
	
	/**
	 * Create a probability computer using Syvester-Poincare development (aka Inclusion-Exclusion principle). See
	 * {@link ProbabilityComputer_SPExpansion} for more.
	 * @return
	 */
	public static ProbabilityComputer createProabilityComputerSPX()
	{
		return new ProbabilityComputer_SPExpansion(getFactory());
	}

	/**
	 * Create a numeric resolver for the given PAS instance. You'll need to have created a SymbolicResolver and a
	 * ProbabilityComputer before calling this method. NumericResolver expands these two interfaces, and should pass the calls
	 * along to the two objects. Therefore, a reference to the NumericResolver should suffice for all analytics once created.
	 * @param pas The PAS instance to analyse.
	 * @param sr A symbolic resolver created for the same PAS instance passed as a parameter.
	 * @param pc
	 * @return
	 */
	public static NumericResolver createNumericResolver(PAS pas, SymbolicResolver sr, ProbabilityComputer pc)
	{
		return new NumericResolverImpl(sr, pc, getFactory());
	}
}

package fopas;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.FOFormulaBRRelation.AliasEntry;
import fopas.basics.FOAlias;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOAliasBindingByRecursionImpl extends FOFormulaBRImpl implements FOAlias
{
	// The hashCode and equals of this class is intentionally left unimplemented as each instance should be unique.
	protected final List<FOTerm> mTerms;
	protected final FOAliasByRecursionImpl mBoundFormula;
	protected final String mName;
	
	FOAliasBindingByRecursionImpl(boolean negated, String name, FOAliasByRecursionImpl boundFormula, List<FOTerm> terms) throws FOConstructionException
	{
		super(negated);
		mName = name;
		mTerms = terms;
		mBoundFormula = boundFormula;
		
		if(terms.size() != mBoundFormula.getCardinality())
			throw new FOConstructionException("Attempt to call alias with wrong number of args.");
	}

	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		FORuntime settings = structure.getSettings();

		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, false);
		
		//TODO: Need to start a new assignment round here with the free variables given an assignment.
		//TODO: Should cache the free variables during the bind creation since this is root level at that point.
		
		if(settings.getTraceLevel() >= 1)
		{
			settings.getStats().numL1CheckAsgIntoAlias++;
			if(settings.getTraceLevel() >= 5)
			{
				settings.trace(-5, depth, this, "FOAliasBindingByRecursionImpl", hashCode(), "checkAssignment", "%s", stringiseAssignments(assignment));
				settings.trace( 5, depth, this, "FOAliasBindingByRecursionImpl", hashCode(), "checkAssignment", "checkAssignment into alias: %s", formatAliasCall());							
			}
		}

		boolean satisfied = mBoundFormula.checkAssignment(depth + 1, structure, mappedAssignment);
		
		return mNegated ^ satisfied;
	}

	protected Map<FOVariable, FOElement> mapAssignments(FOStructure structure,
			Map<FOVariable, FOElement> assignment, boolean isPartial)
	{
		Map<FOVariable, FOElement> mappedAssignment = new LinkedHashMap<FOVariable, FOElement>();

		for(int i = 0; i < mTerms.size(); i++)
		{
			FOTerm term = mTerms.get(i);
			term.assignVariables(structure, assignment, isPartial);
			FOElement asg = term.getAssignment();
			if(asg == null)
			{
				assert isPartial; // All variables should be assigned by this point if it's not partial.					
				if(!isPartial)
					throw new FORuntimeException("Expected assignment not found for alias.");
			}
			else
				mappedAssignment.put(mBoundFormula.getListArgs().get(i), asg);
		}
		return mappedAssignment;
	}

	@Override
	public Iterable<FOVariable> getArgs()
	{
		return mBoundFormula.getArgs();
	}

	@Override
	FormulaType getType()
	{
		return FormulaType.ALIAS_BINDING;
	}

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
	{
		// This only looks at the terms in the args since variables of the alias are mapped (ie. not used).
		for(FOTerm term : mTerms)
			((FOTermByRecursionImpl) term).analyseScope(setVarsSeenInScope);
	}

	@Override
	public int getCardinality()
	{
		return mTerms.size();
	}

	@Override
	public String getName()
	{
		return mName;
	}
	
	List<FOTerm> getBoundTerms()
	{
		return mTerms;
	}

	@Override
	public FOFormula negate() throws FOConstructionException
	{
		return new FOAliasBindingByRecursionImpl(!mNegated, mName, mBoundFormula, mTerms);
	}
	
	String formatAliasCall()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(mName);
		sb.append('(');
		for(int i = 0; i < mTerms.size(); i++)
		{
			if(i != 0)
				sb.append(", ");
			
			FOTerm term = mTerms.get(i);
			FOElement asg = term.getAssignment();
			FOVariable arg = mBoundFormula.getListArgs().get(i);
			if(asg == null)
			{
				sb.append(arg.getName());
			}
			else
			{
				sb.append(arg.getName());
				sb.append("|\"");
				sb.append(asg.getElement());
				sb.append('"');
			}
		}
		sb.append(')');
		
		return sb.toString();
	}

	@Override
	public FOSet<FOElement> eliminateTrue(int depth, FOStructure structure, FOSet<FOElement> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment,  Set<FOFormulaBRRelation.AliasEntry> aliasCalls)
	{
		FORuntime settings = structure.getSettings();

		// Note that this does more than just mapping the assignments. It also does a partial evaluation of the parameters of an alias
		// while doing that thereby creating a new assignment.
		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, true);
		if(settings.getTraceLevel() >= 5)
		{
			settings.trace(-5, depth, this, "FOAliasBindingByRecursionImpl", hashCode(), "eliminateTrue", "(partial for %s) %s", var.getName(), stringiseAssignments(assignment));
			settings.trace( 5, depth, this, "FOAliasBindingByRecursionImpl", hashCode(), "eliminateTrue", "eliminateTrue into alias: %s", formatAliasCall());
		}
		
		// We track any calls into an alias with a given list of parameters. This is so that, when the same set of params are used into the alias again
		// we know we're not going to make progress given that the only assignment that goes into the alias are the mapping assignments we capture here.
		// So, we capture the state entirely here with the name of the alias and its given list of (mapped) assignments.
		AliasEntry ae = new AliasEntry(this, mappedAssignment); 
		FOSet<FOElement> returnSet;
		if(!aliasCalls.contains(ae))
		{
			aliasCalls.add(ae);
			returnSet = mBoundFormula.eliminateTrue(depth + 1, structure, universeSubset, var, complement ^ mNegated, mappedAssignment, aliasCalls);
		}
		else
		{
			if(settings.getTraceLevel() >= 5)
			{
				settings.trace(2, depth, this, "FOAliasBindingByRecursionImpl", hashCode(), "eliminateTrue", "Alias call %s (partial for %s) %s repeat found.",
						formatAliasCall(), var.getName(), stringiseAssignments(assignment));
				settings.getStats().numL0ElimTrueRepeatCall++;				
			}
			returnSet = universeSubset;
		}
		aliasCalls.remove(ae);
		return returnSet;
	}
}
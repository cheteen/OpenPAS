package fopas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import fopas.FOFormulaByRecursionImpl.FOFormulaBRForAll;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOStructureImpl implements FOStructure
{
	protected Map<FOConstant, FOElement> mConstMapping;
	protected FOSet<FOElement> mUniverse;
	
	transient FOFormula mFreeVars;
	
	FOStructureImpl(FOSet<FOElement> universe)
	{
		mUniverse = universe;
		mConstMapping = new HashMap<FOConstant, FOElement>();
	}

	@Override
	public boolean models(FOFormula form) throws FORuntimeException, FOConstructionException
	{
		// TODO: Find out all free variables and \forall them here.
		// TODO: Find any variable collision (illegal) - can be during execution / nice to at the start.
		// TODO: Deal with any unassigned constants - can be during execution / nice to at the start.
		// TODO: Relations / functions wrong cardinality - can be during exeuction / nice to at the start.
		
		// Now let's "compile" this formula to see if there are any problems, and what the free variables are.
		
		Set<FOVariable> setVarsInScope = new LinkedHashSet<>(); // use linked hash set here so we get consistent results each time.
		Set<FOVariable> setVarsSeenInScope = new LinkedHashSet<>();
		Set<FOVariable> setFreeVars = new LinkedHashSet<>();
		List<String> listWarnings = new ArrayList<>();
		
		((FOFormulaByRecursionImpl) form).analyseVars(setVarsInScope, setVarsSeenInScope, setFreeVars, listWarnings);
		
		// Anything that's in my scope here is a free variable, since we're really in a kind of hidden scope here
		// for all the free variables.
		assert setVarsInScope.isEmpty();
		setVarsSeenInScope.addAll(setFreeVars); // use own scope variables first
		setFreeVars = setVarsSeenInScope;
		
		List<FOVariable> listVars = new ArrayList<FOVariable>(setFreeVars.size());
		listVars.addAll(setFreeVars);

		// Let's add forall's for all the free variables to the formula now.
		FOFormula lastScopeForm = form;
		for(FOVariable var : Lists.reverse(listVars))
			lastScopeForm = new FOFormulaByRecursionImpl.FOFormulaBRForAll(false, var, lastScopeForm, true);
		// TODO: Need new interface here to iterate the free var assignments.
		
		return lastScopeForm.models(this);
	}

	@Override
	public FOSet<FOElement> getUniverse()
	{
		// this should really be unmodifiable
		return mUniverse;
	}

	@Override
	public FOElement getConstantMapping(FOConstant foconst)
	{
		return mConstMapping.get(foconst);
	}

	@Override
	public FOElement setConstantMapping(FOConstant foconst, FOElement elt)
	{
		return mConstMapping.put(foconst, elt);
	}

}

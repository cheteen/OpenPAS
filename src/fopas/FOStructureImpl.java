package fopas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import fopas.FOFormulaByRecursionImpl.FOFormulaBRForAll;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOCombinedSet;
import fopas.basics.FOVariable;

class FOStructureImpl implements FOStructure
{
	protected Map<FOConstant, FOElement> mConstMapping;
	protected FOCombinedSet mUniverse;
	final protected Set<FORelation<FOElement>> mRelations;
	final protected Set<FOFunction> mFuns;
	
	transient FOFormula mFreeVars;
	
	FOStructureImpl(FOCombinedSet universe, Set<FORelation<FOElement>> relations, Set<FOFunction> funs)
	{
		//TODO: Need to check that function/relation names and infix ops don't clash.
		mUniverse = universe;
		mConstMapping = new HashMap<FOConstant, FOElement>();
		mRelations = relations;
		mFuns = funs;
	}

	@Override
	public boolean models(FOFormula form) throws FOConstructionException
	{
		// TODO: Find any variable collision (illegal) - can be during execution / nice to at the start.
		// TODO: Deal with any unassigned constants - can be during execution / nice to at the start.
		// TODO: Relations / functions wrong cardinality - can be during exeuction / nice to at the start.
		
		return form.models(this);
	}

	@Override
	public Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException
	{	
		return form.getSatisfyingAssignments(this);
	}

	@Override
	public FOCombinedSet getUniverse()
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

	@Override
	public Iterable<Map<FOVariable, FOElement>> getAssignments(FOFormula form) throws FOConstructionException
	{
		return ((FOFormulaByRecursionImpl) form).getAssignments(this);
	}

	@Override
	public Iterable<FORelation<FOElement>> getRelations()
	{
		return mRelations;
	}

	@Override
	public Iterable<FOFunction> getFunctions()
	{
		return mFuns;
	}

	@Override
	public Iterable<FOConstant> getConstants()
	{
		return mConstMapping.keySet();
	}
}

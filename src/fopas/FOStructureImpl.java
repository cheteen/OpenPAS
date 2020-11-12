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

	@Override
	public Iterable<Map<FOVariable, FOElement>> getAssignments(FOFormula form) throws FOConstructionException
	{
		return ((FOFormulaByRecursionImpl) form).getAssignments(this);
	}
}

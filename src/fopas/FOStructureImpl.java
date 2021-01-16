package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import fopas.basics.FOAlias;
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
import fopas.basics.FOUnionSet;
import fopas.basics.FOVariable;

class FOStructureImpl implements FOStructure
{
	protected Map<FOConstant, FOElement> mConstMapping;
	protected FOSet mUniverse;
	final protected Set<FORelation<FOElement>> mRelations;
	final protected Set<FOFunction> mFuns;
	final protected Map<String, FOFormula> mAliasMapping;

	final protected FORuntime mRuntime;
	
	FOStructureImpl(FOSet universe, Set<FORelation<FOElement>> relations, Set<FOFunction> funs)
	{
		//TODO: Need to check that function/relation names and infix ops don't clash.
		mUniverse = universe;
		mConstMapping = new HashMap<>();
		mRelations = relations;
		mFuns = funs;
		mAliasMapping = new HashMap<>();
		mRuntime = new FORuntime(); // this will have to be refactored so that runtime data is split from code so we can do things like multi-threading.
	}

	@Override
	public boolean models(FOFormula form) throws FOConstructionException
	{
		// TODO: Find any variable collision (illegal) - can be during execution / nice to at the start.
		// TODO: Deal with any unassigned constants - can be during execution / nice to at the start.
		// TODO: Relations / functions wrong cardinality - can be during exeuction / nice to at the start.
		
		// TODO: Also print the explicit version of the formula here.
		mRuntime.trace(2, 0, form, "FOStructureImpl", hashCode(), "models", "Start evaluation.");
		mRuntime.getStats().reset();
		
		boolean models = form.models(this);
		mRuntime.trace(2, 0, form, "FOStructureImpl", hashCode(), "models", "models: %s", models);
		return models;
	}

	@Override
	public Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException
	{	
		return form.getSatisfyingAssignments(this);
	}

	@Override
	public FOSet getUniverse()
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
		return ((FOFormulaBRImpl) form).getAssignments(this);
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

	@Override
	public Iterable<String> getAliases()
	{
		return mAliasMapping.keySet();
	}

	@Override
	public FOFormula getAlias(String name)
	{
		return mAliasMapping.get(name);
	}

	@Override
	public void addAlias(FOAlias formAlias) throws FOConstructionException
	{		
		FOFormula existing = mAliasMapping.put(formAlias.getName(), formAlias);

		if(existing != null)
			throw new FOConstructionException("Tring to recreate existing alias: " + formAlias.getName());
	}
	
	static List<FOVariable> createVarArgs(String argsString)
	{
		String[] argStrings = argsString.split(",");
		List<FOVariable> listVars = new ArrayList<>();
		for(int i = 0; i < argStrings.length; ++i)
			listVars.add(new FOVariableImpl(argStrings[i].trim()));
		return listVars;
	}

	@Override
	public FORuntime getSettings()
	{
		return mRuntime;
	}
}

package fopas;

import java.util.HashMap;
import java.util.Map;

import fopas.basics.FOConstant;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

class FOStructureImpl implements FOStructure
{
	protected Map<FOConstant, FOElement> mConstMapping;
	protected FOSet<FOElement> mUniverse;
	
	FOStructureImpl(FOSet<FOElement> universe)
	{
		mUniverse = universe;
		mConstMapping = new HashMap<FOConstant, FOElement>();
	}

	@Override
	public boolean models(FOFormula form) throws FORuntimeException
	{
		// Send this back to the formula, the formula implementation knows whether this structure is good to model it.
		return form.models(this);
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

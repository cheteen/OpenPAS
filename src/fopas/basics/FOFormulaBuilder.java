package fopas.basics;

import java.util.List;

public interface FOFormulaBuilder
{
	FOFormula buildFrom(String strform, FOStructure structure) throws FOConstructionException;
	FOAlias buildAlias(String name, List<FOVariable> args, String strform, FOStructure structure) throws FOConstructionException;
}

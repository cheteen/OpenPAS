package fopas.basics;

import java.util.List;

public interface FOFormulaBuilder
{
	FOFormula buildFormula(String strform, FOStructure structure) throws FOConstructionException;
	FOAlias buildAlias(String name, List<FOVariable> args, String strform, FOStructure structure) throws FOConstructionException;
}

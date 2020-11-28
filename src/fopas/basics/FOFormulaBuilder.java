package fopas.basics;

import java.util.List;

public interface FOFormulaBuilder
{
	FOFormula buildFrom(String strform, FOStructure structure) throws FOConstructionException;
	FOFormula buildAlias(String name, String strform, FOStructure structure, List<FOVariable> args) throws FOConstructionException;
}

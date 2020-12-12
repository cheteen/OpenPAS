package fopas.basics;

import java.util.List;

public interface FOFormulaBuilder
{
	FOFormula buildFormula(String strform, FOStructure structure) throws FOConstructionException;
	FOAlias buildAlias(FOStructure structure, String name, List<FOVariable> args, String strform) throws FOConstructionException;
	
	FOFormula buildContradiction();
	FOFormula buildTautology();
}

package fopas.basics;

public interface FOFormulaBuilder
{
	FOFormula buildFrom(String strform, FOStructure structure) throws FOConstructionException;
}

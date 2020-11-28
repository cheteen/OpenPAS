package fopas.basics;

public interface FOAlias extends FOFormula
{
	Iterable<FOVariable> getArgs();
	int getCardinality();
}

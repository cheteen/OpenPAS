package fopas.basics;

public interface FOStringiser
{
	String stringiseFormula(FOFormula form);
	String stringiseFormula(FOFormula form, int maxLen);
}

package fopas;

public class FOLanguage
{
	String getOr() { return "|"; }
	int getPrecedenceOr() { return 1100; }
	
	String getAnd() { return "&"; }
	int getPrecedenceAnd() { return 1200; }
	
	String getImp() { return "->"; }
	int getPrecedenceImp() { return 1000; }
}

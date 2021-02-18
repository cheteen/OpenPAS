package fopas.basics;

import java.util.Comparator;

public interface FOElement {	
	enum Type {
		Symbol,
		Integer,
		String
	}
	
	Type getType();
	Object getElement();
	Comparator<FOElement> getDefaultComparator(); // This is a major assumption that we can do this for anything that we can put in a set - but let's see how far we get with it.
	
	public static interface FOString extends FOElement
	{
		String getString();
	}
	public static interface FOInteger extends FOElement, Comparable<FOInteger>
	{
		int getInteger();
	}
	public static interface FOSymbol extends FOElement
	{
		String getName();
	}
}

package fopas.basics;

public interface FOElement {	
	enum Type {
		Symbol,
		Integer,
		String
	}
	
	Type getType();
	Object getElement();
	
	public static interface FOString extends FOElement {}
	public static interface FOInteger extends FOElement, Comparable<FOInteger>
	{
		int getInteger();
	}
	public static interface FOSymbol extends FOElement {}
}

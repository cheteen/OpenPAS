package fopas.basics;

public interface FOElement {	
	enum Type {
		Symbol,
		Integer,
		String
	}
	
	Type getType();
	
	public static interface FOString extends FOElement {}
	public static interface FOInteger extends FOElement
	{
		int getInteger();
	}
	public static interface FOSymbol extends FOElement {}
}

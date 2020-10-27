package fopas.basics;

public interface FOElement {	
	enum Type {
		Symbol,
		Int,
		String
	}
	
	Type getType();
}

package fopas.basics;

import java.util.List;

public interface FOFunction
{
	FOElement eval(FOStructure structure, FOElement ... args);
	String getName();

	/**
	 * Get cardinality of the relation.
	 * @return -1 for any cardinality, >0 otherwise.
	 */
	int getCardinality();
	
	/**
	 * Optional in-fix representation - can be left null.
	 * @return
	 */
	String getInfix();
}

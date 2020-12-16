package fopas.basics;

public interface FORelation <T extends FOElement>
{
	String getName();
	boolean satisfies(FOElement ... args);
	String getInfix();
	
	/**
	 * Get cardinality of the relation.
	 * @return -1 for any cardinality, >0 otherwise.
	 */
	int getCardinality();
	
	int getPrecedence() throws FOConstructionException;
}

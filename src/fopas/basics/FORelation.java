package fopas.basics;

public interface FORelation <T extends FOElement>
{
	String getName();
	boolean satisfies(FOElement ... args) throws FORuntimeException;
	
	/**
	 * Get cardinality of the relation.
	 * @return -1 for any cardinality, >0 otherwise.
	 */
	int getCardinality();
}

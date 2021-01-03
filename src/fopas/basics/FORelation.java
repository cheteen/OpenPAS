package fopas.basics;

import java.util.List;
import java.util.Map;

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
	
	/**
	 * This will return either itself or a new FOSet thats a subset of it. It'll consider the variable and the (partially assigned)
	 * terms it's given and find out what subset of the universe is needed.
	 * @param var
	 * @param universeSubset
	 * @param terms
	 * @param isComplemented Whether the returned set if a relative complement of the universeSubset (useful when relation is used negated).
	 * @return
	 */
	FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset,  List<FOTerm> terms, boolean isComplemented);
}

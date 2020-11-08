package fopas.basics;

public interface FORelation <T extends FOElement>
{
	String getName();
	boolean satisfies(FOElement ... args) throws FORuntimeException;
	int getCardinality();
}

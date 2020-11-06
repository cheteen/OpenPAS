package fopas.basics;

public interface FORelation <T extends FOElement>
{
	boolean satisfies(FOElement ... args);
}

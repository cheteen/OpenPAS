package fopas.basics;

// This should probably really be a generic "UnionSet" as there's nothing universe
// specific here really.
public interface FOUnionSet<T extends FOElement> extends FOSet<T>
{
	FOSet<T> getOriginalSubset(String name);
}

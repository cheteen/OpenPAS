package fopas.basics;

public interface FOSubset<T extends FOElement> extends FOSet<T>
{
	/**
	 * Modify or sacrifice this set to create a subset that eliminates elements outside this relation. Can return self or a new set possibly using this set's data.
	 */
}

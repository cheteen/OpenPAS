package fopas.basics;

import fopas.basics.FOElement.FOInteger;

public interface FOFactory
{
	<T extends FOElement> FOSet<T> createSet();
	<T extends FOElement> FOSet<T> createSet(Iterable<T> elements);
	<T extends FOInteger> FOSet<T> createNaturals();
}

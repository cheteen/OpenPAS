package fopas.basics;

public interface FORange<T extends FOElement> 
{

	public T getStart();
	public T getStartOrInfinite(boolean includeStart);
	public boolean getIncludeStart();

	/**
	 * This may return null if this is an infinite set.
	 * @return the last element in this set according to the enumeration order.
	 */
	public T getEnd();
	public T getEndOrInfinite(boolean includeEnd);
	public boolean getIncludeEnd();	
}

package fopas.basics;

// A range can be an integer or real-number range. And as such, it can be finite, infinite, enumerable or not.
// So we keep this interface as a separate entity because of that.
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

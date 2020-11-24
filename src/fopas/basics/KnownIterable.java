package fopas.basics;

public interface KnownIterable<T> extends Iterable<T>
{
	/**
	 * @return -1 if infinite or unknown
	 */
	int size();
}

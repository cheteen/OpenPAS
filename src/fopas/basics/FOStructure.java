package fopas.basics;

public interface FOStructure
{
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 */
	boolean models(FOFormula form);
}

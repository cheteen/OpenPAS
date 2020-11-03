package fopas.basics;

public interface FOStructure
{
	FOSet<FOElement> getUniverse();
	FOElement getConstantMapping(FOConstant foconst);
	
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 */
	boolean models(FOFormula form);
}

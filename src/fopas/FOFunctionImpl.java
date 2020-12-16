package fopas;

import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FOStructure;

abstract class FOFunctionImpl implements FOFunction
{
	/**
	 * Get the version of the function that inverses the infix presentation. 
	 * @return
	 */
	abstract FOFunction inversePresentInfix();
	
	abstract boolean presentInfix();
}

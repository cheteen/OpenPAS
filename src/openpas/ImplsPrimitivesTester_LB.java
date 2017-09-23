package openpas;

import openpas.basics.PropFactory;

public class ImplsPrimitivesTester_LB extends ImplsPrimitivesTester_Abstract {
	PropFactory createFactory()
	{
		return new LBImpls.LBImplFactory();
	}
}

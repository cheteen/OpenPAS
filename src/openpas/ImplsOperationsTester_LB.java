package openpas;

import openpas.basics.PropFactory;

public class ImplsOperationsTester_LB extends ImplsOperationsTester_Abstract {
	PropFactory createFactory()
	{
		return new LBImpls.LBImplFactory();
	}
}

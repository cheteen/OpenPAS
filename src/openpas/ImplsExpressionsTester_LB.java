package openpas;

import openpas.basics.PropFactory;

public class ImplsExpressionsTester_LB extends ImplsExpressionsTester_Abstract {
	
	PropFactory createFactory()
	{
		return new LBImpls.LBImplFactory();
	}
}

package openpas;

import openpas.basics.PropFactory;

public class ImplsSentencesTester_LB extends ImplsSentencesTester_Abstract {
	PropFactory createFactory()
	{
		return new LBImpls.LBImplFactory();
	}
}

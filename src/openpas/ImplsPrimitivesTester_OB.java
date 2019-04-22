package openpas;

import openpas.basics.PropFactory;

public class ImplsPrimitivesTester_OB extends ImplsPrimitivesTester_Abstract {

	@Override
	PropFactory createFactory() {
		return new OBImpls.OBFactory();
	}
}

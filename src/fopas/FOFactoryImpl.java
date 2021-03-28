//    Copyright (c) 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

package fopas;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFactory;
import fopas.basics.FOSet;

class FOFactoryImpl implements FOFactory
{
	@Override
	public <T extends FOElement> FOSet<T> createSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends FOElement> FOSet<T> createSet(Iterable<T> elements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends FOInteger> FOSet<T> createNaturals() {
		// TODO Auto-generated method stub
		return null;
	}

}

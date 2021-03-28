//    Copyright (c) 2017, 2021 Burak Cetin
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

package openpas.utils;

public class MutableDouble extends Number {

	private static final long serialVersionUID = 1L;
	double mVal;
	
	public MutableDouble(Number val)
	{
		mVal = val.doubleValue();
	}
	public MutableDouble(double val)
	{
		mVal = val;
	}
	
	@Override
	public double doubleValue() {
		return mVal;
	}

	@Override
	public float floatValue() {
		return (float) mVal;
	}

	@Override
	public int intValue() {
		return (int) mVal;
	}

	@Override
	public long longValue() {
		return (long) mVal;
	}
	
	public void setDoubleValue(double val)
	{
		mVal = val;
	}

}

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

// Do not use this class in a container - it doesn't properly define
// hashing/equality functions.
public class PairT<T1, T2> {

	public T1 first;
	public T2 second;
	
	public PairT(T1 first, T2 second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return first.toString() + "-" + second.toString();
	}
	
}
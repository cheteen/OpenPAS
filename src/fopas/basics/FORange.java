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

package fopas.basics;

// A range can be an integer or real-number range. And as such, it can be finite, infinite, enumerable or not.
// So we keep this interface as a separate entity because of that.
public interface FORange<T extends FOElement> 
{
	public T getStart();
	public T getStartOrInfinite(boolean includeStart);
	public boolean getIncludeStart();

	/**
	 * This may return null if this is an infinite set.
	 * @return the last element in this set according to the enumeration order.
	 */
	public T getEnd();
	public T getEndOrInfinite(boolean includeEnd);
	public boolean getIncludeEnd();	
}

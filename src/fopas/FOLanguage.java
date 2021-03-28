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

public class FOLanguage
{
	String getOr() { return "|"; }
	int getPrecedenceOr() { return 1100; }
	
	String getAnd() { return "&"; }
	int getPrecedenceAnd() { return 1200; }
	
	String getImp() { return "->"; }
	int getPrecedenceImp() { return 1000; }
	
	String getForAll() { return "forall"; }
	String getExists() { return "exists"; }
}

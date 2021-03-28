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

import java.util.regex.Pattern;

public class ValidationRules
{
	static final public String validName =  "[a-zA-Z]+[a-zA-Z_0-9]*"; 
	static final public String validInfixOp =  "[=+-/*^><~@#\\.]+"; 
	static final public String validLogicalOp =  "[|¬&]"; 
}

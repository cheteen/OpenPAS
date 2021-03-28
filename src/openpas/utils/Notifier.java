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

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Notifier {
	
	public static final Notifier NULL_NOTIFIER = new Notifier(null);

	PrintStream mPS;
	public Notifier(PrintStream ps)
	{
		mPS = ps;
	}
	
	public void printfln(String message)
	{
		printfln(message, null);
	}
	
	public void printfln(String fmt, List<String> args)
	{
		if(mPS == null)
			return;
		mPS.print("[");
		mPS.print(new Timestamp(new Date().getTime()));
		mPS.print("] ");
		mPS.printf(fmt, args);
		mPS.println();
	}	
}

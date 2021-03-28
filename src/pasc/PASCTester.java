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

package pasc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import openpas.basics.PAS.KBException;
import pasc.PASC.CommandException;


public class PASCTester {
	
	static class TestablePASC extends PASC
	{
		static public PrintStream getCmdOut() {
			return PASC.getCmdOut();
		}
		static public void setCmdOut(PrintStream cmd_out) {
			PASC.setCmdOut(cmd_out);
		}
		static public PrintStream getNotifier() {
			return PASC.getNotifier();
		}
		static public void setNotifier(PrintStream notifier) {
			PASC.setNotifier(notifier);
		}
		static public void initialiseCommon() {
			PASC.initialiseCommon();
		}
	}
	
	static ByteArrayOutputStream sBAOSOut;
	static ByteArrayOutputStream sBAOSNotify;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		sBAOSOut = new ByteArrayOutputStream();
		sBAOSNotify = new ByteArrayOutputStream();
		TestablePASC.setCmdOut(new PrintStream(sBAOSOut));
		TestablePASC.setNotifier(new PrintStream(sBAOSNotify));
		TestablePASC.initialiseCommon();
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}
	
	@Before
	public void setUp() throws Exception {
		sBAOSOut.reset();
		sBAOSNotify.reset();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	String getPASCOutput()
	{
		try {
			return sBAOSOut.toString(StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Assert.fail("Unable to convert string");
			return null; // never executed
		}
	}
	String getPASCNotify()
	{
		try {
			return sBAOSNotify.toString(StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Assert.fail("Unable to convert string");
			return null; // never executed
		}
	}
	
	/**
	 * Run a list of commands and expect them to execute with success.
	 * @param commands
	 * @throws KBException 
	 * @throws CommandException 
	 */
	public void runCommands(Iterable<String> commands) throws CommandException, KBException
	{
		for(String command : commands)
			Assert.assertTrue(PASC.executeLine(command));
	}
	
	//TODO: Extend these tests - this is more or less a dummy test to validate setup.
	@Test
	public void testCreateAssumption() throws CommandException, KBException {
		boolean bCont = PASC.executeLine("init");
		Assert.assertTrue(bCont);
	}

	@Test
	public void testParamSeparator() throws CommandException, KBException
	{
		runCommands(Arrays.asList(
				"init", 
				"sep: ;", 
				"cp: proposition{with,comma}"
				));
		// Check that a proposition with a comma in it is processed correctly.
		Assert.assertNotNull(PASC.pas.getProposition("proposition{with,comma}", true));
	}
}

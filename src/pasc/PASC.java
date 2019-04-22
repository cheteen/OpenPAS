// OpenPAS
//
// Copyright (c) 2017 Burak Cetin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package pasc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

import analytics.Scenarios;
import analytics.Stats;
import openpas.OpenPAS;
import openpas.StringOps.LogicalStringer;
import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.ProbabilityComputer;
import openpas.basics.Proposition;
import openpas.utils.Notifying;

/**
 * OpenPAS console application.
 * Can be used as an interactive console with OpenPAS, or be used to execute an OPS script which is a newline separated sequence of commands.
 * When run w/o parameters PASC drops to interactive console mode, when a parameter is given it runs it and exits.
 */
public class PASC {

	@SuppressWarnings("serial")
	static class CommandException extends Exception
	{ public CommandException(String s) { super(s); } }
	
	static BufferedReader reader;
	static PAS pas;
	static NumericResolver numResolver;
	static String sepParameters = ",";
	static int numBddNodes = 1024 * 1024;
	static String bddDotFile = "dotfile.dot";
	static int numMaxAssumptionsForDot = 20; // TODO: Make this configurable.
	static int numMinAssumptionsForNotifications = 18; // TODO: Make this configurable
	enum ProbabilityComputerType
	{
		BDD,
		SPExpansion
	}
	static ProbabilityComputerType usePC = ProbabilityComputerType.BDD;
	
	static PrintStream cmd_out;
	static PrintStream notifier;
	// To be used for testing
	protected static PrintStream getCmdOut() {
		return cmd_out;
	}
	protected static void setCmdOut(PrintStream cmd_out) {
		PASC.cmd_out = cmd_out;
	}
	protected static PrintStream getNotifier() {
		return notifier;
	}
	protected static void setNotifier(PrintStream notifier) {
		PASC.notifier = notifier;
	}
	
	protected static String[] splitParams(String param) {
		String[] params = param.trim().split(String.format("[ ]*%s[ ]*", sepParameters));
		return params;
	}

	static interface CLICommand
	{
		/**
		 * Executes a command. If the return value from the command is not True, execution is halted.
		 * @param param
		 * @return True to continue execution, False to exit (with success).
		 * @throws CommandException
		 * @throws KBException
		 * @throws IOException
		 */
		boolean execute(String param) throws CommandException, KBException, IOException;
		String help();
	}
	// The LinkedHashMap here allows us the preserve the "inherent" ordering of the commands, ie. the main
	// name for a command comes first, followed by aliases. We later sort the commands
	// according to their main name when we display the help.
	static Map<String, CLICommand> commandsCLI;
	
	protected static void defineCommands() {
		commandsCLI = new LinkedHashMap<String, PASC.CLICommand>();
				
		CLICommand clear = new CLICommand() {
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				clearNumResolver();
				if(pas != null)
					notifyln("Clearing existing PAS instance.");
				pas = OpenPAS.createPAS();
				return true;
			}
			@Override
			public String help() {
				return "Clear the numeric resolver and the underlying PAS instance.";
			}
		};
		commandsCLI.put("clear", clear);
		
		CLICommand init = new CLICommand() {
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				if(param != null)
				{
					String[] params = splitParams(param);
					switch(params[0])
					{
						case "bdd":
							usePC = ProbabilityComputerType.BDD;
							if(params.length > 1)
								numBddNodes = Integer.parseInt(params[1]);
							if(params.length > 2)
								bddDotFile = params[2];
							else
								bddDotFile = null;
							break;
						case "sp":
							usePC = ProbabilityComputerType.SPExpansion;
							break;
						default:
							throw new CommandException("Unknown probability computer specified.");
					}
				}
				return clear.execute(param);
			}
			
			@Override
			public String help() {
				return 	"Initialise the PAS system by specfying the desired parameters for the system.\n" +
						"init is a pre-requisite for all the PAS related commands.\n" +
						"Parameters: [num_resolver_type],[resolver specific params]\n" +
						"	num_resolver_type: 'bdd' or 'sp'\n" +
						"=== Resolver specific params: ===\n" +
						"== BDD resolver ==\n" +
						"Uses a Binary Decision Diagrams based resolver.\n" +
						"Parameters: bdd [num bdd nodes],[dot_file_path]\n" +
						"	num_bdd_nodes: Max. number of BDD nodes allowed to be used by the BDD numeric resolver. The actual number used may exceed this.\n" +
						"	dot_file_path: This path will be used to create a dot file which represents the BDD used.\n" +
						"		default: dotfile.dot\n" +
						"		current value: " + bddDotFile + "\n" +
						"		A file will not be created until a degree computation is done by a BDD resolver. This file is deleted during start up.\n" +
						"\n" + 
						"== Sylvester Pointcare expansion ==\n" + 
						"Creates an exponential number of terms using term expansion, and computes the probability using these.\n" + 
						"Parameters: sp (no parameters)";
			}
		};
		commandsCLI.put("init", init);
		
		CLICommand createProposition = new CLICommand() {						
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("create_proposition needs name parameter(s)");
				clearNumResolver();
				String[] names = splitParams(param);
				for(String name : names)
					pas.createProposition(name, false);
				return true;
			}
			@Override
			public String help() {
				return 	"Creates a new proposition.\n" +
						"Parameters: <proposition_name>\n" +
						"	proposition_name: A name unique in the PAS instance.";
			}
		};
		commandsCLI.put("create_proposition", createProposition);
		commandsCLI.put("cp", createProposition);
		
		CLICommand createAssumption = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("create_assumption needs name and probability parameter");
				clearNumResolver();
				String[] params = splitParams(param);
				if(params.length != 2)
					throw new CommandException("create_assumption needs name and probability parameter seperate by comma");
				double prob = Double.parseDouble(params[1]);
				pas.createAssumption(params[0], false, prob);
				return true;
			}
			@Override
			public String help() {
				return 	"Creates a new assumption.\n" +
						"Parameters: <assumption_name>,<probability>\n" +
						"	assumption_name: A name unique in the PAS instance.\n" +
						"	probability: A double number for the probability of this assumption";
			}
		};
		commandsCLI.put("create_assumption", createAssumption);
		commandsCLI.put("ca", createAssumption);
		
		CLICommand listLiterals = new CLICommand() {					
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				boolean showAssumptions = param == null || param.equals("a");
				boolean showProps = param == null || param.equals("p");
				boolean showKB = param == null || param.equals("k");
				if(!showAssumptions && !showProps && !showKB)
					throw new CommandException("Unrecognised parameters.");
				if(showProps)
				{
					outln("Propositions:");
					Iterator<Proposition> it = pas.getPropositions(true).iterator();
					while(it.hasNext())
					{
						Proposition p = it.next();
						cmd_out.print(p.getName());
						if(it.hasNext())
							cmd_out.print(",");
					}
					outln("");
				}
				if(showAssumptions)
				{
					if(showProps)
						outln("");
					outln("Assumptions:");
					for(Assumption a : pas.getAssumptions(true))
						outln("%3d) %s : %f", a.getIndex(), a.getName(), a.getProbability());
				}
				if(showKB)
				{
					if(showAssumptions || showProps)
						outln("");
					outln("KB:");
					// This command lists the KB as a list of clauses.
					LogicalStringer hs = OpenPAS.getFactory().getHornStringer();
					for(Expression<LogicalOr> exp : pas.getKB().getElements())
						outln(hs.stringise(exp));
				}
				return true;
			}
			@Override
			public String help() {
				return 	"Returns the lists of assumptions, propositions, and the clauses in the PAS knowledgebase.\n" +
						"Parameters: [mode]\n" +
						"	mode: 'a','p', or 'k'\n" +
						"		p: list propositions only\n" +
						"		a: list assumptions only" +
						"		k: list the knowledgebase as clauses only";
			}
		};
		commandsCLI.put("list", listLiterals);
		commandsCLI.put("l", listLiterals);
		
		CLICommand addHorn = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("add_horn needs horn clause parameter (e.g.: add_horn a b -> c)");
				clearNumResolver();
				pas.addHornClause(param);
				return true;
			}
			@Override
			public String help() {
				return 	"Adds an horn clause to the knowledgebase.\n" +
						"Parameters: <horn_clause>\n" + 
						"	horn_clause: A propositional clause of the form, e.g.:'a b c -> x'";
			}
		};
		commandsCLI.put("add_horn", addHorn);
		commandsCLI.put("ah", addHorn);
		
		CLICommand showKB = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				notifyln("KB:");
				outln(pas.getKB().toString());
				//TODO: Set an option to change the stringerant max size limitation.
				return true;
			}
			@Override
			public String help() {
				return 	"Prints a representation of the knowledgebase as a CNF.\n" +
						"This has a size limitation of " + OpenPAS.getFactory().getDefaultStringer().getMaxSize() +  " characters\n" +
						"and will cut the result where this is exceeded.";
			}
		};
		commandsCLI.put("show_kb", showKB);
		commandsCLI.put("sk", showKB);		
		
		CLICommand calcDQS = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("calc_dqs needs CNF hypothesis");
				ensureNumResolver();
				long timeStart = System.nanoTime();
				double degree = numResolver.calcDQS(obtainCommandCNF(param));
				long timeEnd = System.nanoTime();
				outln("%s", degree);
				notifyln("(duration = %f miliseconds)", 1e-6 * (timeEnd - timeStart));
				return true;
			}
			@Override
			public String help() {
				return "Calculate the degree of quasi-support (dqs) for the given hypothesis.\n" +
						"Parameters: <hypothesis>\n" +
						"	hypothesis: A propositional sentence in the CNF form, e.g.: '(a b)+(c d)+(d e f)'";
			}
		};
		commandsCLI.put("calc_dqs", calcDQS);
		commandsCLI.put("dqs", calcDQS);
		
		CLICommand calcDSP = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("calc_dsp needs CNF hypothesis");
				ensureNumResolver();
				long timeStart = System.nanoTime();
				double degree = numResolver.calcNormalisedDSP(obtainCommandCNF(param));
				long timeEnd = System.nanoTime();
				notifyln("(duration = %f miliseconds)", 1e-6 * (timeEnd - timeStart));
				outln("%s", degree);
				return true;
			}
			@Override
			public String help() {
				return 	"Calculate the degree of support (dsp) for the given hypothesis.\n" + 
						"This involves two dqs calculations: The first one will be for the inconsistency,\n" +
						"and the second one for the actual hypothesis. If the dqs for inconsistency\n" +
						"is already calculated for the given PAS instance, it will be recalled and re-used.\n" +
						"The resulting figure will be normalised for consistency. See also calc_unnormalised_dsp.\n" +
						"Parameters: <hypothesis>\n" +
						"	hypothesis: A propositional sentence in the CNF form, e.g.: '(a b)+(c d)+(d e f)'";
			}
		};
		commandsCLI.put("calc_dsp", calcDSP);
		commandsCLI.put("dsp", calcDSP);
		
		CLICommand calcUDSP = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("calc_unnormalised_dsp needs CNF hypothesis");
				ensureNumResolver();
				long timeStart = System.nanoTime();
				double degree = numResolver.calcNonNormalisedDSP(obtainCommandCNF(param));
				long timeEnd = System.nanoTime();
				notifyln("(duration = %f miliseconds)", 1e-6 * (timeEnd - timeStart));
				outln("%s", degree);
				return true;
			}
			@Override
			public String help() {
				return 	"Calculate the non-normalised degree of support (dsp) for the given hypothesis.\n" +
						"This involves two dqs calculations. The first one will be for the inconsistency,\n" +
						"and the second one for the actual hypothesis. If the dqs for inconsistency\n" +
						"is already calculated for the given PAS instance, it will be recalled and re-used.\n" +
						"The resulting figure will not be normalised for consistency. See also calc_dsp.\n" +
						"Parameters: <hypothesis>\n" +
						"	hypothesis: A propositional sentence in the CNF form, e.g.: '(a b)+(c d)+(d e f)'";
			}
		};
		commandsCLI.put("calc_unnormalised_dsp", calcUDSP);
		commandsCLI.put("udsp", calcUDSP);

		CLICommand findQS = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("find_qs needs CNF hypothesis");
				ensureNumResolver();
				SimpleSentence<LogicalOr, LogicalAnd> support = numResolver.findQS(obtainCommandCNF(param));
				outln("%s", support);
				return true;
			}
			@Override
			public String help() {
				return 	"Finds the symbolic quasi-support (QS) for the given hypothesis.\n" + 
						"This doesn't trigger any numeric calculations.\n" +
						"Parameters: <hypothesis>\n" +
						"	hypothesis: A propositional sentence in the CNF form, e.g.: '(a b)+(c d)+(d e f)'";
			}
		};
		commandsCLI.put("find_qs", findQS);
		commandsCLI.put("qs", findQS);

		CLICommand findSP = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("find_sp needs CNF hypothesis");
				ensureNumResolver();
				SimpleSentence<LogicalOr, LogicalAnd> support = numResolver.findSP(obtainCommandCNF(param));
				outln("%s", support);
				return true;
			}
			@Override
			public String help() {
				return 	"Finds the symbolic support (SP) for the given hypothesis.\n" + 
						"This is an expensive operation that involves finding the symbolic quasi-support for the\n" +
						"hypothesis and the inconsistency. Then the intersection is computed to find the SP.\n" +
						"Finding the SP is not needed for finding the (numerical) dsp which can be computed using only dqs values." +
						"This operation doesn't trigger any numeric calculations.\n" +
						"Parameters: <hypothesis>\n" +
						"	hypothesis: A propositional sentence in the CNF form, e.g.: '(a b)+(c d)+(d e f)'";
			}
		};
		commandsCLI.put("find_sp", findSP);
		commandsCLI.put("sp", findSP);

		CLICommand generateDisplayGraph = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				if(bddDotFile == null)
					throw new CommandException("Dot file not specified.");
				if(numResolver == null) // If non-numeric ops is done we'll also have a numResolver, no easy way to stop that.
					throw new CommandException("No numeric operation attempted yet.");
				if(!new File(bddDotFile).exists())
					throw new CommandException("No dot file exists - have you run a numeric operation yet?");

				String graphFile = bddDotFile + ".png";
				if(param != null)
					graphFile = param;
				
				notifier.println("Starting graphviz conversion.");
				// TODO: Create option to select viewer, create automated discovery to find viewer, and suppress it if it fails.
				// TOOD: Add option to specify different type of graph.
				// TODO: Add optional parameter to specify different output file name.
				Process process = new ProcessBuilder("dot","-Tpng", bddDotFile, "-o" + graphFile).start();
				try {
					synchronized (process) {
						if(process.isAlive())
							process.wait(10000);							
					}
				} catch (InterruptedException e) { }
				if(process.isAlive())
					notifyln("Conversion process %s running for too long.", process.toString());
				else if(process.exitValue() != 0)
					notifyln("Failed with exist code: %d (dot must be in PATH)", process.exitValue());
				else
				{
					new ProcessBuilder("xdg-open", bddDotFile + ".png").start();
				}
				return true;
			}
			@Override
			public String help() {
				return 	"Generate and display the BDD dot graph.\n" + 
						"This needs 'dot' executable to be in path (e.g. graphviz installed).\n" +
						"The viewer should exist on a recent Gnome desktop - furter support TBD.\n" +
						"Current dot file is: " + bddDotFile;
			}
		};
		commandsCLI.put("generate_display_graph", generateDisplayGraph);
		commandsCLI.put("gdg", generateDisplayGraph);
		
		CLICommand exit = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				return false;
			}
			@Override
			public String help() {
				return "Exits PAS console.";
			}
		};
		commandsCLI.put("exit", exit);
		
		CLICommand help = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				if(param == null)
				{
					Map<CLICommand, Map.Entry<List<String>, String>> commandToHelp = new LinkedHashMap<>();
					for(Map.Entry<String, CLICommand> entry : commandsCLI.entrySet())
					{
						CLICommand command = entry.getValue();
						String commandKey = entry.getKey();
						Map.Entry<List<String>, String> commandEntry = commandToHelp.get(command);
						if(commandEntry == null)
						{
							String helpFirstLine = command.help().split("\n")[0];
							List<String> commandsList = new ArrayList<>(Arrays.asList(commandKey));
							commandToHelp.put(command, new AbstractMap.SimpleEntry<List<String>, String>(commandsList, helpFirstLine));
						}
						else
						{
							List<String> commandsList = commandEntry.getKey();
							commandsList.add(commandKey);
						}
					}
					// Sort the entries into a list to that we can show the commands in order.
					// This picks up the first name for a command and uses that to sort alphabetically.
					List<Map.Entry<CLICommand, Map.Entry<List<String>, String>>> sortedCommands = FluentIterable.from(commandToHelp.entrySet())
							.toSortedList( (o1, o2) -> o1.getValue().getKey().get(0).compareTo(o2.getValue().getKey().get(0)));					
					
					notifyln("The command format is as follows:");
					notifyln("command: parameters");
					notifyln("");
					notifyln("A command is followed by a colon (i.e. ':') and any parameters. Each command has its own parameter scheme following the ':'.");
					notifyln("");
					notifyln("For example, to get help about help try:");
					notifyln("help: help");
					notifyln("");
					notifyln("The commands defined are below. The alias(es) for the command are inside [] where exist(s):");
					notifyln("An optional parameter is specified using [parameter] and can be omitted. A mandatory parameter is");
					notifyln("specified using <parameter> and has to be present for the command to succeed.");
					notifyln("");
					for(Map.Entry<CLICommand, Map.Entry<List<String>, String>> commandToHelpEntry : sortedCommands)
					{
						Map.Entry<List<String>, String> commandEntry = commandToHelpEntry.getValue();
						List<String> commandsList = commandEntry.getKey();
						
						String commandNames;
						if(commandsList.size() == 1)
							commandNames = commandsList.get(0);
						else // more than one names
							commandNames = String.format("%s %s", commandsList.get(0), commandsList.subList(1, commandsList.size()));
						notifyln("%s : %s", commandNames, commandEntry.getValue());
					}
					notifyln("");
					notifyln("Type 'help: command' to get more detailed help for each commmand.");
					notifyln("");
					notifyln("You can visit the project page at: http://openpas.steweche.co.uk for more help.");					
				}
				else
				{
					CLICommand commandFound = commandsCLI.get(param);
					if(commandFound == null)
						throw new CommandException("Uknown command for help: " + param);
					notifyln("%s:", param);
					notifier.print(commandFound.help());
					notifyln("");
				}
				return true;
			}
			@Override
			public String help() {
				return 	"Command to get help.\n" +
						"Parameters: command\n" +
						"	command : The command to get help about.";
			}
		};
		commandsCLI.put("help", help);
		commandsCLI.put("h", help);
		
		CLICommand save = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				
				PrintStream saver;
				if(param != null)
					saver = new PrintStream(new FileOutputStream(param));
				else
					saver = notifier;

				try
				{
					String initLine;
					if(usePC == ProbabilityComputerType.BDD)
						initLine = "bdd," + numBddNodes;
					else if(usePC == ProbabilityComputerType.SPExpansion)
						initLine = "sp";
					else
						throw new CommandException("Uknown PC type during save.");
					
					saver.printf("#PASC save @%s\n", new Timestamp(new Date().getTime()));
					
					saveForPASC(saver, pas, initLine);			
					
					notifyln("Save successful.");
				}
				finally
				{
					if(saver != notifier)
						saver.close();
				}
				
				return true;
			}
			
			@Override
			public String help() {
				return 	"Saves the current PAS instance. If a file is given as a parameter, it is used.\n" +
						"Otherwise, it will print the state into the output\n" +
						"Parameters: [file_path]\n" +
						"	file_path: A valid and writable location in the file system";
			}
		};
		commandsCLI.put("save", save);
		
		CLICommand run = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException 
			{
				if(param == null)
					throw new CommandException("No file to load specified - need file parameter.");

				notifyln("Running file: %s", param);
				try(InputStream fis = new FileInputStream(param))
				{
					boolean canContinue = executeStream(fis);
					if(!canContinue)
						return false; //exits PASC					
				}
				notifyln("File loaded and run successfully.");

				return true;
			}
			@Override
			public String help() {
				return 	"Loads and runs a specified OPS file.\n" +
						"Parameters: <filename>\n" +
						"	filename: A valid and readable file in the file system, typically a file with an .ops extension";
			}
		};
		commandsCLI.put("run", run);
		
		CLICommand exec = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				if(param == null)
					throw new CommandException("No file specified for execution.");
				executeCommandAsBinary(param, null, true);
				return true;
			}
			@Override
			public String help() {
				return 	"Executes the given external executable with args.\n" + 
						"Parameters: <executable> [args]\n" +
						"	executable: Path to a valid executable on the file system.\n" +
						"	args: Any number of optional args. Can be not quoted, double-quoted, or single-quoted.";
			}
		};
		commandsCLI.put("exec", exec);
		commandsCLI.put("!", exec);

		CLICommand shellExec = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				if(param == null)
					throw new CommandException("No params specified for execution.");
				executeCommandAsBinary("bash -c", Arrays.asList(param), true);
				return true;
			}
			@Override
			public String help() {
				return 	"Executes the given shell commands. These may include shell expansions.\n" + 
						"Parameters: <shell commands>\n" +
						"	shell commands: Any number of shell commands which are passed to the shell.\n" +
						"Current shell is harded coded to be bash and must be accesible in path (TBE).\n" +
						"The given parameters here are passed to 'bash -c' as extra parameters.";
			}
		};
		commandsCLI.put("shell_exec", shellExec);
		commandsCLI.put("!s", shellExec);

		CLICommand to_cnf = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("Parameter as DNF needed.");
				
				SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF(param);
				outln(OpenPAS.getFactory().getConverter().convertDNF(dnf).toString());
				return true;
			}
			@Override
			public String help() {
				return 	"Converts a given DNF to CNF form.\n" +
						"Parameters: <dnf>\n" + 
						"	dnf: A propositional sentence in DNF form.";
			}
		};
		commandsCLI.put("to_cnf", to_cnf);

		CLICommand to_dnf = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				if(param == null)
					throw new CommandException("Parameter as CNF needed.");
				
				SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF(param);
				outln(OpenPAS.getFactory().getConverter().convertCNF(cnf).toString());
				return true;
			}
			@Override
			public String help() {
				return 	"Converts a given CNF to DNF form.\n" +
						"Parameters: <cnf>\n" + 
						"	cnf: A propositional sentence in CNF form.";
			}
		};
		commandsCLI.put("to_dnf", to_dnf);

		CLICommand scenarios = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();
				ensureNumResolver();
				if(param == null)
					throw new CommandException("Parameter as DNF needed.");
				
				SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF(param);
				Scenarios.printScenarios(dnf, pas, numResolver, cmd_out);
				return true;
			}
			@Override
			public String help() {
				return 	"Runs a scenario analysis on the given DNF.\n" +
						"Parameters: <dnf>\n" + 
						"	dnf: A propositional sentence in DNF form.\n" +
						"This works by extracting all the assumptions from the given DNF. Then these are put\n" +
						"on a truth table. The table looks at things like whether the given assumption assignment\n" +
						"satisfies the DNF, whether it's part of the KBs inconsistency, and the probability of the \n" +
						"assumption assignment. This may be an assignmen for all the assumptions in which case these \n" +
						"would correspond to PAS scenarios. If they are partial assignments they may contain partial \n" +
						"inconsistencies which would be separated using additional assumptions not considered in the table.";
			}
		};
		commandsCLI.put("scenarios", scenarios);
		
		CLICommand stats = new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException, IOException {
				verifyInitialised();

				Stats.printPASStats(pas, cmd_out);
				return true;
			}
			@Override
			public String help() {
				return 	"Prints various stats about the current PAS instance.";
			}
		};
		commandsCLI.put("stats", stats);
		
		CLICommand setSep= new CLICommand() {			
			@Override
			public boolean execute(String param) throws CommandException, KBException {
				verifyInitialised();
				if(param == null)
					throw new CommandException(
							"set_parameter_seperator needs single character parameter to specify the new parameter separator. Current value: " + sepParameters);
				if(param.length() != 1)
					throw new CommandException("Separator needs to be a single character.");
				sepParameters = param;
				return true;
			}
			@Override
			public String help() {
				return 	"Changes the separator used by the parser to split parameters.\n" +
						"Parameters: [sep_character]\n" + 
						"	sep_character: A single character. Default value:,";
			}
		};
		commandsCLI.put("set_parameter_seperator", setSep);
		commandsCLI.put("sep", setSep);		
	}

	public static boolean executeStream(InputStream inStream) throws IOException, CommandException, KBException 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
						
		boolean canContinue;
		String line;
		while((line = reader.readLine()) != null)
		{
			notifyln(".: %s", line);
			canContinue = executeLine(line);
			if(!canContinue)
				return false; // exits PASC
		}
		return true;
	}

	static boolean readAndProcessCommand() throws CommandException, KBException
	{
		notifier.print("> ");
		String line = "";
		try {
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return executeLine(line);
	}

	public static boolean executeLine(String line) throws CommandException, KBException {
		if(line.length() == 0)
			return true; //allow empty lines
		if(line.startsWith("#"))
			return true; //allow comments
				
		String[] commandArgs = line.trim().split("[ ]*:[ ]*");
		
		try 
		{
			String param = null;
			if(commandArgs.length > 2)
				throw new CommandException("Command syntax error with ':'.");
			if(commandArgs.length > 1)
				param = commandArgs[1].replaceFirst("#.*$", "").trim();
			
			CLICommand commandFound = commandsCLI.get(commandArgs[0]);
			if(commandFound == null)
				throw new CommandException("Unknown command given: " + commandArgs[0]);
			return commandFound.execute(param);
		} 
		catch (java.lang.NumberFormatException e) 
		{
			// Convert number error to command error.
			throw new CommandException("Error parsing number (" + e.getMessage() + ")");
		} catch (IOException e) {
			throw new CommandException("Error in file I/O (" + e.getMessage() + ")");
		}
	}

	static SimpleSentence<LogicalAnd, LogicalOr> obtainCommandCNF(String param) throws KBException 
	{
		SimpleSentence<LogicalAnd, LogicalOr> hypo;
		if(param.contains("->"))
		{
			hypo = OpenPAS.getFactory().createCNFSentence();
			hypo.addElement(pas.constructHornClause(param));
		}
		else
		{
			// Check if we have a CNF or a clause
			if(param.contains("("))
				hypo = pas.constructCNF(param);				
			else // Parse input as clause and construct the CNF if possible
			{
				Expression<LogicalOr> clause = pas.constructClause(param);
				hypo = OpenPAS.getFactory().createCNFSentence(Arrays.asList(clause));
			}
		}
		return hypo;
	}
	
	protected static void verifyInitialised() throws CommandException
	{
		if(pas == null)
			throw new CommandException("System not initialised.");
	}
	
	protected static void clearNumResolver()
	{
		if(numResolver != null)
		{
			notifyln("Clearing numeric resolver.");
			numResolver = null;			
		}
	}

	protected static void ensureNumResolver() {
		if(numResolver == null)
		{
			int numAssumptions = Iterables.size(pas.getAssumptions(true));
			
			ProbabilityComputer pc = null;
			if(usePC == ProbabilityComputerType.BDD)
			{
				notifyln("Creating BDD probability computer with %d nodes", numBddNodes);
				String dotfile = null;
				if(bddDotFile != null && numAssumptions <= numMaxAssumptionsForDot)
				{
					dotfile = bddDotFile;
					notifyln("Using dot file at: %s", bddDotFile);
				}
				pc = OpenPAS.createProbabilityComputerBDD(numBddNodes, dotfile);
			}
			else if(usePC == ProbabilityComputerType.SPExpansion)
			{
				notifyln("Creating SP expansion probability computer.", numBddNodes);
				pc = OpenPAS.createProabilityComputerSPX();
			}
			
			numResolver = OpenPAS.createNumericResolver(pas, OpenPAS.createImplicateResolver(pas), pc);
			if(numAssumptions >= numMinAssumptionsForNotifications && numResolver instanceof Notifying)
				((Notifying) numResolver).setNotifier(notifier);
		}
	}
	
	static void notifyln(String format, Object... args)
	{
		notifier.format(format, args);
		notifier.println();
	}
	static void outln(String format, Object...args)
	{
		cmd_out.format(format, args);
		cmd_out.println();
	}
	
	protected static InputStream executeCommandAsBinary(String commands, List<String> rawCommands, boolean printOutput) throws CommandException, IOException {
		// TODO: This is no good to parse escaped quotes - needs enhancements, can still use "'" or '"' though.
		Pattern escaped = Pattern.compile("[ ]*\\\"(.*?)\\\"|[ ]*\\'(.*?)\\'|[ ]*([^ ]+)");
		Matcher mArgs = escaped.matcher(commands);
		List<String> args = new ArrayList<>();
		while(mArgs.find())
		{
			if(mArgs.group(1) != null)
				args.add(mArgs.group(1));
			else if(mArgs.group(2) != null)
				args.add(mArgs.group(2));
			else if(mArgs.group(3) != null)
				args.add(mArgs.group(3));
			else
				throw new CommandException("Problem parsing args.");
		}
		
		if(rawCommands != null)
			for(String command : rawCommands)
				args.add(command);

		Process process = new ProcessBuilder(args).start();
		try {
			synchronized (process) {
				if(process.isAlive())
					process.wait(24 * 60 * 60 * 1000); // wait 24 hours by default TODO: Make this configurable.
			}
		} catch (InterruptedException e) { }

		if(process.getErrorStream().available() > 0)
		{
			notifyln("Execution error stream:");
			ByteStreams.copy(process.getErrorStream(), notifier);					
		}
		
		// Print stdout to output here if this is asked for, or we're about to throw
		// an exception.
		if(printOutput || process.isAlive() || process.exitValue() != 0)
			if(process.getInputStream().available() > 0)
			{
				notifyln("Execution output stream:");
				// Note that the output of the execution is considered to be the command output.
				ByteStreams.copy(process.getInputStream(), cmd_out);
			}

		if(process.isAlive())
		{
			notifyln("Execution running too long - will try to kill the process %s.", process.toString());
			process.destroy(); // ignore if this kill fails.
			throw new CommandException("Execution ran too long.");
		}
		
		if(process.exitValue() != 0)
		{
			throw new CommandException("Execution failed with exit code: " + Integer.toString(process.exitValue()));
		}
		
		return process.getInputStream();
	}

	/**
	 * Save a PAS instance so it can be loaded into PASC.
	 * @param saver where the output is to be printed.
	 * @param initLine Optional parameter (can be null) that creates an init line.
	 */
	public static void saveForPASC(PrintStream saver, PAS pas, String initLine) 
	{
		if(initLine != null)
		{
			saver.print("init: ");
			saver.println(initLine);						
		}
		else // init as default
			saver.println("init");
		
		saver.printf("#Assumptions:\n");
		for(Assumption a : pas.getAssumptions(true))
			saver.printf("create_assumption: %s,%f\n", a.getName(), a.getProbability());
		saver.printf("#Propositions:\n");
		saver.print("create_proposition: ");
		for(Proposition p : pas.getPropositions(true))
		{
			saver.print(p.getName());
			saver.print(",");
		}
		saver.println();
		saver.printf("#KB:\n");
		LogicalStringer hs = OpenPAS.getFactory().getHornStringer();
		for(Expression<LogicalOr> expr : pas.getKB().getElements())
			saver.printf("add_horn: %s\n", hs.stringise(expr));
	}

	protected static void initialiseCommon() {
		defineCommands();

		// Delete the dot file on start up to avoid re-using a previous file.
		if(bddDotFile != null)
			new File(bddDotFile).delete();
	}

	public static void main(String args[])
	{		
		initialiseCommon();

		if(args.length > 0)
		{
			notifier = new PrintStream(ByteStreams.nullOutputStream()); // suppress notifications in file mode
			//TODO: This should be a log file (or even a syslog facility) specified by parameter.
			cmd_out = System.out;

			// File mode.
			try(FileInputStream fis = new FileInputStream(args[0])) 
			{
				executeStream(fis);
				fis.close();
			} 
			catch (FileNotFoundException e1) {
				System.err.println(String.format("Input file not found: %s", args[0]));
				System.exit(1);
			} catch (IOException e) {
				System.err.println(String.format("I/P problem executing: %s", args[0]));
				e.printStackTrace(System.err);
				System.exit(1);
			} catch (CommandException e) {
				System.err.println(String.format("Error executing command: %s", e));
				e.printStackTrace(System.err);
				System.exit(1);
			} catch (KBException e) {
				System.err.println(String.format("PAS error executing command: %s", e));
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		else
		{
			// Could've been nice to make notifier .err for a different colour but Eclipse mixes the order with that even with flushing.
			notifier = System.out;
			cmd_out = System.out;

			notifyln("Welcome to the OpenPAS PASC console");
			notifyln("");
			notifyln("This is a simple console application to serve as an interactive analysis and example platform.");
			notifyln("");
			notifyln("Copyright 2017 Burak Cetin");
			notifyln("");
			notifyln("This program comes with ABSOLUTELY NO WARRANTY.");
			notifyln("This is free software, and you are welcome to redistribute it under certain conditions.");
			notifyln("");
			notifyln("Enter 'help' to get help.");			
			notifyln("");			

			reader = new BufferedReader(new InputStreamReader(System.in));
			
			boolean cont = true;
			do
			{
				try
				{
					cont = readAndProcessCommand();
				} catch (CommandException e) 
				{
					notifyln("Command error: %s", e.getMessage());
				} catch (KBException e) {
					notifyln("PAS error: %s", e.getMessage());
				}			
			} while(cont);

			notifyln("Good bye.");
		}
	}
}

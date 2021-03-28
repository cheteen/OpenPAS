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

package openpas;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.ProbabilityComputer;
import openpas.utils.MutableDouble;
import openpas.utils.PairT;

/**
 * A class that implements the ProbabilityComputer interface using a binary decision diagrams (BDD) based implementation. <br>
 * 
 * The underlying BDD library used is JavaBDD. Last I checked it didn't have any 64-bit binaries for Buddy or CUDD
 * but the pure Java library is working w/o any problems. As far as I can see, JavaBDD is not maintained, last project commit in 2011.
 * I will try to find out what happened to it. <br>
 * 
 * The probability calculation algorithm used in this class comes essentially untouched out of my MS thesis:
 * B. Cetin, “Probabilistic Argumentation Systems Entity-Transitive Relation-Implication model and its efficient applications,”
 * Master’s thesis, Bogazici University, 2005.<br>
 * 
 * It uses existing ideas but needs to be further documented, explained, and references where possible.
 * Also the code by now is archaic (e.g. predating generics) and could use a face-lift.
 */
class ProbabilityComputer_BDD implements ProbabilityComputer
{
	protected int mBddNumNodes;
	protected String mDotFilePath;
	
	public ProbabilityComputer_BDD(int bddNumNodes)
	{
		this(bddNumNodes, null);
	}

	public ProbabilityComputer_BDD(int bddNumNodes, String dotFilePath)
	{
		mBddNumNodes = bddNumNodes;		
		mDotFilePath = dotFilePath;
	}

	@Override
	public double computeDNFProbability(SimpleSentence<LogicalOr, LogicalAnd> dnf) {
		return computeDNFProbability(dnf, mBddNumNodes, mDotFilePath);
	}
	
	protected static PairT<BDD, List<Assumption>> createBDDfromDNF(BDDFactory bddf, SimpleSentence<LogicalOr, LogicalAnd> dnf) {
		List<Assumption> listAsmts = SentenceUtils.extractAssumptionsFromDNF(dnf);
		if(listAsmts == null)
			return null;
	
		int numAsmts = listAsmts.size();
		bddf.setVarNum(numAsmts);    	
		bddf.autoReorder(BDDFactory.REORDER_SIFT);
		
		// Map literals on the array
		Map<Integer, Integer> mapIndex2Array = new HashMap<Integer, Integer>(numAsmts);
		for(int ix = 0; ix < numAsmts; ++ix)
			mapIndex2Array.put(listAsmts.get(ix).getIndex(), ix);
		
		// Let's construct the DNF as a BDD
		BDD bddDNF = bddf.zero();
		for(Expression<LogicalAnd> trm : dnf.getElements())
		{
			// Construct the single term
			BDD bddTrm = bddf.one();
			for(Literal lit : trm.getLiterals())
			{
				int ivar = mapIndex2Array.get(lit.getIndex());
				if(lit.getNeg())
					bddTrm.andWith(bddf.nithVar(ivar));
				else
					bddTrm.andWith(bddf.ithVar(ivar));
			}
			bddDNF.orWith(bddTrm);
		}
		
		return new PairT<BDD, List<Assumption>>(bddDNF, listAsmts);
	}

	// Public util function to get numeric results using BDDs from DNFs.
	public static double computeDNFProbability(SimpleSentence<LogicalOr, LogicalAnd> dnf, int bddNumNodes, String outdotfile) 
	{
		if(dnf.isFalse())
			return 0;
		if(dnf.isTrue())
			return 1;
		
		// Initialize BDD - "" selects the pure-Java BDD which is the only one possible right now with 64-bit arch.
		BDDFactory bddf = BDDFactory.init("", bddNumNodes, bddNumNodes);
		try
		{
			// Convert the DNF to a BDD
			PairT<BDD, List<Assumption>> bl = ProbabilityComputer_BDD.createBDDfromDNF(bddf, dnf);
			if(bl == null)
				return Double.NaN; // This will happen when the DNF specified has propositions in it.
			BDD bddDNF = bl.first;
			// To make sense of the BDD we need the matching list of assumptions used.
			List<Assumption> listAsmts = bl.second;
			
			// Pack probabilities into a double array for the function used below.
			int numAsmts = listAsmts.size();
			double[] probs = new double[numAsmts];
			for(int ix = 0; ix < numAsmts; ++ix)
				probs[ix] = listAsmts.get(ix).getProbability();
			
			if(outdotfile != null)
			{
				boolean ok = true;
				PrintStream cout = System.out;
				try
				{
					PrintStream psFile = new PrintStream(new FileOutputStream(outdotfile));
					System.setOut(psFile);
					bddDNF.printDot();
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
					ok = false;
				}
				finally
				{
					System.setOut(cout);
				}
				if(ok)
				{
					try 
					{
						String contents = new String(Files.readAllBytes(Paths.get(outdotfile)));
						
						for(int ix = 0; ix < numAsmts; ++ix)
						{
							// replace generic labels from BDDs with the real labels we have.
							contents = contents.replace(String.format("[label=\"%d\"]", ix), String.format("[label=\"%s\"]", listAsmts.get(ix).getName()));
						}
						
						try(  PrintWriter out = new PrintWriter(outdotfile) )
						{
						    out.println( contents );
						}
					}
					catch (IOException e) 
					{
						e.printStackTrace();
						ok = false;
					}					
				}
			}
	
			double dqs = computeBDDProb(bddf, bddDNF, probs);
			return dqs;
		}
		finally
		{
			bddf.done();
		}
	}

	static protected double computeBDDProb(BDDFactory bfRelevant, BDD bddIni, double pr[]) 
	{
		// Handle exceptional cases.
		if(bddIni.isOne())
			return 1;
		if(bddIni.isZero())
			return 0;
		
    	// To keep probabilities for nodes on the BDD
    	// It should be ok to use HashMap because, "buddy" has good hash codes
    	Map<BDD, MutableDouble> nodeMsgs = new HashMap<BDD, MutableDouble>(bfRelevant.getNodeTableSize());

    	// To keep active nodes
    	BDDComparator comparador = new BDDComparator(bddIni.nodeCount(), bfRelevant);
    	SortedSet<BDD> setSubBDD = new TreeSet<BDD>(comparador);
    	
    	setSubBDD.add(bddIni.id());
    	nodeMsgs.put(bddIni,new MutableDouble(1.0)); //pr[bfRelevant.level2Var(bddIni.level())]
    	
    	while(!setSubBDD.isEmpty()) {
    		
    		// Process the first element (the lowest levelled BDD)
    		BDD current = setSubBDD.first();
    		setSubBDD.remove(current);
    		
    		MutableDouble message = nodeMsgs.get(current);
    		double varprob = pr[bfRelevant.level2Var(current.level())];
    	
    		BDD subbdd = current.low();
    		double effprob = 1 - varprob;    		
    		// Process node
    		if(!subbdd.equals(bfRelevant.zero())) {
    			MutableDouble submsg = nodeMsgs.get(subbdd);
    			if(submsg == null) { // the sub node never received a token
    				nodeMsgs.put(subbdd,new MutableDouble(message.doubleValue() * effprob));
    				if(!subbdd.equals(bfRelevant.one())) // don't go beyond the "1" node!
    					setSubBDD.add(subbdd);
    			} else {
    				submsg.setDoubleValue(submsg.doubleValue() + message.doubleValue() * effprob);
    			}
    		}
    		
    		// Exactly same as above (ie.like a macro) for the higher branch
    		subbdd = current.high();
    		effprob = varprob;    		
    		// Process node
    		if(!subbdd.equals(bfRelevant.zero())) {
    			MutableDouble submsg = nodeMsgs.get(subbdd);
    			if(submsg == null) { // the sub node never received a token
    				nodeMsgs.put(subbdd, new MutableDouble(message.doubleValue() * effprob));
    				if(!subbdd.equals(bfRelevant.one())) // don't go beyond the "1" node!
    					setSubBDD.add(subbdd);
    			} else {
    				submsg.setDoubleValue(submsg.doubleValue() + message.doubleValue() * effprob);
    			}
    		}
    		
    		// BDD not used anymore.
        	current.free();
        	// TODO: Is above necessary? Does .low .high create clones?
        	// TODO: Is it worth to remove done BDD entries from the nodeMsgs map?
    	}
    	 
    	// return the "accumulated" messages for terminal node 1
    	return (nodeMsgs.get(bfRelevant.one())).doubleValue();
    }
    
    static private class BDDComparator implements Comparator<BDD> {
    	protected HashMap<BDD, Integer> mapNodesDiscovered;
    	protected int levelWidth;
    	protected int numNodesEachLevel[];
    	public BDDComparator(int nodeCount, BDDFactory bddf) {
    		mapNodesDiscovered = new HashMap<BDD, Integer>(bddf.getNodeTableSize());
    		levelWidth = nodeCount; // as large as to keep all nodes in one level
    		numNodesEachLevel = new int[nodeCount];
    	}
    	protected Integer addNodeToOrdering(BDD newnode) {
    		int nodeLevel = newnode.level();
    		int currentPopul = numNodesEachLevel[nodeLevel];
    		int nodesOrder = nodeLevel * levelWidth + currentPopul;
    		mapNodesDiscovered.put(newnode, new Integer(nodesOrder));
    		numNodesEachLevel[nodeLevel] = ++currentPopul;
    		// TODO: Use mutableint, don't create nodes order twice unnecessarily.
    		return new Integer(nodesOrder);
    	}
        public int compare(BDD o1, BDD o2) {
        	Integer or1 = mapNodesDiscovered.get(o1);
        	if(or1 == null)
        		or1 = addNodeToOrdering(o1);
        	
        	Integer or2 = mapNodesDiscovered.get(o2);
        	if(or2 == null)
        		or2 = addNodeToOrdering(o2);

        	return or1.intValue() - or2.intValue();
        }
    }
}

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

package examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import analytics.Stats;
import openpas.OpenPAS;
import openpas.basics.Assumption;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.utils.Notifying;
import openpas.utils.SimpleRange;
import pasc.PASC;

/**
 * This is an example program to showcase the use of OpenPAS to solve a probabilistic problem. It models
 * and finds the probability of drawing a Poker pair from a deck. There's a matching
 * {@link PokerHandsCompact} example that shows how to do this in a more compact manner, but this example
 * should be easier to understand at first. <br><br>
 * 
 * For modelling a draw of N cards from a deck we create  assumptions of cards being dealt. We need N 
 * assumptions for each card being dealt one after another, e.g. for 5 draws:<br><br>
 * spade, club, heart, and diamond, A, K, Q, J, 10-2 	<br>
 * e.g. <br>
 * D1_SA, D1_SK, ..., D1_D3, D1_D2  (52)		<br>
 * ...											<br>
 * D5_SA, D5_SK, ..., D5_D3, D5_D2  (52)		<br>
 *<br><br>
 * 5 * 52 = 260 dealing card assumptions
 *<br><br>
 * Then we add the following rules:<br>
 * 1) Only one card can be drawn at a time.<br>
 * 2) A card has to drawn each time.<br>
 * 3) A given card only be drawn once.<br><br>
 *
 * Then we define 52 propositions for "having been dealt a card" no matter the order.<br>
 * e.g.	SA, SK, ..., D3, D2 (52)<br><br>
 *
 * A good analysis can be seen here: https://en.wikipedia.org/wiki/List_of_poker_hands
 * We only analyse a pair here: having any card with the same rank. There are 13x C(4,2) = 78 different types of pairs.<br><br>
 *
 * This is a naive implementation that assigns a new assumption for each draw of a card resulting in an explosion
 * of scenarios. We can only analyse the case for two draws using this model and OpenPAS. This involves
 * 104 assumptions and ~ 2e31 possible scenarios to analyse. OpenPAS manages this in ca. 15 hours
 * on a modest laptop (as of 2017) (see {@link PokerHandsCompact} for a much faster version and more details about performance).<br><br>
 * 
 * To get quicker response play with the NUM_DRAWS/NUM_SUITS/NUM_RANKS values below. For example, an assignment of
 * 2/2/2 returns immediate results. Analysing smaller models of problems makes it easier to spot problems.<br><br>
 * 
 * The idea for this program is that, you run it to generate an .ops file which allows an import into {@link PASC}
 * using the 'run' command. Then you can do interactive analysis. The 'scenarios' command is a useful command
 * to analyse how your PAS model works.<br><br>
 * 
 * When run, this program will write a PASC importable PAS instance to disk, and continue to calculate the
 * dsp for "got_pair" which is the proposition for getting a Poker pair.
 * 
 * @see {@link PokerHandsCompact} for a different approach to the same problem which actually works for 5 draws.
 */

public class PokerHandsNaive {
	
	final static String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
	final static String[] SUITS = {"C", "D", "H", "S"};//

	// Modify these parameter to get quicker results and simpler models.
	final static int NUM_RANKS = 13; // max=RANKS.length
	final static int NUM_SUITS = 4; // max=SUITS.length
	final static int NUM_DRAWS = 5;
	
	public static String getCardName(int s, int rank) {
		return SUITS[s] + RANKS[rank];
	}
	public static String getCardName(int index) {
		return SUITS[index / NUM_RANKS] + RANKS[index % NUM_RANKS];
	}
	
	public static String getDrawnCardName(int draw, int s, int rank) {
		return "draw" + Integer.toString(draw) + "_" + getCardName(s, rank);
	}
	public static String getDrawnCardName(int draw, int index) {
		return getDrawnCardName(draw, index / NUM_RANKS, index % NUM_RANKS);
	}

	public static void main(String[] args) throws KBException, FileNotFoundException {
		PAS pas = OpenPAS.createPAS();
		PropFactory fac = OpenPAS.getFactory();
		
		final int numCards = NUM_SUITS * NUM_RANKS;
		
		// Create an assumption for drawing a card in each draw.
		// Each draw has an increasing probability of getting a card as there are fewer
		// cards left.
		for(int i = 0; i < NUM_DRAWS; ++i)
			for(int j = 0; j < NUM_SUITS; ++j)
				for(int k = 0; k < NUM_RANKS; ++k)
					pas.createAssumption(getDrawnCardName(i, j, k), false, 1.0 / (numCards - i));
		
		// Only one card can be drawn during a draw.
		for(int i = 0; i < NUM_DRAWS; ++i)
			for(int j = 0; j < numCards - 1; ++j)
				for(int k = j + 1; k < numCards; ++k)
					pas.addHornClause(Arrays.asList(
							pas.getAssumption(getDrawnCardName(i, j), true),
							pas.getAssumption(getDrawnCardName(i, k), true)
							),
							fac.getFalse());
		
		// One card has to be drawn in a draw
		for(int i = 0; i < NUM_DRAWS; ++i)
		{
			final int draw = i; // keep compiler happy to create a (kind of) closure.
			Iterable<Assumption> notCardsInADraw = SimpleRange.range(numCards)
					.transform((card) -> pas.getAssumption(getDrawnCardName(draw, card), false));
			
			// Add that if no card is drawn from a draw, this is a falsity.
			pas.addHornClause(notCardsInADraw, fac.getFalse());
		}
		
		// A card can be drawn once between draws.
		for(int i = 0; i < NUM_DRAWS - 1; ++i)
			for(int j = i + 1; j < NUM_DRAWS; ++j)
				for(int k = 0; k < numCards; ++k)
					pas.addHornClause(Arrays.asList(
							pas.getAssumption(getDrawnCardName(i, k), true),
							pas.getAssumption(getDrawnCardName(j, k), true)
							),
							fac.getFalse()
							);
		
		// If a card has been drawn at any draw, we have it.
		for(int i = 0; i < numCards; ++i)
		{
			Proposition have = pas.createProposition("have_" + getCardName(i), false);
			for(int j = 0; j < NUM_DRAWS; ++j)
				pas.addHornClause(Arrays.asList(pas.getAssumption(getDrawnCardName(j, i), true)), have);
		}
		
		// If we have the same rank of two suits, that's a pair.
		Proposition gotPair = pas.createProposition("got_pair", false);
		for(int i = 0; i < NUM_RANKS; ++i)
			for(int j = 0; j < NUM_SUITS - 1; ++j)
				for(int k = j + 1; k < NUM_SUITS; ++k)
				{
					Proposition have1 = pas.getProposition("have_" + getCardName(j, i), true);
					Proposition have2 = pas.getProposition("have_" + getCardName(k, i), true);
					pas.addHornClause(Arrays.asList(have1, have2), gotPair);
				}

		File outfile = new File("poker_hands_naive_pairs.ops");
		PrintStream fps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
		PASC.saveForPASC(fps, pas, null);
		fps.close();
		
		System.out.println("PokerHandsNaive:");
		System.out.println("NUM_RANKS = " + NUM_RANKS);
		System.out.println("NUM_SUITS = " + NUM_SUITS);
		System.out.println("NUM_DRAWS = " + NUM_DRAWS);
		
		System.out.println("Wrote PAS instance to file " + outfile.getAbsolutePath());
		System.out.println("You can load the contents of this file into PASC using the 'run' command.");
		System.out.println();
		
		System.out.println("PAS stats:");
		Stats.printPASStats(pas, System.out);
		System.out.println();

		SimpleSentence<LogicalAnd, LogicalOr> senGotPair = pas.constructCNF("(got_pair)");
		System.out.println("Calculating dsp for " + senGotPair);
		
		NumericResolver nr = OpenPAS.createNumericResolver(pas, OpenPAS.createImplicateResolver(pas), OpenPAS.createProbabilityComputerBDD(1024*1024));
		((Notifying) nr).setNotifier(System.out);
		long start = System.nanoTime();
		double dsp = nr.calcNormalisedDSP(senGotPair);
		System.out.println();
		System.out.println("dsp(h) = " + dsp); // this is the probability of pulling a pair
		// We can compute this same result as follows (for a draw of two cards): p(pair) = 13 * C(4,2) / C(52,2)
		System.out.println("(" + (System.nanoTime() - start)/1e9 + " sec taken)");
	}
}

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

import com.google.common.collect.Iterables;

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
 * This is an example program that finds the probability of drawing a Poker pair from a deck. There's a matching
 * {@link PokerHandsNaive} example that shows how to do this in a straight forward manner. Here we have a comparison
 * of the two approaches. <br><br>
 *
 * This example approaches the problem differently by using a more compact representation of each drawn card.
 * Instead of assigning each card an assumption we use 4 assumptions to represent a rank (note 2^4=16),
 * and a further two assumptions to represent the suit. Note there's a total of 4 suits and 13 ranks giving us the 52 cards. <br><br>
 * 
 * This way, we get 6 assumptions (note 2^6=64 > 52) to represent a drawn card from a deck. This way of drawing cards means
 * that an assignment of assumptions corresponds to a single card drawn at a time (ie. avoids contradictions).<br><br>
 * 
 * The remaining 14 cards in a deck out of 64 are marked as contradictory - hence out of consideration.<br><br>
 * 
 * We define a set of 6 assumption for each draw. That gives us 5 * N draws assumptions overall to represent a draw of N cards
 * from a standard deck.<br><br>
 * 
 * We mark that when a card is drawn once the other draws become contradictory. Any assignment of assumptions corresponds
 * to a draw - and that gives us the final requirement that a card has to be drawn in a draw.<br><br>
 * 
 * Here are some metrics between this example and the naive version. Times are taken on a modest laptop (as of 2017) and timings 
 * are here to give a rough idea and so not very methodically captured:<br>
 * <br>
 * 								NUM_RANKS	NUM_SUITS	NUM_DRAWS	#assumptions 	~#scenarios	~duration (sec)	<br>
 * {@link PokerHandsNaive}		13			2			3			78				3e23		900				<br>
 * 								13			4			2			104				2e31		186				<br>
 * 								8			4			2			64				2e19		21				<br>
 * 								13			4			3			156				9e46		57000			<br>
 * 								13			4			4			260				2e78		-				<br>
 * 								13			4			5			260				2e78		-				<br>
 * 								
 * <br>
 * {@link PokerHandsCompact}	13			2			3			15				3e4			3				<br>
 * 								13			4			2			12				4e3			2				<br>
 * 								8			4			2			10				1e3			1				<br>
 * 								13			4			3			18				3e5			43				<br>
 * 								13			4			4			24				2e7			7000			<br>
 * 								13			4			5			30				1e9			-				<br>
 * <br>
 * 
 * It can be seen that the {@link PokerHandsCompact} runs much faster, and there's a non-linear relation between the runtime
 * and the number of assumptions even though more assumptions generally cause slower runs. Most of the time spent here
 * tends to be during the symbolic QS finding which is an area improvement in OpenPAS.
 * 
 * Unfortunately neither version can fully handle the 5 draws case which is arguably the most interesting/useful one. Further OpenPAS
 * improvements should make this possible (e.g. use of a BDD based symbolic engine).
 * 
 * The idea with these examples is that you run it to generate an .ops file, and then can continue playing with these PAS instances
 * inside {@link PASC}. You can modify the variables NUM_DRAWS, NUM_RANKS, and NUM_SUITS to investigate different configurations. <br><br>
 * 
 * It may be instructive to reduce these numbers to e.g. 2/2/2 and perform a "scenarios" analysis in PASC to get a close look into
 * how PAS works under the cover.<br><br>
 * 
 * When run, the program continues to attempt to calculate the dsp for "got_pair" which is the Poker pair proposition. This is
 * to give an idea about how to programmatically perform numerical computations.
 * 
 * @see {@link PokerHandsNaive} A naive implementation of the same problem.
 */
public class PokerHandsCompact {

	static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
	static final String[] SUITS = {"D", "S", "H", "C"}; //order must match getDrawnSuit function

	// Modify these to get different results.
	static final int NUM_RANKS = 13; // max = RANKS.length
	static int NUM_SUITS = 4; // max = SUITS.length (remove final from here to keep Eclipse happy: https://bugs.eclipse.org/bugs/show_bug.cgi?id=397971)
	static final int NUM_DRAWS = 2;

	static final String FMT_CARD_DRAWN_DIGIT = "draw%d_digit%d";
	static final String FMT_CARD_DRAWN_SUIT_COLOUR = "draw%d_suit_black";
	static final String FMT_CARD_DRAWN_SUIT_ROUND = "draw%d_suit_round";
	static final String FMT_HAVE_CARD = "have_%s";
		
	static final int NUM_CARDS = NUM_RANKS * NUM_SUITS;
	static final int NUM_DIGITS = (int) Math.ceil(Math.log(NUM_RANKS)/Math.log(2));

	static String getCardName(int index) {
		return getCardName(index / NUM_RANKS, index % NUM_RANKS);
	}
	static String getCardName(int suit, int rank)
	{
		return SUITS[suit] + RANKS[rank];		
	}

	// Return a set of assumptions that represent this card. This is the core of more compact representation
	// that we use a group of assumptions to represent an integer (the card rank) when considered together.
	static Iterable<Assumption> getDrawnCardRank(PAS pas, int draw, int rank)
	{
		// This selects the assumptions and their states to get this rank of card.
		// e.g. for draw 0 and rank 4 we loop for ceil(log_2(4)) = 2 times, and we select 100 -> false, false, true for assumptions
		// draw0_digit0, draw0_digit1, draw0_digit2 in reverse order to "1 0 0" which becomes:
		// draw 0 card 4 (binary 100) = draw0_digit0 = false, draw0_digit1 = false, draw0_digit2 = true
		// This could've been done more elegantly if Java supported true closures with yield.
		return SimpleRange.range(NUM_DIGITS)
			.transform( (digit) -> pas.getAssumption(String.format(FMT_CARD_DRAWN_DIGIT, draw, digit), (rank >> digit) % 2 == 1));
	}
	
	static Iterable<Assumption> getDrawnSuit(PAS pas, int draw, int suit)
	{
		// In here we imply the following list of four suits:
		// 0: black = false	round = false	-> diamond
		// 1: black = true	round = false	-> spade
		// 2: black = false	round = true	-> heart
		// 3: black = true	round = true	-> club
		if(NUM_SUITS == 4)
			return Arrays.asList(
					pas.getAssumption(String.format(FMT_CARD_DRAWN_SUIT_COLOUR, draw), suit % 2 == 1),
					pas.getAssumption(String.format(FMT_CARD_DRAWN_SUIT_ROUND, draw), (suit >> 1) % 2 == 1)
					);
		else if(NUM_SUITS == 2)
			return Arrays.asList(pas.getAssumption(String.format(FMT_CARD_DRAWN_SUIT_COLOUR, draw), suit % 2 == 1));
		throw new IllegalStateException("Unsupported number of suits.");
	}
	
	// For a full deck this should return a list of 4 + 2 = 6 assumptions set to true or false
	// representing a card that's drawn in a draw.
	static Iterable<Assumption> getCard(PAS pas, int draw, int index)
	{
		return Iterables.concat(
				getDrawnCardRank(pas, draw, index % NUM_RANKS),
				getDrawnSuit(pas, draw, index / NUM_RANKS)
				);
	}
	
	public static void main(String[] args) throws KBException, FileNotFoundException {
		
		PAS pas = OpenPAS.createPAS();
		PropFactory fac = OpenPAS.getFactory();
		
		for(int draw = 0; draw < NUM_DRAWS; ++draw)
		{
			// This is going to be 4 for 13 cards since 2^4=16 is the smallest in that covers up to 0->12.
			for(int digit = 0; digit < NUM_DIGITS; ++digit)
				pas.createAssumption(String.format(FMT_CARD_DRAWN_DIGIT, draw, digit), false, 0.5);
			
			// Cards 13->15 are invalid in all the draws
			for(int rank = NUM_RANKS; rank < (1 << NUM_DIGITS); ++rank)
				pas.addHornClause(getDrawnCardRank(pas, draw, rank), fac.getFalse());
			
			// It's worth explaining how probabilities work here. We set 0.5 for each assumption, so for 4
			// assumptions and 13 cards we get 1/16 probability of drawing a card:
			//
			// p(card) = 1/2 * 1/2 * 1/2 * 1/2 = 1/16
			//
			// When 3 of these are set as inconsistent scenarios the probability of pulling an inconsistent card is 3/16,
			// and so the probability of pulling a valid/consistent card is: 1 - 3/16 = 13/16.
			//
			// Then in the end we normalise the results to get p'(card) as follows:
			//
			// p'(card) = 1/16 / 13/16 = 13/16
			//
			// as needed.

			// Define suit assumptions
			pas.createAssumption(String.format(FMT_CARD_DRAWN_SUIT_COLOUR, draw), false, 0.5);
			if(NUM_SUITS > 2)
				pas.createAssumption(String.format(FMT_CARD_DRAWN_SUIT_ROUND, draw), false, 0.5);
		}
		
		// 1) Only one card can be drawn in a draw: This comes for free since a given arrangement of the six assumptions
		// can only represent a single card.
		
		// 2) A card has to be drawn in a draw: This comes for free since any arrangement of the assumptions is a draw.

		// 3) A card can only be drawn once, so if we draw the same card in two separate draws it's a falsity.
		// This doesn't come for free, we need to declare this a contradiction ourselves.
		// It gives us [C(num_draws, 2) * num_cards] clauses to add.
		for(int index = 0; index < NUM_CARDS; ++index)
			for(int draw1 = 0; draw1 < NUM_DRAWS - 1; ++draw1)
				for(int draw2 = draw1 + 1; draw2 < NUM_DRAWS; ++draw2)
					pas.addHornClause(Iterables.concat(
							getCard(pas, draw1, index),
							getCard(pas, draw2, index)
							), fac.getFalse());
		
		// This set up allows us to represent a draw of 5 cards from a deck using 5x6=30 assumptions.
		
		// Define the propositions that we have a card
		for(int draw = 0; draw < NUM_DRAWS; ++draw)
			for(int ix = 0; ix < NUM_CARDS; ++ix)
			{
				Proposition have = pas.createProposition(String.format(FMT_HAVE_CARD, getCardName(ix)), false);
				// Now map the assumptions to the propositions
				pas.addHornClause(getCard(pas, draw, ix), have);
			}
				
		// Define what a pair looks like.
		Proposition gotPair = pas.createProposition("got_pair", false);
		for(int rank = 0; rank < NUM_RANKS; ++rank)
			for(int suit1 = 0; suit1 < NUM_SUITS - 1; ++suit1)
				for(int suit2 = suit1 + 1; suit2 < NUM_SUITS; ++suit2)
				{
					Proposition have1 = pas.getProposition(String.format(FMT_HAVE_CARD, getCardName(suit1, rank)), true);
					Proposition have2 = pas.getProposition(String.format(FMT_HAVE_CARD, getCardName(suit2, rank)), true);
					pas.addHornClause(Arrays.asList(have1, have2), gotPair);
				}
		
		File outfile = new File("poker_hands_compact_pairs.ops");
		PrintStream fps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
		PASC.saveForPASC(fps, pas, null);
		fps.close();
		
		System.out.println("PokerHandsCompact:");
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
		System.out.println("(" + (System.nanoTime() - start)/1e9 + " msec taken)");
	}
}

package fopas;

import java.util.Comparator;
import java.util.List;

import fopas.FOTermByRecursionImpl.FOTermVariable;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOTerm;
import fopas.basics.FOTerm.TermType;
import fopas.basics.FOVariable;

/**
 * This class exists to group together relations that have the following properties:
 * 
 * a) They have exactly two args.
 * b) They implement a relation where the same function is applied to both args the relation continues to hold.
 *    The simplest such relation is an equality.
 *    
 * The main functionality implemented in this particular class will be to implement a common constrain functionality.
 * What really needs to happen here is a full-fledged equation resolving function.
 * 
 * This resolver needs to resolve the inequality (or other comparison) where x may be a part of a term on either (or both) args.
 * The hardest case in here is the following:
 * 
 * f(t1, t2, ..., x, ..., t{n-1}, a{n}) < g(u1, u2, ..., x, ..., u{n-1}, u{n})
 * 
 * where f and g are functions corresponding to each of the two terms, x is the variable we're trying to constrain,
 * ti and ui are terms with assignments. If we got this far for a constrain then the partial assignment must've succeeded
 * such that all tis and uis are assigned. There are also simpler cases for variables and constants which are trivial and 
 * have to be handled here as well, but let's focus on the harder function case.
 * 
 * We need to resolve for function h such that:
 * 
 * x < h(t1, t2, ..., tn, u1, u2, ..., un)
 * 
 * What I will here is a simple starter where it'll handle the only scenarios where x exists as a bare arg on either
 * side of the comparison:
 * 
 * x < t1
 * 
 * or
 * 
 * t1 < x
 * 
 * @param <T>
 */
abstract public class FORelationOfComparison<T extends FOElement> extends FORelationImpl<T>
{
	FORelationOfComparison(String name)
	{
		super(name);
	}
	
	static class FORelationImplEquals extends FORelationOfComparison<FOElement>
	{
		FORelationImplEquals()
		{
			super("Equals");
		}
	
		@Override
		public int getCardinality() { return 2; }
	
		@Override
		public String getInfix()
		{
			return "=";
		}
	
		@Override
		public int getPrecedence()
		{
			return 2000;
		}
	
		@Override
		public boolean satisfies(FOElement... args)
		{
			if(args.length != 2)
				throw new FORuntimeException("Expected 2 args, got " + args.length + ".");
			if(args[0] == null || args[1] == null)
				throw new FORuntimeException(String.format("Got null arg(s): %s/%s", args[0], args[1]));
			
			return args[0].equals(args[1]);
		}

		@Override
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			assert terms.size() == 2;
			// We need to figure out which arg is the variable and which one is the "other" (non-variable) arg.
			// It's possible none of the args related to the variable, in this case other will be null.
			
			FOTerm other = null;
			if(terms.get(0).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(0)).getVariable().equals(var))
				other = terms.get(1);
			if(terms.get(1).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(1)).getVariable().equals(var))
			{
				if(other != null)
				{
					// This is the case where both args are the variable in question, so either everything is true or false depending on the complement.
					// This will return the universe in the non-complement case, but crucially, in the complemented case,
					// it'll return an empty set which say for no element of the universe it can be true.
					// This does a trick in negating the complement on the empty set to get back the universe set in a different way.
					return new FOSetUtils.EmptySet<>()
							.complement(universeSubset, !isComplemented);
				}
				
				// The first arg is the non-variable arg.
				other = terms.get(0); 
			}
			
			// Check if the other term is not a partial assignment, then we can constrain the subset to a single element.
			if(other != null && other.getAssignment() != null)
			{
				return new FOSetUtils.SingleElementSet<FOElement>(other.getAssignment())
							.complement(universeSubset, isComplemented);
			}


			// We don't have anything better at the moment, return the original set.
			return universeSubset;
		}
	}
	
	static class FORelationImplInequality extends FORelationOfComparison<FOElement>
	{
		protected final boolean mLessThan;
		protected final boolean mEquals;
		protected final Comparator<FOElement> mOrder;
		FORelationImplInequality(Comparator<FOElement> order, boolean lessThan, boolean equals)
		{
			super(createName(lessThan, equals));
			mOrder = order;
			mLessThan = lessThan;
			mEquals = equals;
		}
		
		protected static String createName(boolean lessThan, boolean equals)
		{
			if(lessThan)
				if(equals)
					return "LessThanEquals";
				else
					return "LessThan";
			else
				if(equals)
					return "GreaterThanEquals";
				else
					return "GreaterThan";
		}
	

		@Override
		public int getCardinality() { return 2; }
	
		@Override
		public String getInfix()
		{
			// This is inconsistent with how name is handled, so, not good, but leave it be for now.
			if(mLessThan)
				if(mEquals)
					return "<=";
				else
					return "<";
			else
				if(mEquals)
					return ">=";
				else
					return ">";
		}
	
		@Override
		public int getPrecedence()
		{
			if(mLessThan)
				if(mEquals)
					return 2005;
				else
					return 2006;
			else
				if(mEquals)
					return 2007;
				else
					return 2008;
		}
	
		@Override
		public boolean satisfies(FOElement... args)
		{
			if(args.length != 2)
				throw new FORuntimeException("Expected 2 args, got " + args.length + ".");
			if(args[0] == null || args[1] == null)
				throw new FORuntimeException(String.format("Got null arg(s): %s/%s", args[0], args[1]));

			int compare = mOrder.compare(args[0], args[1]);
			if(compare == 0)
				return mEquals;

			return mLessThan ^ compare < 0;
		}
		
		@Override
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			assert terms.size() == 2;
			// We need to figure out which arg is the variable and which one is the "other" (non-variable) arg.
			// It's possible none of the args related to the variable, in this case other will be null.
			
			boolean inverseTermOrder = false;
			
			FOTerm other = null;
			if(terms.get(0).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(0)).getVariable().equals(var))
			{
				assert terms.get(0).getAssignment() == null;
				other = terms.get(1);
			}
			if(terms.get(1).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(1)).getVariable().equals(var))
			{
				assert terms.get(1).getAssignment() == null;
				
				// This is the case where both args are the variable in question, so either everything is true or false depending on the complement.
				if(other != null)
				{
					// This does a trick in negating the complement on the empty set to get back the universe set in a different way.
					// Should consider changing the API here to signal the failure to contsrain in a different way than returning the universet set
					// so that when we do return the universe set here it means that's what the constrain did instead of having to create another
					// fake universe set).
					// Case: v1 <= v1
					if(mEquals) // This mimics the equality case above.
						return new FOSetUtils.EmptySet<>()
							.complement(universeSubset, !isComplemented);
					else
						// Case: v1 < v1
						// Same as above but w/o the negation on the complement.
						return new FOSetUtils.EmptySet<>()
								.complement(universeSubset, isComplemented);					
				}

				// The first arg is the non-variable arg.
				other = terms.get(0);
				inverseTermOrder = true;
			}
			
			// No constraining possible if the variable isn't present in this formula.
			if(other == null)
				return universeSubset;
			
			// If we have no assignment on the other term, we may be able to get something by solving the equation for var.
			// We don't support that _yet_, so we need to skip for now.
			FOElement termAssignment = other.getAssignment(); 
			if(termAssignment == null)
				return universeSubset;
			
			// If universe subset isn't ordered, we don't have an intrinsic way to constrain the set.
			// The forall iteration will be constrained by the relation already, so we will need to leave it to that phase.
			if(!(universeSubset instanceof FOOrderedEnumerableSet))
				return universeSubset;
			
			FOOrderedEnumerableSet<FOElement> fosetOEUniverseSubset = (FOOrderedEnumerableSet<FOElement>) universeSubset;
			// Check if the universe subset has the same order this inequality expects.
			if(!fosetOEUniverseSubset.getOrder().equals(mOrder))
				return universeSubset;
			
			if(inverseTermOrder ^ mLessThan)
			{
				if(mEquals)
					return fosetOEUniverseSubset.constrainToRange(fosetOEUniverseSubset.getFirstOrInfinite(), termAssignment)
							.complement(universeSubset, isComplemented);
				else
				{
					FOElement prev = fosetOEUniverseSubset.getPreviousOrNull(termAssignment);
					if(prev == null)
						return new FOSetUtils.EmptySet<>()
								.complement(universeSubset, isComplemented);
					else
						return fosetOEUniverseSubset.constrainToRange(fosetOEUniverseSubset.getFirstOrInfinite(), prev)
								.complement(universeSubset, isComplemented);
				}
			}
			else
			{
				if(mEquals)
					return fosetOEUniverseSubset.constrainToRange(termAssignment, fosetOEUniverseSubset.getLastOrInfinite())
							.complement(universeSubset, isComplemented);
				else
				{
					FOElement next = fosetOEUniverseSubset.getNextOrNull(termAssignment);
					if(next == null)
						return new FOSetUtils.EmptySet<>()
								.complement(universeSubset, isComplemented);
					else
						return fosetOEUniverseSubset.constrainToRange(next, fosetOEUniverseSubset.getLastOrInfinite())
								.complement(universeSubset, isComplemented);
				}
			}
		}
	}	
}

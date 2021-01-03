package fopas;

import java.util.List;

import fopas.FOTermByRecursionImpl.FOTermVariable;
import fopas.basics.FOElement;
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
		public boolean satisfies(FOElement... args)
		{
			if(args.length != 2)
				throw new FORuntimeException("Expected 2 args, got " + args.length + ".");
			if(args[0] == null || args[1] == null)
				throw new FORuntimeException(String.format("Got null arg(s): %s/%s", args[0], args[1]));
			
			return args[0].equals(args[1]);
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
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			assert terms.size() == 2;
			FOTerm other = null;
			if(terms.get(0).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(0)).getVariable().equals(var))
				other = terms.get(1);
			if(terms.get(1).getType() == TermType.VARIABLE && ((FOTermVariable) terms.get(1)).getVariable().equals(var))
			{
				if(other != null)
				{
					// This will return the universe in the non-complement case, but crucially, in the complemented case,
					// it'll return an empty set which say for no element of the universe it can be true.
					return new FOSetUtils.EmptySet<>(String.format("%s|%s", universeSubset.getName(), mName))
							.complement(universeSubset, !isComplemented);
				}
				
				other = terms.get(0); 
			}
			
			// Check if the other term is not a partial assignment, then we can contrain the subset to a single element.
			if(other != null && other.getAssignment() != null)
			{
				return new FOSetUtils.SingleElementSet<FOElement>(
						String.format("%s|%s", universeSubset.getName(), mName), other.getAssignment())
							.complement(universeSubset, isComplemented);
			}


			// We don't have anything better at the moment, return the original set.
			return universeSubset;
		}
	}
}

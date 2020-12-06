package fopas;

import java.util.Iterator;

import fopas.basics.FOFormula;
import fopas.basics.FOTerm;

/**
 * This is an implementation that's closely tied to how the few ByRecusion classes
 * are implemented.
 * @author Burak Cetin
 *
 */
public class FOByRecursionStringiser
{
	String stringiseFOFormula(FOFormula form, int maxLen)
	{
		StringBuffer sb = new StringBuffer();
		stringiseFOFormula(form, maxLen, sb);
		if(sb.length() > maxLen)
			return sb.substring(0, maxLen - 3) + "...";
		return sb.toString();
	}

	void stringiseFOFormula(FOFormula form, int maxLen, StringBuffer sb)
	{
		if(sb.length() >= maxLen)
			return;
		
		if(form.isNegated())
			sb.append("¬");

		FOFormulaByRecursionImpl recform = (FOFormulaByRecursionImpl) form;
		switch(recform.getType())
		{
		case FOR_ALL:
			FOFormulaByRecursionImpl.FOFormulaBRForAll recformall = (FOFormulaByRecursionImpl.FOFormulaBRForAll) recform;
			sb.append("(forall _");
			sb.append(recformall.getVariable().getName());
			sb.append(")");
			stringiseFOFormula(recformall.getScopeFormula(), maxLen, sb);
			break;
		case OR:
			FOFormulaByRecursionImpl.FOFormulaBROr recformor = (FOFormulaByRecursionImpl.FOFormulaBROr) recform;
			//FOFormulaByRecursionImpl.FOFormulaBROr.SubType subtype = recformor.getSubType();
			sb.append("(");
			Iterator<FOFormula> subforms = recformor.getFormulas().iterator();
			if(!subforms.hasNext())
				break;
			FOFormula nextform = subforms.next();
			stringiseFOFormula(nextform, maxLen, sb);
			while(subforms.hasNext())
			{
				if(sb.length() >= maxLen)
					return;

				nextform = subforms.next();
				sb.append(" | ");
				stringiseFOFormula(nextform, maxLen, sb);
			}
			sb.append(")");
			break;
		case RELATION:
			sb.append("(");
			FOFormulaByRecursionImpl.FOFormulaBRRelation recformrel = (FOFormulaByRecursionImpl.FOFormulaBRRelation) recform;
			if(recformrel.getRelation().getClass() == FORelationImpl.FORelationImplEquals.class)
			{
				Iterator<FOTerm> termit = recformrel.getTerms().iterator();
				FOTerm term = termit.next();
				stringiseFOTerm(term, maxLen, sb);
				sb.append(" = ");
				term = termit.next();
				stringiseFOTerm(term, maxLen, sb);				
			}
			else
			{
				sb.append(recformrel.getRelation().getName());
				sb.append("(");
				Iterator<FOTerm> termit = recformrel.getTerms().iterator();
				FOTerm term = termit.next();
				stringiseFOTerm(term, maxLen, sb);
				while(termit.hasNext())
				{
					term = termit.next();
					sb.append(",");
					stringiseFOTerm(term, maxLen, sb);
				}
				sb.append(")");
			}
			sb.append(")");
			break;
		case ALIAS_BINDING:
			FOAliasByRecursionImpl.FOAliasBindingByRecursionImpl foalias = (FOAliasByRecursionImpl.FOAliasBindingByRecursionImpl) recform;
			sb.append(foalias.getName());
			sb.append("(");
			Iterator<FOTerm> termit = foalias.getBoundTerms().iterator();
			if(termit.hasNext())
			{
				FOTerm argTerm = termit.next();
				stringiseFOTerm(argTerm, maxLen, sb);
				while(termit.hasNext())
				{
					argTerm = termit.next();
					sb.append(", ");
					stringiseFOTerm(argTerm, maxLen, sb);
				}				
			}
			sb.append(")");
			break;
			
		default:
			sb.append("ERROR!"); // should never happen.
			break;
		}
	}
	
	void stringiseFOTerm(FOTerm term, int maxLen, StringBuffer sb)
	{
		if(sb.length() >= maxLen)
			return;
		
		FOTermByRecursionImpl recterm = (FOTermByRecursionImpl) term;
		switch(recterm.getType())
		{
		case VARIABLE:
			FOTermByRecursionImpl.FOTermVariable rectermvar = (FOTermByRecursionImpl.FOTermVariable) term;
			sb.append("_");
			sb.append(rectermvar.getVariable().getName());
			break;
		case CONSTANT:
			FOTermByRecursionImpl.FOTermConstant rectermconst = (FOTermByRecursionImpl.FOTermConstant) term;
			sb.append(rectermconst.getConstant().getName());
			break;
		case FUNCTION:
			FOTermByRecursionImpl.FOTermFunction rectermfun = (FOTermByRecursionImpl.FOTermFunction) term;
			String infix = rectermfun.getFunction().getInfix();
			Iterable<FOTerm> terms = rectermfun.getTerms();
			Iterator<FOTerm> termit = terms.iterator();
			if(infix == null)
			{
				sb.append(rectermfun.getFunction().getName());
				infix = ","; // don't have an infix but an explicit function call, so "," acts like an infix.
			}
			sb.append("(");				
			FOTerm interm = termit.next();
			stringiseFOTerm(interm, maxLen, sb);
			while(termit.hasNext())
			{
				sb.append(" ");
				sb.append(infix);
				sb.append(" ");
				interm = termit.next();
				stringiseFOTerm(interm, maxLen, sb);
			}
			sb.append(")");
			
			break;
		default:
			sb.append("ERROR!"); // should never happen.
			break;		
		}		
	}
}

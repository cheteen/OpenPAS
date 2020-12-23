package fopas;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.FluentIterable;

import fopas.FOFormulaBRForAll.ForAllSubtype;
import fopas.FOFormulaBROr.OrSubType;
import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

/**
 * This is an implementation that's closely tied to how the few ByRecusion classes
 * are implemented.
 * @author Burak Cetin
 *
 */
public class FOByRecursionStringiser
{
	static class FOFormulaBRForAllPresenter extends FOFormulaBRForAll
	{
		FOFormulaBRForAllPresenter(FOFormulaBRForAll fbrfa)
		{
			super(fbrfa.isNegated(), fbrfa.getVariable(), fbrfa.getScopeFormula(), fbrfa.getOriginalSubtype());
		}
		
		boolean presentNegated()
		{
			if(mSubtype == ForAllSubtype.FOR_ALL)
				return mNegated;
			else if(mSubtype == ForAllSubtype.EXISTS)
				return !mNegated;
			else
				throw new FORuntimeException("Unexpected scope command type.");
		}
		
		ForAllSubtype presentSubtype()
		{
			return mSubtype;
		}
		
		FOFormula presentScopeFormula()
		{
			if(mSubtype == ForAllSubtype.FOR_ALL)
				return mScopeFormula;
			else if(mSubtype == ForAllSubtype.EXISTS)
			{
				try
				{
					return mScopeFormula.negate();
				} catch (FOConstructionException e)
				{
					throw new FORuntimeException("Unexpected exception negating sentence.");
				}
			}
			else
				throw new FORuntimeException("Unexpected scope command type.");
		}
	}
	
	static class FOFormulaBROrPresenter extends FOFormulaBROr
	{
		FOFormulaBROrPresenter(FOFormulaBROr formOrImpl)
		{
			// Ok using implementation level variables isn't good here, but saves us an unnecessary list creation,
			// and it's only a little bad.
			super(formOrImpl.mNegated, formOrImpl.mFormulas, formOrImpl.mSubType);
		}
		
		@Override
		Iterable<FOFormula> presentFormulas()
		{
			if(getOriginalSubType() == OrSubType.AND)
			{
				return FluentIterable.from(super.presentFormulas()).transform( form -> {
					try {
						return form.negate();
					} catch (FOConstructionException e) {
						e.printStackTrace();
						return null; // This should never throw really.
					}
				});				
			}
			else if(getOriginalSubType() == OrSubType.IMP)
			{
				if(mFormulas.size() != 2)
					throw new FORuntimeException("Incorrectly created formula found.");
				try
				{
					return FluentIterable.from(Arrays.asList(mFormulas.get(0).negate(), mFormulas.get(1)));
				} catch (FOConstructionException e)
				{
					throw new FORuntimeException("Incorrectly created formula found - cannot negate.");
				}
			}
			else if(getOriginalSubType() == OrSubType.OR)
			{
				return super.presentFormulas();				
			}
			else
				throw new FORuntimeException("Unexpected logical op found!");
		}
		
		@Override
		boolean presentNegated()
		{
			if(getOriginalSubType() == OrSubType.AND)
				return !super.isNegated();
			else if(getOriginalSubType() == OrSubType.OR || getOriginalSubType() == OrSubType.IMP)
				return super.isNegated();
			else
				throw new FORuntimeException("Unexpected formula subtype found.");
		}
		
		@Override
		OrSubType presentSubType()
		{
			// Can present as is.
			return super.getOriginalSubType();
		}
	}
	
	final FOLanguage mLang;
	public FOByRecursionStringiser()
	{
		this(new FOLanguage());
	}
	FOByRecursionStringiser(FOLanguage lang)
	{
		mLang = lang;
	}
	
	String stringiseFOFormula(FOFormula form, int maxLen)
	{
		return stringiseFOFormula(form, maxLen, true);
	}
	
	String stringiseFOFormula(FOFormula form, int maxLen, boolean useExtended)
	{
		StringBuffer sb = new StringBuffer();
		stringiseFOFormula(form, maxLen, useExtended, sb);
		if(sb.length() > maxLen)
			return sb.substring(0, maxLen - 3) + "...";
		return sb.toString();
	}

	void stringiseFOFormula(FOFormula form, int maxLen, boolean useExtended, StringBuffer sb)
	{
		if(sb.length() >= maxLen)
			return;
		
		FOFormulaBRImpl recform = (FOFormulaBRImpl) form;

		if(useExtended && recform.getType() == FormulaType.OR)
			recform = new FOFormulaBROrPresenter((FOFormulaBROr) recform);
		else if(useExtended && recform.getType() == FormulaType.FOR_ALL)
			recform = new FOFormulaBRForAllPresenter((FOFormulaBRForAll) recform);
		
		if(recform.presentNegated())
			sb.append("¬");

		switch(recform.getType())
		{
		case FOR_ALL:
			FOFormulaBRForAll recformall = (FOFormulaBRForAll) recform;
			sb.append("(");
			if(recformall.presentSubtype() == ForAllSubtype.FOR_ALL)
				sb.append(mLang.getForAll());
			else if(recformall.presentSubtype() == ForAllSubtype.EXISTS)
				sb.append(mLang.getExists());
			else
				throw new FORuntimeException("Unexpected scope command found.");	
			sb.append(" _");
			sb.append(recformall.getVariable().getName());
			sb.append(")");
			stringiseFOFormula(recformall.presentScopeFormula(), maxLen, useExtended, sb);
			break;
		case OR:
			// We use a presenter formula here which pretends to be an AND or OR sentence.
			FOFormulaBROr recformor = (FOFormulaBROr) recform;
			
			sb.append("(");
			Iterator<FOFormula> subforms = recformor.presentFormulas().iterator();
			if(!subforms.hasNext())
				break;
			FOFormula nextform = subforms.next();
			stringiseFOFormula(nextform, maxLen, useExtended, sb);
			while(subforms.hasNext())
			{
				if(sb.length() >= maxLen)
					return;

				nextform = subforms.next();
				sb.append(" ");
				if(recformor.presentSubType() == OrSubType.OR)
					sb.append(mLang.getOr());
				else if(recformor.presentSubType() == OrSubType.IMP)
					sb.append(mLang.getImp());
				else if(recformor.presentSubType() == OrSubType.AND)
					sb.append(mLang.getAnd());
				else
				{
					sb.append("ERROR!");
					return;
				}
				sb.append(" ");
				stringiseFOFormula(nextform, maxLen, useExtended, sb);
			}
			sb.append(")");
			break;
		case RELATION:
			sb.append("(");
			FOFormulaBRRelation recformrel = (FOFormulaBRRelation) recform;
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
			String sep = rectermfun.getFunction().getInfix();
			Iterable<FOTerm> terms = rectermfun.getTerms();
			Iterator<FOTerm> termit = terms.iterator();
			FOFunctionImpl funimpl = (FOFunctionImpl) rectermfun.getFunction();
			boolean isExplicit = sep == null || !funimpl.presentInfix();
			if(isExplicit)
			{
				sb.append(rectermfun.getFunction().getName());
				sep = ","; // don't have an infix but an explicit function call, so "," acts like an infix.
			}
			sb.append("(");				
			FOTerm interm = termit.next();
			stringiseFOTerm(interm, maxLen, sb);
			while(termit.hasNext())
			{
				if(!isExplicit)
					sb.append(" ");
				sb.append(sep);
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

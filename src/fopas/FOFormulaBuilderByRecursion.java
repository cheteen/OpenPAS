package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import fopas.FOFormulaBuilderByRecursion.FOToken.Type;
import fopas.FOFormulaByRecursionImpl.FOFormulaBRForAll;
import fopas.FOFormulaByRecursionImpl.FOFormulaBROr;
import fopas.FOFormulaByRecursionImpl.FOFormulaBRRelation;
import fopas.FOFormulaByRecursionImpl.FormulaType;
import fopas.FOTermByRecursionImpl.FOTermVariable;
import fopas.basics.FOAlias;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOFormulaBuilder;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;
import fopas.basics.ValidationRules;

public class FOFormulaBuilderByRecursion implements FOFormulaBuilder
{
	protected static class FOTokenScope
	{
		final FOVariable scopeVar;
		FOTokenScope(FOVariable scopeVar)
		{
			this.scopeVar = scopeVar;
		}
	}
	
	static class FOToken
	{
		enum Type
		{
			START_GROUP,
			END_GROUP,
			COMMA,
			NEGATION,
			LOGICAL_OP,
			RELATION,
			INFIX_RELATION_OP,
			FUNCTION,
			INFIX_FUNCTION_OP,
			SCOPE_COMMAND,
			VARIABLE,
			CONSTANT,
			END_SENTENCE,
			ALIAS,
			COMP_SUBFORMULA,
			COMP_SCOPE
		}
		final Type type;
		String value;
		FOFormula subformula;
		FOTokenScope tokenScope;
		FOToken(Type type, String value)
		{
			this.type = type;
			this.value = value;
		}
		FOToken(Type type, FOFormula subformula)
		{
			this.type = type;
			assert type == Type.COMP_SUBFORMULA;
			this.subformula = subformula;
		}
		FOToken(Type type, FOTokenScope tokenScope)
		{
			this.type = type;
			assert type == Type.COMP_SCOPE;
			this.tokenScope = tokenScope;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((subformula == null) ? 0 : subformula.hashCode());
			result = prime * result + ((tokenScope == null) ? 0 : tokenScope.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FOToken other = (FOToken) obj;
			if (subformula == null) {
				if (other.subformula != null)
					return false;
			} else if (!subformula.equals(other.subformula))
				return false;
			if (tokenScope == null) {
				if (other.tokenScope != null)
					return false;
			} else if (!tokenScope.equals(other.tokenScope))
				return false;
			if (type != other.type)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
	
	static class PosForward
	{
		int ixPos;
		PosForward(int ixPos)
		{
			this.ixPos = ixPos;
		}
	}
	
	static class TokenPart
	{
		int ixStart;
		int ixEnd;
		
		TokenPart(int ixStart, int ixEnd)
		{
			this.ixStart = ixStart;
			this.ixEnd = ixEnd;
		}
		
		int size()
		{
			return ixEnd - ixStart;
		}
	}
	
	// TODO: Need to test these two or remove them.
	// This builds a canned formula that should exist in all structures and is always true.
	public static FOFormula buildTautology()
	{
		FOVariable fox = new FOVariableImpl("x");
		FOTerm termX = new FOTermByRecursionImpl.FOTermVariable(fox);
		FOFormula formEq = new FOFormulaBRRelation(false, new FORelationImpl.FORelationImplEquals(), Arrays.asList(termX, termX));
		FOFormula formAll = new FOFormulaBRForAll(false, fox, formEq);
		return formAll;
	}
	
	// This builds a canned formula that should exist in all structures and is a contradiction.
	public static FOFormula buildContradiction()
	{
		FOVariable fox = new FOVariableImpl("x");
		FOTerm termX = new FOTermByRecursionImpl.FOTermVariable(fox);
		FOFormula formEq = new FOFormulaBRRelation(false, new FORelationImpl.FORelationImplEquals(), Arrays.asList(termX, termX));
		FOFormula formAll = new FOFormulaBRForAll(false, fox, formEq);
		FOFormula formNotAll = new FOFormulaBRForAll(true, fox, formEq);
		FOFormula formAnd = new FOFormulaBROr(true, Arrays.asList(formNotAll, formAll));
		return formAnd;
	}
	
	void buildMaps(FOStructure structure,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases
			)
	{
		for(FORelation<FOElement> rel : structure.getRelations())
		{
			if(rel.getInfix() != null)
				mapInfixRels.put(rel.getInfix(), rel);
			mapRels.put(rel.getName(), rel);			
		}
		
		for(FOFunction fun : structure.getFunctions())
		{
			if(fun.getInfix() != null)
				mapInfixFuns.put(fun.getInfix(), fun);			
			mapFuns.put(fun.getName(), fun);
		}
		
		for(FOConstant foconst : structure.getConstants())
		{
			mapConstants.put(foconst.getName(), foconst);
		}		
		
		for(String aliasName : structure.getAliases())
		{
			mapAliases.put(aliasName, structure.getAlias(aliasName));
		}		
	}
	
	@Override
	public FOAlias buildAlias(String name, List<FOVariable> args, String strform, FOStructure structure) throws FOConstructionException
	{
		return (FOAlias) buildFormula(strform, structure, name, args);
	}

	@Override
	public FOFormula buildFormula(String strform, FOStructure structure) throws FOConstructionException
	{
		return buildFormula(strform, structure, null, null);
	}
	
	// This builder is better than the previous one, but still not happy about it.
	// Need to possibly rewrite this in the future to:
	// - Combine infix operations for logical ops and relations.
	// - So that it recognises terms and formulas.
	// - Then inherent precedens of the infix operators can decide whether we're building a term or a formula.
	// - Each paranthesis pair is then processed using the above.
	// - This way the constructFomula method doesn't have to return null which is very ugly.
	public FOFormula buildFormula(String strform, FOStructure structure, String aliasName, List<FOVariable> aliasArgs) throws FOConstructionException
	{
		Map<String, FORelation<FOElement>> mapRels = new HashMap<>();
		Map<String, FORelation<FOElement>> mapInfixRels = new HashMap<>();		
		Map<String, FOFunction> mapFuns = new HashMap<>();
		Map<String, FOFunction> mapInfixFuns = new HashMap<>();
		Map<String, FOConstant> mapConstants = new HashMap<>();
		Map<String, FOFormula> mapAliases = new HashMap<>();
		
		buildMaps(structure, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
		FOAliasByRecursionImpl formAlias = null;
		if(aliasName != null)
		{
			formAlias = new FOAliasByRecursionImpl(aliasName, aliasArgs); 
			mapAliases.put(aliasName, formAlias);			
		}
		
		List<FOToken> tokens = parseTokens(strform, structure, mapRels, mapInfixRels,
				mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
		FOToken finalToken = buildSubformula(tokens, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, false);
		if(finalToken == null)
			throw new FOConstructionException("Did not find a valid formula to build.");
		
		FOFormula form = finalToken.subformula;
		
		if(formAlias != null)
		{
			formAlias.setScopeFormula(form);
			return formAlias;
		}
		else
			return form;
	}

	FOToken buildSubformula(List<FOToken> tokens,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases,
			boolean isNegated
			) throws FOConstructionException
	{
		for(int ixStart = 0; ixStart < tokens.size(); ++ixStart)
		{
			int ixEnd = -1;
			int paraInStart = ixStart + 1;
			boolean subNegation = false;
			if(tokens.get(ixStart).type == Type.START_GROUP || 
					tokens.get(ixStart).type == Type.NEGATION && tokens.get(ixStart + 1).type == Type.START_GROUP)
			{
				if(tokens.get(ixStart).type == Type.NEGATION)
				{
					subNegation = true;
					++paraInStart;
				}
				
				int paraDepth = 1;
				for(int i = paraInStart; i < tokens.size(); i++)
				{
					if(tokens.get(i).type == Type.END_GROUP)
					{
						paraDepth--;
						if(paraDepth == 0)
						{
							ixEnd = i;
							break;
						}
					}
					else if(tokens.get(i).type == Type.START_GROUP)
						paraDepth++;
				}					
			}
					
			if(ixEnd != -1)
			{
				FOToken compToken = buildSubformula(new ArrayList<>(tokens.subList(paraInStart, ixEnd)),
						mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, subNegation);
				// It's legit for building subformula to fail for something between parantheses.
				// It may still be valid syntax such as a function call.
				if(compToken != null)
					replaceTokens(tokens, ixStart, ixEnd + 1, compToken);
			}
		}
		
		// By this point the everything between parantheses should have been converted to subformulas in the tokens (sub)list.
		// Now look for "|" operations.
		List<TokenPart> splitted = splitTokens(tokens, new FOToken(Type.LOGICAL_OP, "|"));
		if(splitted != null)
		{
			for(TokenPart part : Lists.reverse(splitted))
			{
				if(part.size() == 1)
				{
					if(tokens.get(part.ixStart).type != Type.COMP_SUBFORMULA)
						throw new FOConstructionException("Error in logical op, expected subformula not found.");				
				}
				else
				{
					FOToken subformula = buildSubformula(tokens.subList(part.ixStart, part.ixEnd), mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, false);
					replaceTokens(tokens, part.ixStart, part.ixEnd, subformula);
				}
			}
		}
		
		// When "&" is added, it would be implemented here.
		
		// At this point we are looking at a single formula possibly with composite tokens or a command scope ,

		// Let's first get the command scope out of the way.
		if(tokens.get(0).type == Type.SCOPE_COMMAND)
		{
			int ixTok = 0;
			FOToken token = tokens.get(ixTok);
			ixTok++;
			assert token.value.equals("forall");
			
			if(tokens.get(ixTok).type != Type.VARIABLE)
				throw new FOConstructionException("Expected variable not found for command scope.");
			FOToken scopeTokenVariable = tokens.get(ixTok); 
			ixTok++;
			
			if(ixTok != tokens.size())
				throw new FOConstructionException("Unexpected token found in command scope.");
			
			FOVariable variable = new FOVariableImpl(scopeTokenVariable.value);
			return new FOToken(Type.COMP_SCOPE, new FOTokenScope(variable));
		}
		
		FOFormula formula;
		if(tokens.size() == 1 && tokens.get(0).type == Type.COMP_SUBFORMULA)
			// There was surrounding parantheses, so the whole thing got compressed to a single subformula.
			formula = tokens.get(0).subformula;
		else
		{
			// It's only a single formula possibly with composite tokens.
			formula = constructFormula(tokens, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, isNegated);
			if(formula == null)
				return null; // didn't find a formula here (it may still be valid syntax)
			isNegated = false; // We've embedded this negation in the subformula, so can remove this.
		}
		
		if(isNegated)
			return new FOToken(Type.COMP_SUBFORMULA, new FOFormulaByRecursionImpl.FOFormulaBROr(true, Arrays.asList(formula)));
		else
			return new FOToken(Type.COMP_SUBFORMULA, formula);
	}
	
	private List<TokenPart> splitTokens(List<FOToken> tokens, FOToken anchor) throws FOConstructionException
	{
		List<TokenPart> splitted = null;
		int ixStart = 0;
		
		for(int ixEnd = 0; ixEnd < tokens.size() + 1; ++ixEnd)
		{
			if(splitted == null && ixEnd == tokens.size())
				break;
			if((ixEnd == tokens.size()) ||  // the last part of a group found
					tokens.get(ixEnd).equals(anchor)
					)
			{
				if(ixStart == ixEnd)
					throw new FOConstructionException("Badly structured tokens list.");
				
				if(splitted == null)
					splitted = new ArrayList<>();
				
				splitted.add(new TokenPart(ixStart, ixEnd));
				ixStart = ixEnd + 1;
			}
		}
		
		return splitted;
	}

	private void replaceTokens(List<FOToken> tokens, int ixStart, int ixEnd, FOToken subformula)
	{
		//TODO: use sublist.clear idiom for this.
		for(int i = ixEnd - 1; i >= ixStart; --i)
			tokens.remove(i);
		tokens.add(ixStart, subformula);
	}
	
	FOFormula constructFormula(List<FOToken> tokens,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases,
			boolean isExternallyNegated
			) throws FOConstructionException
	{
		int ixToken = 0;
		
		boolean hasNegation = false;
		if(tokens.get(ixToken).type == Type.NEGATION)
		{
			hasNegation = true;
			ixToken++;
		}
		
		// This servers to collapse unnecessary parantheses.
		boolean isNegated = hasNegation ^ isExternallyNegated;
		
		FOFormula form;
		
		if(tokens.get(ixToken + 1).type == Type.LOGICAL_OP)
		{
			// At this point all subformulas should already have been created as composite tokens,
			// we just need to wrap them.
			
			// This option can't work with infix operators.
			if(hasNegation)
				throw new FOConstructionException("Unexpected negation token found.");

			FOToken tokLogOp = tokens.get(ixToken + 1);
			assert tokLogOp.value.equals("|");
			
			List<FOFormula> listFormulas = new ArrayList<>();
			while(ixToken < tokens.size())
			{
				FOToken tok = tokens.get(ixToken);
				if(tok.type != Type.COMP_SUBFORMULA)
					throw new FOConstructionException("Unexpected token found: " + tok.value);
				listFormulas.add(tok.subformula);
				ixToken++;
				
				if(ixToken == tokens.size())
					break;

				FOToken tokInLogical = tokens.get(ixToken);
				if(!tokLogOp.value.equals(tokInLogical.value))
					throw new FOConstructionException("Inconsistent logical op found: " + tokInLogical.value);
				ixToken++;
			}

			form = new FOFormulaByRecursionImpl.FOFormulaBROr(isNegated, listFormulas);
		}
		// forall formula
		else if(tokens.get(ixToken).type == Type.COMP_SCOPE)
		{
			// Like the above case, at this point all subformulas should already have been created as composite tokens,
			// let's wrap them.

			FOToken tokScope = tokens.get(ixToken);
			FOVariable variable = tokScope.tokenScope.scopeVar;
			ixToken++;

			FOToken tokScopeFormula = tokens.get(ixToken); 
			ixToken++;
			if(tokScopeFormula.type != Type.COMP_SUBFORMULA)
				throw new FOConstructionException("Expected scoped formula not found: " + tokScopeFormula.value);

			form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(isNegated, variable, tokScopeFormula.subformula);
		}
		// Relation formula (prefix)
		else if(tokens.get(ixToken).type == Type.RELATION)
		{
			FOToken tokRelation = tokens.get(ixToken);
			ixToken++;

			if(tokens.get(ixToken).type != Type.START_GROUP)
				throw new FOConstructionException("Synthax error with relation - expecting starting paranthesis.");
			ixToken++;

			PosForward pf = new PosForward(ixToken);
			List<FOTerm> terms = new ArrayList<FOTerm>();
			while(ixToken < tokens.size())
			{
				FOTerm term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				terms.add(term);

				FOToken tokInRel = tokens.get(pf.ixPos); 
				pf.ixPos++;
				if(tokInRel.type == Type.END_GROUP)
					break;

				if(tokInRel.type != Type.COMMA)
					throw new FOConstructionException("Synthax error with relation - expecting comma.");				
			}
			ixToken = pf.ixPos;
			
			FORelation<FOElement> rel = mapRels.get(tokRelation.value);
			assert rel != null; // tokeniser handles the existence of the relation.
			
			form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(isNegated, rel, terms);
		}
		// Relation formula (infix)
		// This is the most complex case. We could and should in the future support mixing of different infix relations in a formula.
		// We can handle this by introducing a precedence value for each infix relation.
		// But for now, for simplicity, assume only one infix relation can exist in a single-formula string.
		else if(tokens.get(ixToken + 1).type == Type.INFIX_RELATION_OP)
		{
			// This option can't work with infix operators.
			if(hasNegation)
				throw new FOConstructionException("Unexpected negation token found.");

			FOToken tokInfixRelation = tokens.get(ixToken + 1);
			
			List<FOTerm> terms = new ArrayList<FOTerm>();
			PosForward pf = new PosForward(ixToken);
			FOTerm term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
			terms.add(term);

			while(pf.ixPos < tokens.size())
			{
				FOToken tokInfixFound = tokens.get(pf.ixPos);
				if(tokInfixFound.type != Type.INFIX_RELATION_OP)
					break;
				
				if(!tokInfixFound.value.equals(tokInfixRelation.value))
					throw new FOConstructionException("Need to use parantheses for now. Inconsistent infix relation found: " + tokInfixRelation.value);				
				
				pf.ixPos++;
				
				term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				terms.add(term);
			}
			ixToken = pf.ixPos;
			
			FORelation<FOElement> rel = mapInfixRels.get(tokInfixRelation.value);
			assert rel != null; //tokeniser should handle this.

			form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(isNegated, rel, terms);			
		}
		else if(tokens.get(ixToken).type == Type.ALIAS)
		{
			FOToken tokAlias = tokens.get(ixToken); 
			ixToken++;

			if(tokens.get(ixToken).type != Type.START_GROUP)
				throw new FOConstructionException("Synthax error with relation - expecting starting paranthesis.");
			ixToken++;
			
			List<FOTerm> subterms = new ArrayList<FOTerm>();
			PosForward pf = new PosForward(ixToken);
			while(pf.ixPos < tokens.size())
			{
				FOTerm subterm = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				subterms.add(subterm);
			
				FOToken tokenInFun = tokens.get(pf.ixPos);
				pf.ixPos++;

				if(tokenInFun.type == Type.END_GROUP)
					break;

				if(tokenInFun.type != Type.COMMA)
					throw new FOConstructionException("Synthax error with function - expecting comma.");
			}
			ixToken = pf.ixPos;
			
			form = new FOAliasByRecursionImpl.FOAliasBindingByRecursionImpl(
					tokAlias.value, isNegated, (FOAliasByRecursionImpl) mapAliases.get(tokAlias.value), subterms);
		}
		else
			return null; // This used to throw exception, but now refuses to create a formula instead.
		
		if(ixToken != tokens.size())
			throw new FOConstructionException("Unexpected token found at the end.");
		
		return form;
	}
	
	FOTerm constructTerm(List<FOToken> tokens, PosForward pf,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants
			) throws FOConstructionException
	{
		FOToken token = tokens.get(pf.ixPos);
		if(token.type == Type.VARIABLE)
		{
			FOVariable fovar = new FOVariableImpl(token.value); //could consider pooling these at some point.
			FOTerm term = new FOTermByRecursionImpl.FOTermVariable(fovar);
			pf.ixPos++;
			return term;
		}
		else if(token.type == Type.CONSTANT)
		{
			FOConstant foconst = mapConstants.get(token.value);
			assert foconst != null; // this is handled by the tokeniser.
			FOTerm term = new FOTermByRecursionImpl.FOTermConstant(foconst);
			pf.ixPos++;
			return term;			
		}
		// prefix function
		else if(token.type == Type.FUNCTION)
		{
			FOFunction fofun = mapFuns.get(token.value);
			assert fofun != null; //tokeniser should handle this.
			pf.ixPos++;
			
			if(tokens.get(pf.ixPos).type != Type.START_GROUP)
				throw new FOConstructionException("Starting paranthesis not found.");
			pf.ixPos++;
			
			List<FOTerm> subterms = new ArrayList<FOTerm>();
			while(pf.ixPos < tokens.size())
			{
				FOTerm subterm = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				subterms.add(subterm);
			
				FOToken tokenInFun = tokens.get(pf.ixPos);
				pf.ixPos++;

				if(tokenInFun.type == Type.END_GROUP)
					break;

				if(tokenInFun.type != Type.COMMA)
					throw new FOConstructionException("Synthax error with function - expecting comma.");
			}
			
			pf.ixPos++;
			
			FOTerm termfun = new FOTermByRecursionImpl.FOTermFunction(fofun, subterms);
			return termfun;
		}
		else if(tokens.get(pf.ixPos).type == Type.START_GROUP && pf.ixPos < tokens.size() - 2 && tokens.get(pf.ixPos + 2).type == Type.INFIX_FUNCTION_OP)
		{
			FOToken tokInfixFunction = tokens.get(pf.ixPos + 2);
			pf.ixPos ++;
			
			List<FOTerm> subterms = new ArrayList<FOTerm>();
			FOTerm term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
			subterms.add(term);

			while(pf.ixPos < tokens.size())
			{
				FOToken tokInfixFound = tokens.get(pf.ixPos);
				if(tokInfixFound.type != Type.INFIX_FUNCTION_OP)
					break;

				if(!tokInfixFound.value.equals(tokInfixFunction.value))
					throw new FOConstructionException("Inconsistent infix function found: " + tokInfixFunction.value);				

				pf.ixPos++;
				
				term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				subterms.add(term);
			}
			
			if(tokens.get(pf.ixPos).type != Type.END_GROUP)
				throw new FOConstructionException("End paranthesis not found for infix function: " + tokInfixFunction.value);				
			pf.ixPos++;
			
			FOFunction fun = mapInfixFuns.get(tokInfixFunction.value);
			assert fun != null; //tokeniser should handle this.
			
			FOTerm termfun = new FOTermByRecursionImpl.FOTermFunction(fun, subterms);
			return termfun;
		}
		else
			throw new FOConstructionException("Synthax error parsing function.");				
	}
	
	List<FOToken> parseTokens(String strform, FOStructure structure,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases
			) throws FOConstructionException
	{
		Pattern nameExtractor =  Pattern.compile("^(" + ValidationRules.validName + ")"); 
		Pattern nameInfixOp =  Pattern.compile("^(" + ValidationRules.validInfixOp + ")"); 
		
		
		List<FOToken> listTokens = new ArrayList<FOToken>();
		int ixPos = 0;
		
		while(ixPos < strform.length())
		{
			// Trim whitespace at the start.
			if(strform.startsWith(" ", ixPos) || strform.startsWith("\t", ixPos))
			{
				ixPos++;
				continue;				
			}

			if(strform.startsWith("(", ixPos))
			{
				listTokens.add(new FOToken(FOToken.Type.START_GROUP, "("));
				ixPos++;
				continue;
			}
			
			if(strform.startsWith(")", ixPos))
			{
				listTokens.add(new FOToken(FOToken.Type.END_GROUP, ")"));
				ixPos++;
				continue;
			}
			
			if(strform.startsWith(",", ixPos))
			{
				listTokens.add(new FOToken(FOToken.Type.COMMA, ","));
				ixPos++;
				continue;
			}

			if(strform.startsWith("¬", ixPos))
			{
				listTokens.add(new FOToken(FOToken.Type.NEGATION, "¬"));
				ixPos++;
				continue;
			}

			if(strform.startsWith("|", ixPos) || strform.startsWith("&", ixPos))
			{
				listTokens.add(new FOToken(FOToken.Type.LOGICAL_OP, strform.substring(ixPos, ixPos + 1)));
				ixPos++;
				continue;
			}

			if(strform.startsWith("_", ixPos))
			{
				Matcher m = nameExtractor.matcher(strform.subSequence(ixPos + 1, strform.length()));
				if(!m.find())
					throw new FOConstructionException("Variable name can't be parsed: " + strform.substring(ixPos));
				assert m.groupCount() == 1;
				String name = m.group(1);
				listTokens.add(new FOToken(Type.VARIABLE, name));
				ixPos += (name.length() + 1);
				continue;
			}
			
			Matcher mInfix = nameInfixOp.matcher(strform.subSequence(ixPos, strform.length()));
			if(mInfix.find())
			{
				assert mInfix.groupCount() == 1;
				String name = mInfix.group(1);
				boolean infixed = false;
				if(mapInfixRels.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.INFIX_RELATION_OP, name));
					infixed = true;
				}
				else if(mapInfixFuns.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.INFIX_FUNCTION_OP, name));					
					infixed = true;
				}
				if(infixed)
				{
					ixPos += name.length();
					continue;					
				}
			}

			Matcher mName = nameExtractor.matcher(strform.subSequence(ixPos, strform.length()));
			if(mName.find())
			{
				assert mName.groupCount() == 1;
				boolean named = false;
				String name = mName.group(1);
				if(mapRels.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.RELATION, name));
					named = true;
				}
				else if(mapFuns.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.FUNCTION, name));
					named = true;
				}
				else if(name.equals("forall"))
				{
					listTokens.add(new FOToken(Type.SCOPE_COMMAND, name));
					named = true;
				}
				else if(mapConstants.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.CONSTANT, name));
					named = true;
				}
				else if(mapAliases.keySet().contains(name))
				{
					listTokens.add(new FOToken(Type.ALIAS, name));
					named = true;
				}
				if(named)
				{
					ixPos += name.length();
					continue;					
				}
			}
			
			throw new FOConstructionException("Can't parse: " + strform.substring(ixPos));
		}
		
		return listTokens;
	}
}

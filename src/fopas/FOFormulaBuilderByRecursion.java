package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	}
	
	static class PosForward
	{
		int ixPos;
		PosForward(int ixPos)
		{
			this.ixPos = ixPos;
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
	
	// Need to re-write this to:
	// 1) Second pass of creating terms.
	// 2) Third pass that looks at the depths of matching paranthesis and partitions formula recusion with that.
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
		
		FOFormula form = constructFormula(tokens, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
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
			boolean subNegation = false;
			if(tokens.get(ixStart).type == Type.START_GROUP || 
					tokens.get(ixStart).type == Type.NEGATION && tokens.get(ixStart + 1).type == Type.START_GROUP)
			{
				int paraStart = ixStart + 1;
				if(tokens.get(ixStart).type == Type.NEGATION)
				{
					subNegation = true;
					++paraStart;
				}
				
				int endPara = 1;
				for(int i = paraStart; i < tokens.size(); i++)
				{
					if(tokens.get(i).type == Type.END_GROUP)
					{
						endPara--;
						if(endPara == 0)
						{
							ixEnd = i;
							break;
						}
					}
					else if(tokens.get(i).type == Type.START_GROUP)
						endPara++;
				}					
			}
					
			if(ixEnd != -1)
			{
				FOToken compToken = buildSubformula(tokens.subList(ixStart + 1, ixEnd - 1), mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, subNegation);
				replaceTokens(tokens, ixStart, ixEnd, compToken);
			}
		}
		
		// By this point the everything between parantheses should have been converted to subformulas in the tokens (sub)list.
		// Now look for "|" operations.
		for(int ixStart = 0; ixStart < tokens.size(); ++ixStart)
		{
			for(int ixEnd = ixStart + 1; ixEnd < tokens.size(); ixEnd++)
			{
				if(tokens.get(ixStart).type == Type.LOGICAL_OP)
				{
					assert tokens.get(ixStart).value.equals("|");
					FOToken subformula = buildSubformula(tokens.subList(ixStart, ixEnd - 1), mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, false);
					replaceTokens(tokens, ixStart, ixEnd, subformula);
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
		
		// Now it's only a single formula possibly with composite tokens.
		FOFormula formula = constructFormula(tokens, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
		if(isNegated)
			return new FOToken(Type.COMP_SUBFORMULA, new FOFormulaByRecursionImpl.FOFormulaBROr(true, Arrays.asList(formula)));
		else
			return new FOToken(Type.COMP_SUBFORMULA, formula);
	}

	private void replaceTokens(List<FOToken> tokens, int ixStart, int ixEnd, FOToken subformula)
	{
		for(int i = ixStart; i < ixEnd + 1; ++i)
			tokens.remove(ixStart);
		tokens.add(ixStart, subformula);
	}
	
	FOFormula constructFormula(List<FOToken> tokens,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases
			) throws FOConstructionException
	{
		int ixToken = 0;
		
		boolean isNegated = false;
		if(tokens.get(ixToken).type == Type.NEGATION)
		{
			isNegated = true;
			ixToken++;
		}
		
		FOFormula form;
		
		if(tokens.get(ixToken + 1).type == Type.LOGICAL_OP)
		{
			FOToken tokLogOp = tokens.get(ixToken + 1);
			assert tokLogOp.value.equals("|");

			// At this point all subformulas should already have been created as composite tokens,
			// we just need to wrap them.
		}
		// forall formula
		else if(tokens.get(ixToken).type == Type.COMP_SCOPE)
		{
			FOToken tokScope = tokens.get(ixToken);
			ixToken++;
			
			// Like the above case, at this point all subformulas should already have been created as composite tokens,
			// let's wrap them.

			// Let's build the scope as a single formula first.
			//FOFormula scopeFormula = constructFormula(tokens.subList(ixToken, tokens.size()), mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
			
			FOVariable variable = tokScope.tokenScope.scopeVar;
			form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(isNegated, variable, scopeFormula);
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
			//TODO: This can't be negated.
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
		else if((tokens.get(pf.ixPos).type == Type.NEGATION && tokens.get(pf.ixPos + 1).type == Type.ALIAS)
				|| tokens.get(pf.ixPos).type == Type.ALIAS)
		{
			// This is a bit of a syntactic sugar. The following:
			// ¬(¬alias(x))
			// becomes:
			// alias(x)
			// isNegated is at the start of paras
			// aliasNegated captures the negation before the alias.
			// Alternative to this would've been using this:
			// ¬(alias(x))
			// This would've created an OR sentence with a single non-negated alias.
			// Really need to get rid of this very clumsy way of creating formulas,
			// need to get something that consumes paras and then relations.
			boolean aliasNegated = false;
			if(tokens.get(pf.ixPos).type == Type.NEGATION)
			{
				aliasNegated = true;
				pf.ixPos++;
			}
			
			FOToken tokAlias = tokens.get(pf.ixPos); 
			pf.ixPos++;

			if(tokens.get(pf.ixPos).type != Type.START_GROUP)
				throw new FOConstructionException("Synthax error with relation - expecting starting paranthesis.");
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
			
			form = new FOAliasByRecursionImpl.FOAliasBindingByRecursionImpl(
					tokAlias.value, isNegated ^ aliasNegated, (FOAliasByRecursionImpl) mapAliases.get(tokAlias.value), subterms);
		}
		else
			// TODO: Error messages not helpful, need to improve error reporting here.
			throw new FOConstructionException("Synthax error constructing a formula.");
		
		if(hasLastPara)
		{
			if(tokens.get(pf.ixPos).type != Type.END_GROUP)
				throw new FOConstructionException("Unmatching paranthesis found.");
			pf.ixPos++;			
		}
			
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

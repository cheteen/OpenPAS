package fopas;

import java.util.ArrayList;
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
			END_SENTENCE
		}
		Type type;
		String value;
		FOToken(Type type, String value)
		{
			this.type = type;
			this.value = value;
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
	
	void buildMaps(FOStructure structure,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants
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
	}
	
	// Need to re-write this to:
	// 1) Second pass of creating terms.
	// 2) Third pass that looks at the depths of matching paranthesis and partitions formula recusion with that.
	@Override
	public FOFormula buildFrom(String strform, FOStructure structure) throws FOConstructionException
	{
		Map<String, FORelation<FOElement>> mapRels = new HashMap<>();
		Map<String, FORelation<FOElement>> mapInfixRels = new HashMap<>();		
		Map<String, FOFunction> mapFuns = new HashMap<>();
		Map<String, FOFunction> mapInfixFuns = new HashMap<>();
		Map<String, FOConstant> mapConstants = new HashMap<>();
		
		buildMaps(structure, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
		
		List<FOToken> tokens = parseTokens(strform, structure, mapRels, mapInfixRels,
				mapFuns, mapInfixFuns, mapConstants);
		
		// This below is painfully ugly, need to rewrite this stuff.
		// Check if there are surrounding parentheses for the sentence, if not add them.
		int ixStart = 0;
		if(tokens.get(ixStart).type == Type.NEGATION)
			ixStart++;
		boolean needToAddParas = tokens.get(ixStart).type != Type.START_GROUP;
		if(tokens.get(ixStart).type == Type.START_GROUP)
		{
			// Find if this is a surrounding para.
			int endPara = 1; // if this drops to 0, then we need to add still
			for(int i = ixStart + 1; i < tokens.size() - 1; i++)
			{
				if(tokens.get(i).type == Type.END_GROUP)
					endPara--;
				else if(tokens.get(i).type == Type.START_GROUP)
					endPara++;
				if(endPara == 0)
				{
					needToAddParas = true;
					break;
				}
			}				
		}
		if(needToAddParas)
		{
			tokens.add(0, new FOToken(Type.START_GROUP, "("));
			tokens.add(new FOToken(Type.END_GROUP, ")"));			
		}
		
		//Add a special end of sentence here to mark the end - stops the code from having to check end of tokens.
		tokens.add(new FOToken(Type.END_SENTENCE, ""));
		
		PosForward pf = new PosForward(0);
		
		FOFormula form = constructFormula(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
		
		if(tokens.get(pf.ixPos).type != Type.END_SENTENCE)
			throw new FOConstructionException("Syntax error constructing the formula.");
		
		return form;
	}
	
	FOFormula constructFormula(List<FOToken> tokens, PosForward pf,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants
			) throws FOConstructionException
	{
		// Negated formula (short cut)
		boolean isNegated = false;
		if(tokens.get(pf.ixPos).type == Type.NEGATION)
		{
			isNegated = true;
			pf.ixPos++;
		}
		
		// Allow the top-level sentence to not have parentheses.
		if(tokens.get(pf.ixPos).type != Type.START_GROUP)
			throw new FOConstructionException("Missing starting paranthesis for formula.");
		pf.ixPos++;
		boolean hasLastPara = true;
		
		FOFormula form;
		
		// forall formula
		if(tokens.get(pf.ixPos).type == Type.SCOPE_COMMAND)
		{
			FOToken token = tokens.get(pf.ixPos);
			pf.ixPos++;
			assert token.value.equals("forall");
			
			if(tokens.get(pf.ixPos).type != Type.VARIABLE)
				throw new FOConstructionException("Expected variable not found for command scope.");
			FOToken scopeVariable = tokens.get(pf.ixPos); 
			pf.ixPos++;
			
			if(tokens.get(pf.ixPos).type != Type.END_GROUP)
				throw new FOConstructionException("Unmatching bracket found for command scope.");
			pf.ixPos++;
			
			FOFormula scopeFormula = constructFormula(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
			
			FOVariable variable = new FOVariableImpl(scopeVariable.value);
			form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(isNegated, variable, scopeFormula);
			
			// This is an ugly hack, but need this until a rewrite:
			hasLastPara = false; // forall doesn't need/consume last para because of its format
		}
		// OR of formulas
		else if(tokens.get(pf.ixPos).type == Type.NEGATION || tokens.get(pf.ixPos).type == Type.START_GROUP)
		{
			List<FOFormula> subFormulas = new ArrayList<FOFormula>();
			FOFormula subForm = constructFormula(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
			subFormulas.add(subForm);

			// Must find Or here.
			if((tokens.get(pf.ixPos).type != Type.LOGICAL_OP || !tokens.get(pf.ixPos).value.equals("|"))
					&& tokens.get(pf.ixPos).type != Type.END_GROUP // allow single subformula as OR
					)
				throw new FOConstructionException("Expected first logical op not found.");				
			
			while(pf.ixPos < tokens.size())
			{
				if(tokens.get(pf.ixPos).type != Type.LOGICAL_OP)
					break;
				
				if(!tokens.get(pf.ixPos).value.equals("|")) // should never happen.
					throw new FOConstructionException("Expected logical op not found.");				
				pf.ixPos++;

				subForm = constructFormula(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				subFormulas.add(subForm);				
			}
			form = new FOFormulaByRecursionImpl.FOFormulaBROr(isNegated, subFormulas);
		}
		// Relation formula (prefix)
		else if(tokens.get(pf.ixPos).type == Type.RELATION)
		{
			FOToken tokRelation = tokens.get(pf.ixPos); 
			pf.ixPos++;

			if(tokens.get(pf.ixPos).type != Type.START_GROUP)
				throw new FOConstructionException("Synthax error with relation - expecting starting paranthesis.");
			pf.ixPos++;

			List<FOTerm> terms = new ArrayList<FOTerm>();
			while(pf.ixPos < tokens.size())
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
			
			FORelation<FOElement> rel = mapRels.get(tokRelation.value);
			assert rel != null; // tokeniser handles the existence of the relation.
			
			form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(isNegated, rel, terms);
		}
		// Relation formula (infix)
		else if(tokens.get(pf.ixPos + 1).type == Type.INFIX_RELATION_OP)
		{
			FOToken tokInfixRelation = tokens.get(pf.ixPos + 1);
			
			List<FOTerm> terms = new ArrayList<FOTerm>();
			FOTerm term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
			terms.add(term);

			while(pf.ixPos < tokens.size())
			{
				FOToken tokInfixFound = tokens.get(pf.ixPos);
				if(tokInfixFound.type != Type.INFIX_RELATION_OP)
					break;
				
				if(!tokInfixFound.value.equals(tokInfixRelation.value))
					throw new FOConstructionException("Inconsistent infix relation found: " + tokInfixRelation.value);				
				
				pf.ixPos++;
				
				term = constructTerm(tokens, pf, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				terms.add(term);
			}
			
			FORelation<FOElement> rel = mapInfixRels.get(tokInfixRelation.value);
			assert rel != null; //tokeniser should handle this.

			form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(isNegated, rel, terms);			
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
			Map<String, FOConstant> mapConstants
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

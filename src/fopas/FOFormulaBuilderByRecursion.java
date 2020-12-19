package fopas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fopas.FOFormulaBuilderByRecursion.FOToken.Type;
import fopas.FOFormulaByRecursionImpl.FOFormulaBRForAll;
import fopas.FOFormulaByRecursionImpl.FOFormulaBRForAll.ForAllSubtype;
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

/**
 * This builder goes through several steps.
 * 
 * High level transformation goes like this: string -> simple tokens -> standard function/rel folds & scope tokens & some negations ->
 *                                               parseTokens   buildParentheses
 *                                               
 *     -> a single negation at start & scope tokens & a standard or infix function/rel fold -> a single, composite, term or formula token
 * buildParts                                                                      buildSingleExpression
 * 
 * Folds are an intermediate representation that makes it easier to build expressions (terms or formulas) by handing out the token
 * parsing logic to buildParts function which knows how to parse _all_ infix operators using the common concept of precendence. There
 * can be negations left amongst fold tokens when they're not negating parentheses.
 * 
 * 1) Tokeniser goes over the input string and using regular expressions identifies all various tokens. That variables start with "_"
 * is a key differentiator here since we don't use variable declarations. All other things are declared, so the tokeniser
 * checks them against the various declared things (function, infix function, relation, ...) to make sure they're known. We 
 * recognise and support both infix and standard function calls, as well as infix and standard relations and infix logical operators.
 * 
 * 2) Once tokens are recognised, we go and try to build all parentheses recursively. Special treatments is made for argument lists and
 * scope operators. Arg list is represented as a fold token. The rest when parsed depth-first, are sent buildParts.
 * This recognises and swallows a preceding negation as a parameter. buildParts builds from the leaf backwards each time returning
 * a token that contains a composite token that has a term or a formula in it. This way lists of tokens are converted into composite
 * token, and the final built formula thatis returned is extracted out of the last remaining composite token. Of course the top level
 * sentence is also built whether it's surrounded by parentheses or not.
 * 
 * buildParentheses processes and replaces each pair of parentheses, and the moves onto building parts in the transformed list of tokens
 * using buildParts which by this point contains no parenthesis and expressions possibly separated by infix operators.
 * 
 * 3) buildParts expects to find a series of tokens possibly with infix operators between them (logica, function and relation). It parses
 * all declared infix relation and infix functions as well as implicit infix logical operators ("->", "|", "&") dept-first. At the leaf it
 * expects to find a single expression. This will either be a single already folded token for an expression, or an infix operator
 * separated expression that's ready to be folded. After folding, this will become a valid formula or a valid term.
 * It starts with trying to construct a formula, if that fails it tries to build the fold as term.
 * 
 * 4) constructFormula expects to find the tokens that make up a single formula. It's helped primarily by having everything that matters
 * into folds by this point, so there's little complexity. constructFormula will return null if it fails which will happen
 * if it finds a term.
 * 
 * 5) constructTerm is called when we definitely expect to find a single term in the list of tokens. This construction isn't recursive
 * at this point. The construction of terms in terms is handle by the parenthesis construction where a term fold can be made of
 * composite tokens containing terms.
 *
 * @author Burak Cetin
 */
public class FOFormulaBuilderByRecursion implements FOFormulaBuilder
{
	final FOLanguage mLang = new FOLanguage();
	protected static class FOTokenScope
	{
		final FOVariable scopeVar;
		final boolean isNegated;
		FOTokenScope(FOVariable scopeVar, boolean isNegated)
		{
			this.scopeVar = scopeVar;
			this.isNegated = isNegated;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isNegated ? 1231 : 1237);
			result = prime * result + ((scopeVar == null) ? 0 : scopeVar.hashCode());
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
			FOTokenScope other = (FOTokenScope) obj;
			if (isNegated != other.isNegated)
				return false;
			if (scopeVar == null) {
				if (other.scopeVar != null)
					return false;
			} else if (!scopeVar.equals(other.scopeVar))
				return false;
			return true;
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
			COMP_SCOPE,
			COMP_TERM,
			FOLD
		}
		final Type type;
		String value;
		FOFormula subformula;
		FOTokenScope tokenScope;
		FOTerm term;
		FOToken foldAnchor;
		List<FOToken> args;
		FOToken(Type type, String value)
		{
			this.type = type;
			this.value = value;
		}
		FOToken(FOFormula subformula)
		{
			this.type = Type.COMP_SUBFORMULA;
			this.subformula = subformula;
		}
		FOToken(FOTerm term)
		{
			this.type = Type.COMP_TERM;
			this.term = term;
		}
		FOToken(FOTokenScope tokenScope, String value)
		{
			this.type = Type.COMP_SCOPE;
			this.tokenScope = tokenScope;
			this.value = value;
		}
		FOToken(FOToken foldAnchor, List<FOToken> args)
		{
			this.type = Type.FOLD;
			this.foldAnchor = foldAnchor;
			this.args = args;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((subformula == null) ? 0 : subformula.hashCode());
			result = prime * result + ((term == null) ? 0 : term.hashCode());
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
			if (term == null) {
				if (other.term != null)
					return false;
			} else if (!term.equals(other.term))
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
	
	// This builds a canned formula that should exist in all structures and is always true.
	public FOFormula buildTautology()
	{
		FOVariable fox = new FOVariableImpl("x");
		FOTerm termX = new FOTermByRecursionImpl.FOTermVariable(fox);
		FOFormula formEq = new FOFormulaBRRelation(false, new FORelationImpl.FORelationImplEquals(), Arrays.asList(termX, termX));
		FOFormula formAll = new FOFormulaBRForAll(false, fox, formEq);
		return formAll;
	}
	
	// This builds a canned formula that should exist in all structures and is a contradiction.
	public FOFormula buildContradiction()
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
			mapFuns.put(fun.getName(), ((FOFunctionImpl) fun).inversePresentInfix());
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
	public FOAlias buildAlias(FOStructure structure, String name, List<FOVariable> args, String strform) throws FOConstructionException
	{
		return (FOAlias) buildFormula(strform, structure, name, args);
	}

	@Override
	public FOFormula buildFormula(String strform, FOStructure structure) throws FOConstructionException
	{
		return buildFormula(strform, structure, null, null);
	}
	
	public FOFormula buildFormula(String strform, FOStructure structure, String aliasName, List<FOVariable> aliasArgs) throws FOConstructionException
	{
		Map<String, FORelation<FOElement>> mapRels = new HashMap<>();
		Map<String, FORelation<FOElement>> mapInfixRels = new HashMap<>();		
		Map<String, FOFunction> mapFuns = new HashMap<>();
		Map<String, FOFunction> mapInfixFuns = new HashMap<>();
		Map<String, FOConstant> mapConstants = new HashMap<>();
		Map<String, FOFormula> mapAliases = new HashMap<>();
		
		buildMaps(structure, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
		// Process ops in decreasing precedence, e.g. a & b | c <-> (a & b) | c
		TreeMap<Integer, FOToken> anchors = new TreeMap<>();
		anchors.put(mLang.getPrecedenceAnd(), new FOToken(Type.LOGICAL_OP, mLang.getAnd()));
		anchors.put(mLang.getPrecedenceOr(), new FOToken(Type.LOGICAL_OP, mLang.getOr()));
		anchors.put(mLang.getPrecedenceImp(), new FOToken(Type.LOGICAL_OP, mLang.getImp()));
		for(FORelation<? extends FOElement> inrel : mapInfixRels.values())
			anchors.put(inrel.getPrecedence(), new FOToken(Type.INFIX_RELATION_OP, inrel.getInfix()));
		for(FOFunction infun : mapInfixFuns.values())
			anchors.put(infun.getPrecedence(), new FOToken(Type.INFIX_FUNCTION_OP, infun.getInfix()));
		List<FOToken> infixOps = new ArrayList<>(anchors.values());
		
		FOAliasByRecursionImpl formAlias = null;
		if(aliasName != null)
		{
			formAlias = new FOAliasByRecursionImpl(aliasName, aliasArgs); 
			mapAliases.put(aliasName, formAlias);			
		}
		
		List<FOToken> tokens = parseTokens(strform, structure, mapRels, mapInfixRels,
				mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
		FOToken finalToken = buildParentheses(tokens, false, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, infixOps);
		if(finalToken == null)
			throw new FOConstructionException("Did not find a valid formula to build.");
		
		FOFormula form = finalToken.subformula;
		form.checkFormula(structure); // don't allow invalid formulas to be created.
		
		if(formAlias != null)
		{
			formAlias.setScopeFormula(form);
			return formAlias;
		}
		else
			return form;
	}

	/**
	 * Build the subformula that's between parentheses.
	 */
	FOToken buildParentheses(List<FOToken> tokens,
			boolean isNegated,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases,
			List<FOToken> anchors
			) throws FOConstructionException
	{
		for(int ixStart = 0; ixStart < tokens.size(); ++ixStart)
		{
			int ixEnd = -1;
			int paraInStart = ixStart + 1;
			boolean subNegation = false;
			FOToken groupType = null;
			if(tokens.get(ixStart).type == Type.START_GROUP || 
					tokens.get(ixStart).type == Type.NEGATION && tokens.get(ixStart + 1).type == Type.START_GROUP)
			{
				if(ixStart > 0)
				{
					FOToken tokPre = tokens.get(ixStart - 1);
					if(tokPre.type == Type.FUNCTION
							|| tokPre.type == Type.RELATION
							|| tokPre.type == Type.ALIAS)
					{
						groupType = tokPre;
					}
				}
				
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
				// This pair of parentheses has to contain a formula or term.
				if(groupType == null)
				{
					FOToken compToken = buildParentheses(new ArrayList<>(tokens.subList(paraInStart, ixEnd)),
							subNegation, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, anchors);					
					assert compToken != null; // this is either a formula or a term
					replaceTokens(tokens, ixStart, ixEnd + 1, compToken);
				}
				else // This is some kind of arg group, let's parse args 
				{
					List<List<FOToken>> listArgs = splitTokens(
							new ArrayList<>(tokens.subList(paraInStart, ixEnd)), new FOToken(Type.COMMA, ","));
					List<FOToken> listFolding = new ArrayList<>();
					if(listArgs != null)
					{
						for(List<FOToken> arg : listArgs)
						{
							FOToken tokArg = buildParentheses(arg, isNegated, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, anchors);
							if(tokArg == null)
								throw new FOConstructionException("Error constructing arg.");
							if(tokArg.type != Type.COMP_TERM)
								throw new FOConstructionException("Term not found constructing arg.");
							listFolding.add(tokArg);
						}						
					}
					else
					{
						FOToken tokArg = buildParentheses(new ArrayList<>(tokens.subList(paraInStart, ixEnd)),
								isNegated, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases, anchors);						
						if(tokArg == null)
							throw new FOConstructionException("Error constructing arg.");
						listFolding.add(tokArg);
					}
					FOToken fold = new FOToken(groupType, listFolding);
					replaceTokens(tokens, ixStart - 1, ixEnd + 1, fold);
				}
			}
		}
		
		// Let's first get the command scope out of the way.
		if(tokens.get(0).type == Type.SCOPE_COMMAND)
		{
			int ixTok = 0;
			FOToken token = tokens.get(ixTok);
			ixTok++;
			
			if(!token.value.equals(mLang.getForAll()) && !token.value.equals(mLang.getExists()))
				throw new FOConstructionException("Unexpected scope command found: " + token.value);
			
			if(tokens.get(ixTok).type != Type.VARIABLE)
				throw new FOConstructionException("Expected variable not found for command scope.");
			FOToken scopeTokenVariable = tokens.get(ixTok); 
			ixTok++;
			
			if(ixTok != tokens.size())
				throw new FOConstructionException("Unexpected token found in command scope.");
			
			FOVariable variable = new FOVariableImpl(scopeTokenVariable.value);
			return new FOToken(new FOTokenScope(variable, isNegated), token.value);
		}
		
		// By this point we are inside any parentheses, so we can start examining infix ops.
		return buildParts(tokens, isNegated, anchors, 0,
				mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
	}

	/**
	 * Once in parenthesis, build parts created by using logical ops.
	 */
	private FOToken buildParts(List<FOToken> tokens, boolean isNegated, List<FOToken> anchors, int ixAnchor,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels, Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns, Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases) throws FOConstructionException
	{
		List<FOToken> listPartTokens;
		if(ixAnchor < anchors.size())
		{
			List<List<FOToken>> splitted = splitTokens(tokens, anchors.get(ixAnchor));
			if(splitted == null) // Can't parse with current anchor, go forward.
			{
				// Since we can't make use of isNegated here, pass it on.
				return buildParts(tokens, isNegated, anchors, ixAnchor + 1,
						mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
			}
			else
			{
				List<FOToken> listFold = new ArrayList<>();
				// At branch, need to process leaves first.
				for(List<FOToken> part : splitted)
				{
					FOToken tokPart = buildParts(part, false, anchors, ixAnchor + 1,
							mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
					if(tokPart == null)
						throw new FOConstructionException("Failed to construct formula part.");
					listFold.add(tokPart);
				}
				FOToken tokFold = new FOToken(anchors.get(ixAnchor), listFold);
				listPartTokens = new ArrayList<>(1);
				listPartTokens.add(tokFold);
			}
		}
		else // if we are past the leaf, then the tokens we have need to be ready as is.
			listPartTokens = tokens;
		
		// builtTokens should contain a single formula for a part with all parts from this anchor down (in precedence) built already. 
		return buildSingleExpression(listPartTokens, isNegated, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
	}
	
	private List<List<FOToken>> splitTokens(List<FOToken> tokens, FOToken anchor) throws FOConstructionException
	{
		List<List<FOToken>> splitted = null;
		int ixStart = 0;
		
		for(int ixEnd = ixStart; ixEnd < tokens.size() + 1; ++ixEnd)
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
				
				splitted.add(new ArrayList<>(tokens.subList(ixStart, ixEnd)));
				ixStart = ixEnd + 1;
			}
		}
		
		return splitted;
	}

	private void replaceTokens(List<FOToken> tokens, int ixStart, int ixEnd, FOToken subformula)
	{
		tokens.subList(ixStart, ixEnd).clear();
		tokens.add(ixStart, subformula);
	}

	/**
	 * This will create a token that contains a formula or a term.
	 * @param tokens The tokens should contain the description of a single formula or a term.
	 * @param isNegated
	 * @param mapRels
	 * @param mapInfixRels
	 * @param mapFuns
	 * @param mapInfixFuns
	 * @param mapConstants
	 * @param mapAliases
	 * @return
	 * @throws FOConstructionException
	 */
	private FOToken buildSingleExpression(List<FOToken> tokens, boolean isNegated,
			Map<String, FORelation<FOElement>> mapRels, Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns, Map<String, FOFunction> mapInfixFuns, Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases) throws FOConstructionException
	{
		FOFormula formula;
		if(tokens.size() == 1 && tokens.get(0).type == Type.COMP_SUBFORMULA)
			// There was surrounding parentheses, so the whole thing got compressed to a single subformula.
			formula = tokens.get(0).subformula;
		else
		{
			// It's only a single formula possibly with composite tokens.
			formula = constructFormula(tokens, isNegated, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
			if(formula == null)
			{
				FOTerm term = constructTerm(tokens, new PosForward(0), mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants);
				return new FOToken(term); // didn't find a formula here but found a term.
			}
			isNegated = false; // We've embedded this negation in the subformula, so can remove this.
		}
		
		if(isNegated)
			return new FOToken(new FOFormulaByRecursionImpl.FOFormulaBROr(true, Arrays.asList(formula)));
		else
			return new FOToken(formula);
	}

	/**
	 * Construct a single formula from possibly composite tokens.
	 */
	FOFormula constructFormula(List<FOToken> tokens,
			boolean isExternallyNegated,
			Map<String, FORelation<FOElement>> mapRels,
			Map<String, FORelation<FOElement>> mapInfixRels,
			Map<String, FOFunction> mapFuns,
			Map<String, FOFunction> mapInfixFuns,
			Map<String, FOConstant> mapConstants,
			Map<String, FOFormula> mapAliases
			) throws FOConstructionException
	{
		int ixToken = 0;
		
		boolean hasNegation = false;
		if(tokens.get(ixToken).type == Type.NEGATION)
		{
			hasNegation = true;
			ixToken++;
		}
		
		// This serves to collapse unnecessary parentheses.
		boolean isNegated = hasNegation ^ isExternallyNegated;
		
		FOFormula form;
				
		if(tokens.get(ixToken).type == Type.FOLD && tokens.get(ixToken).foldAnchor.type == Type.LOGICAL_OP)
		{
			// This option can't work with infix operators.
			if(hasNegation)
				throw new FOConstructionException("Unexpected negation token found.");
			
			FOToken tokFold = tokens.get(ixToken);
			FOToken tokAnchor = tokFold.foldAnchor;
			verifyFoldHas(tokFold, Type.COMP_SUBFORMULA, "Expected formula not found constructing logical op.");
			ixToken++;

			if(tokAnchor.value.equals(mLang.getOr()))
			{
				List<FOFormula> listFormulas = new ArrayList<>(tokFold.args.size());
				for(FOToken tok : tokFold.args)
					listFormulas.add(tok.subformula);
				form = new FOFormulaByRecursionImpl.FOFormulaBROr(isNegated, listFormulas);
			}
			else if(tokAnchor.value.equals(mLang.getAnd()))
			{
				// Use this formulation for creating "&": a & b := ¬(¬a | ¬b)
				List<FOFormula> listNegatedFormulas = new ArrayList<>(tokFold.args.size());
				for(FOToken tok : tokFold.args)
					listNegatedFormulas.add(tok.subformula.negate());
				form = new FOFormulaByRecursionImpl.FOFormulaBROr(!isNegated, listNegatedFormulas, FOFormulaByRecursionImpl.FOFormulaBROr.OrSubType.AND);
			}
			else if(tokAnchor.value.equals(mLang.getImp()))
			{
				// We use: a -> b :- ¬a | b
				if(tokFold.args.size() > 2)
					throw new FOConstructionException("Implication accepts only two operands: implicate and implicant.");
				
				List<FOFormula> impPair = new ArrayList<>(2);
				impPair.add(tokFold.args.get(0).subformula.negate());
				impPair.add(tokFold.args.get(1).subformula);

				form = new FOFormulaByRecursionImpl.FOFormulaBROr(isNegated, impPair, FOFormulaByRecursionImpl.FOFormulaBROr.OrSubType.IMP);
			}
			else
				throw new FOConstructionException("Unexpected logical op found: " + tokAnchor.value); // this should never happen.
		}
		// forall formula
		else if(tokens.get(ixToken).type == Type.COMP_SCOPE)
		{
			assert !hasNegation; // negation preceding the scope is handled within the token.
			
			// Like the above case, at this point all subformulas should already have been created as composite tokens,
			// let's wrap them.

			List<FOToken> scopes = new ArrayList<>();
			scopes.add(tokens.get(ixToken));
			ixToken++;

			// Consume the rest of the scopes if there are any.
			for(FOToken tokInScope = tokens.get(ixToken); tokInScope.type == Type.COMP_SCOPE; tokInScope = tokens.get(++ixToken))
				scopes.add(tokInScope);
			
			FOToken tokInScope = tokens.get(ixToken);
			if(tokInScope.type != Type.COMP_SUBFORMULA)
				throw new FOConstructionException("Expected scoped formula not found: " + tokInScope.value);
			ixToken++;
			
			// We apply the outer negation to the first scope only, and need to construct this
			// from the end back to the front.
			FOFormula subForm = tokInScope.subformula;
			for(int i = scopes.size() - 1; i >= 0; i--)
			{
				FOToken tokScope = scopes.get(i);
				FOTokenScope tokenScope = tokScope.tokenScope;
				ForAllSubtype subtype;
				if(tokScope.value.equals(mLang.getForAll()))
					subtype = ForAllSubtype.FOR_ALL;
				else if(tokScope.value.equals(mLang.getExists()))
				{
					subtype = ForAllSubtype.EXISTS;
					subForm = subForm.negate();
				}
				else
				{
					assert false; // should've been stopp at the tokeniser
					throw new FOConstructionException("Unexpected scope command found.");
				}
				
				FOVariable variable = tokenScope.scopeVar;
				subForm = new FOFormulaByRecursionImpl.FOFormulaBRForAll(
						(i == 0 & isNegated) ^ tokenScope.isNegated ^ subtype == ForAllSubtype.EXISTS, variable, subForm, subtype);				
			}
			
			// The last subForm after a possible succession of scopes is the final formula.
			form = subForm;
		}
		else if(tokens.get(ixToken).type == Type.FOLD &&
				(
						tokens.get(ixToken).foldAnchor.type == Type.INFIX_RELATION_OP ||
						tokens.get(ixToken).foldAnchor.type == Type.RELATION
				))
		{
			assert !hasNegation || tokens.get(ixToken).foldAnchor.type == Type.RELATION;  

			FOToken tokFold = tokens.get(ixToken);
			FOToken tokAnchor = tokFold.foldAnchor;
			verifyFoldHas(tokFold, Type.COMP_TERM, "Expected term not found constructing relation.");
			ixToken++;

			FORelation<FOElement> rel = mapInfixRels.get(tokAnchor.value);
			assert rel != null; //tokeniser should handle this.

			List<FOTerm> listTerms = new ArrayList<>(tokFold.args.size());
			for(FOToken tok : tokFold.args)
				listTerms.add(tok.term);

			form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(isNegated, rel, listTerms);			
		}
		else if(tokens.get(ixToken).type == Type.FOLD && tokens.get(ixToken).foldAnchor.type == Type.ALIAS)
		{
			FOToken tokFold = tokens.get(ixToken);
			FOToken tokAnchor = tokFold.foldAnchor;
			verifyFoldHas(tokFold, Type.COMP_TERM, "Expected term not found constructing relation.");
			ixToken++;

			List<FOTerm> listTerms = new ArrayList<>(tokFold.args.size());
			for(FOToken tok : tokFold.args)
				listTerms.add(tok.term);

			form = new FOAliasByRecursionImpl.FOAliasBindingByRecursionImpl(
					isNegated, tokAnchor.value, (FOAliasByRecursionImpl) mapAliases.get(tokAnchor.value), listTerms);
		}
		else
			return null; // This used to throw exception, but now refuses to create a formula instead.
		
		if(ixToken != tokens.size())
			throw new FOConstructionException("Unexpected token found at the end.");
		
		return form;
	}

	private void verifyFoldHas(FOToken tokFold, FOToken.Type type, String err) throws FOConstructionException
	{
		for(FOToken tok : tokFold.args)
			if(tok.type != type)
				throw new FOConstructionException(err);
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
		if(tokens.size() == 1 && token.type == Type.COMP_TERM)
			return token.term;
		else if(token.type == Type.VARIABLE)
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
		else if(token.type == Type.FOLD &&
				(
					token.foldAnchor.type == Type.INFIX_FUNCTION_OP ||
					token.foldAnchor.type == Type.FUNCTION
				))
		{
			FOToken tokFold = token;
			FOToken tokAnchor = tokFold.foldAnchor;
			verifyFoldHas(tokFold, Type.COMP_TERM, "Expected term not found constructing function.");
			
			FOFunction fofun;
			if(tokAnchor.type == Type.FUNCTION)
				fofun = mapFuns.get(tokAnchor.value);
			else
				fofun = mapInfixFuns.get(tokAnchor.value);				
			assert fofun != null; //tokeniser should handle this.
			pf.ixPos++;
			
			List<FOTerm> listTerms = new ArrayList<>(tokFold.args.size());
			for(FOToken tok : tokFold.args)
				listTerms.add(tok.term);

			FOTerm termfun = new FOTermByRecursionImpl.FOTermFunction(fofun, listTerms);
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
		
		List<String> listOperators = Arrays.asList(
				mLang.getOr(),
				mLang.getAnd(),
				mLang.getImp()
				);
		
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

			boolean breakLoopAfterLogicalOp = false;
			for(String operator : listOperators)
			{
				if(strform.startsWith(operator, ixPos))
				{
					listTokens.add(new FOToken(FOToken.Type.LOGICAL_OP, strform.substring(ixPos, ixPos + operator.length())));
					ixPos += operator.length();
					breakLoopAfterLogicalOp = true;
					break;
				}
				if(breakLoopAfterLogicalOp)
					break;
			}
			if(breakLoopAfterLogicalOp)
				continue;

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
				else if(name.equals(mLang.getForAll()) || name.equals(mLang.getExists()))
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

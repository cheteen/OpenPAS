package fopas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fopas.FOFormulaBuilderByRecursion.FOToken.Type;
import fopas.FOFormulaByRecursionImpl.FormulaType;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOFormulaBuilder;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOStructure;
import fopas.basics.ValidationRules;

public class FOFormulaBuilderByRecursion implements FOFormulaBuilder
{
	@Override
	public FOFormula buildFrom(String strform, FOStructure structure) throws FOConstructionException
	{
		return null;
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
			CONSTANT
		}
		Type type;
		String value;
		FOToken(Type type, String value)
		{
			this.type = type;
			this.value = value;
		}
	}
	
	List<FOToken> parseTokens(String strform, FOStructure structure) throws FOConstructionException
	{
		Pattern nameExtractor =  Pattern.compile("^(" + ValidationRules.validName + ")"); 
		Pattern nameInfixOp =  Pattern.compile("^(" + ValidationRules.validInfixOp + ")"); 
		
		Map<String, FORelation<FOElement>> mapRels = new HashMap<>();
		Map<String, FORelation<FOElement>> mapInfixRels = new HashMap<>();
		for(FORelation<FOElement> rel : structure.getRelations())
		{
			if(rel.getInfix() != null)
				mapInfixRels.put(rel.getInfix(), rel);
			mapRels.put(rel.getName(), rel);			
		}
		
		Map<String, FOFunction> mapFuns = new HashMap<>();
		Map<String, FOFunction> mapInfixFuns = new HashMap<>();
		for(FOFunction fun : structure.getFunctions())
		{
			if(fun.getInfix() != null)
				mapInfixFuns.put(fun.getInfix(), fun);			
			mapFuns.put(fun.getName(), fun);
		}
		
		Map<String, FOConstant> mapConstants = new HashMap<>();
		for(FOConstant foconst : structure.getConstants())
		{
			mapConstants.put(foconst.getName(), foconst);
		}
		
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

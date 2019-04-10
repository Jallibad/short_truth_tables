package logic;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logic.malformedexpression.InvalidArgumentsException;

public class TruthAssignment
{	
	public List<Literal> columns = new ArrayList<>();
	public List<List<Boolean>> table = new ArrayList<>();
	
	public TruthAssignment(Expression exp)
	{
		columns = new ArrayList<>(exp.getVariables());
		Map<Literal, Boolean> settings = new HashMap<>();
		for
		(
			// Start at 2^n-1
			BigInteger variableSettings = BigInteger.ONE.shiftLeft(columns.size()).subtract(BigInteger.ONE);
			// While >= 0	
			variableSettings.signum() != -1;
			// Decrement by one each time
			variableSettings = variableSettings.subtract(BigInteger.ONE)
		) 
		{
			List<Boolean> currSettings = new ArrayList<>();
			for (int i=0; i<columns.size(); ++i)
			{
				boolean currSetting = variableSettings.testBit(i);
				currSettings.add(currSetting);
				settings.put(columns.get(i), currSetting);
			}
			currSettings.add(exp.evaluate(settings));
			table.add(currSettings);
		}
	}
	
	public TruthAssignment(boolean[][] truthTable)
	{
		for (int y=0; y<truthTable[0].length; ++y)
		{
			List<Boolean> partial = new ArrayList<>();
			for (int x=0; x<truthTable.length; ++x)
				partial.add(truthTable[x][y]);
			table.add(partial);
		}
		
		char currLiteral = 'A';
		for (int i=0; i<truthTable[0].length-1; ++i, ++currLiteral)
			try
			{
				columns.add(new Literal(Character.toString(currLiteral)));
			}
			catch (InvalidArgumentsException e)
			{
				// If we've run out of characters to make new literals from
				throw new Error();
			}
	}
	
	public String toString()
	{
		StringBuilder ans = new StringBuilder();
		
		for (Expression e : columns)
			ans.append(e.toString()+"|");
		ans.append("\n");
		
		for (int x=0; x<table.size(); ++x)
		{
			for (int y=0; y<table.get(x).size(); ++y)
				ans.append((table.get(x).get(y) ? 'T' : 'F')+"|");
			ans.append('\n');
		}
		
		return ans.toString();
	}
}
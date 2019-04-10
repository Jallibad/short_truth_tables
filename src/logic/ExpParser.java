package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import logic.malformedexpression.InvalidArgumentsException;
import logic.malformedexpression.MalformedExpressionError;
import logic.malformedexpression.MalformedExpressionException;
import logic.malformedexpression.NotAnOperatorException;
import logic.malformedexpression.UnmatchedParenthesesException;

public final class ExpParser
{
	private static final Logger LOGGER = Logger.getLogger(ExpParser.class.getName());
	
	/**
	 * Private constructor to prevent instantiation
	 */
	private ExpParser()
	{
		
	}
	
	/**
	 * Wrapper function that upcasts the MalformedExpressionException from ExpParser::parse to an unchecked exception. 
	 * @param exp the String to convert to an expression
	 * @return An Expression object that is equivalent to the specified expression
	 */
	public static Expression parseUnsafe(String exp)
	{
		try
		{
			return parse(exp);
		}
		catch (MalformedExpressionException e)
		{
			throw new MalformedExpressionError(e.getMessage());
		}
	}
	
	/**
	 * Parses text for an expression.  Infix and prefix forms are both supported, depending on the symbol position for
	 * each operator.  
	 * @param exp the String to convert to an Expression
	 * @return an Expression object that is equivalent to the specified expression
	 */
	public static Expression parse(String exp) throws MalformedExpressionException
	{	
		StringBuilder ans = new StringBuilder(exp);
		checkMatchingParentheses(ans);
		convertOperatorsToEnglish(ans);
		removeExtraSpaces(ans);
		if (exp.length() == 0)
			throw new MalformedExpressionException("The input is empty");
		return readTerm(ans);
	}
	
	/**
	 * Checks if all of the parentheses in the given expression are matching.
	 * @param exp the expression to check
	 * @throws UnmatchedParenthesesException if at any point the expression closes an unopened parenthesis
	 * or if the total number open and closed are not the same.
	 */
	private static void checkMatchingParentheses(StringBuilder exp) throws UnmatchedParenthesesException
	{
		// Start on the top level of the expression, an open parenthesis means going down a level.
		int levelsDeep = 0;
		
		for (int i=0; i<exp.length(); ++i)
			if (exp.charAt(i) == '(')
				levelsDeep++;
			// Throw an error if there are too many closing parentheses.
			else if (exp.charAt(i) == ')' && --levelsDeep < 0)
				throw new UnmatchedParenthesesException(exp.toString(), i);
		
		// If not on the top level after the loop throw an error.
		if (levelsDeep != 0)
			throw new UnmatchedParenthesesException(exp.toString(), exp.length());
	}
	
	/**
	 * Replaces all operator symbols such as "¬" to the corresponding operator name such as "NEG".
	 * Puts an extra space around each symbol to avoid situations such as "A∧B"->"ANEGB".
	 * @param exp the expression in which to convert all operators
	 * @throws NotAnOperatorException if there are any non alphanumeric characters remaining after the replace
	 */
	private static void convertOperatorsToEnglish(StringBuilder exp) throws NotAnOperatorException
	{
		// I don't know a better way to replaceAll on multiple different patterns for a StringBuilder
		String ans = exp.toString();
		for (Operator op : Operator.values())
			ans = ans.replaceAll(op.displayText, " "+op.name()+" ");
		// Check if the answer contains anything but letters, whitespace, or parentheses.
		for (int i=0; i<ans.length(); ++i)
			if (ans.charAt(i) != '(' && ans.charAt(i) != ')' && ans.charAt(i) != ' ' && !Character.isAlphabetic(ans.charAt(i)))
				throw new NotAnOperatorException(String.format
				(
						"The character '%c' at index %d is not a valid operator",
						ans.charAt(i),
						i
				));
		
		// Clear the original StringBuilder and insert the new string.
		//I don't know if there's a better way to do this with StringBuilder.
		exp.setLength(0);
		exp.append(ans);
		LOGGER.fine(exp::toString);
	}
	
	/**
	 * Removes leading and trailing, as well as repeated whitespace
	 * @param exp the expression to trim
	 */
	private static void removeExtraSpaces(StringBuilder exp)
	{
		// Apparently Java 9, but not 8, has support for matching a StringBuilder directly
		StringBuffer buffer = new StringBuffer();
		
		// Matches whitespace at the start or end of the string, repeated whitespace,
		// whitespace after a '(', and whitespace before a ')'.
		Matcher trimMatcher = Pattern.compile("^\\s+|\\s+$|\\s+(?=\\s)|(?<=\\()\\s+|\\s+(?=\\))").matcher(exp);
		while (trimMatcher.find())
			trimMatcher.appendReplacement(buffer, "");
		trimMatcher.appendTail(buffer);
		
		// Set the input to the result.
		exp.setLength(0);
		exp.append(buffer);
	}
	
	/**
	 * Reads a single term from the given expression, removing the characters from the passed expression.
	 * @param exp a StringBuilder containing an expression to parse
	 * @return an expression representing the next term of the argument
	 * @throws MalformedExpressionException if the next term is not syntactically valid
	 */
	private static Expression readTerm(StringBuilder exp) throws MalformedExpressionException
	{
		// Find the matching closing parenthesis
		int closeParen = findNextParen(exp, 0);
		
		// Unwrap any surrounding parentheses
		if (exp.charAt(0) == '(' && closeParen == exp.length()-1)
		{	
			// Find the number of parentheses wrapped around the term
			int numWrapped = 0;
			while (exp.charAt(numWrapped) == '(' && closeParen-findNextParen(exp, numWrapped) == numWrapped)
				numWrapped++;
			
			// Read the following expression after unwrapping it from the possibly nested parentheses 
			Expression term = readTerm(new StringBuilder(exp.substring(numWrapped, closeParen-numWrapped+1)));
			
			// Delete the entire term including the parentheses and the following space
			exp.delete(0, closeParen+2);
			
			return term;
		}
		// Handle the special case of an unwrapped negation
		else if (exp.length() > 4 && exp.substring(0, 4).equals("NEG "))
		{
			exp.delete(0, 4); // Remove the preceding "NEG "
			return new Function(Operator.NEG, readTerm(exp));
		}
		// Otherwise parse as a function
		else
		{
			List<StringBuilder> terms = getSubTerms(exp);
			
			// If there's one term in the list it should be a literal
			if (terms.size() == 1)
				return new Literal(terms.get(0).toString());
			
			moveOperatorToPrefix(terms);
			
			// Streams and lambdas won't work here because we want want exceptions to bubble up
			List<Expression> ansTerms = new ArrayList<>();
			// Pop the first term and cast it to an Operator
			Operator op = Operator.valueOf(terms.remove(0).toString());
			for (StringBuilder term : terms)
				ansTerms.add(readTerm(term));
			return new Function(op, ansTerms);
		}
	}
	
	/**
	 * Gets a list of the high level terms in the given string.  A high level term is
	 * one that is not nested inside another expression.
	 * @param exp the string to pull terms from
	 * @return a list of the high level terms in the string.
	 */
	private static List<StringBuilder> getSubTerms(StringBuilder exp)
	{
		List<StringBuilder> ans = new ArrayList<>();
		int bracketCount = 0;
		int lastMatch = 0; // The index of the last term that was read
		
		// For each top level space in the expression
		for (int i=0; i<exp.length(); ++i)
			if (exp.charAt(i) == '(')
				bracketCount++;
			else if (exp.charAt(i) == ')')
				bracketCount--;
			// Add term if on the top level, first char is a space, and previous term is not a negation.
			else if (bracketCount == 0 && exp.charAt(i) == ' ' && !exp.substring(Math.max(i-3, 0), i).equals("NEG"))
			{
				ans.add(new StringBuilder(exp.substring(lastMatch, i)));
				lastMatch = i+1;
			}
		
		// The last term isn't followed by a space, we have to add it now
		ans.add(new StringBuilder(exp.substring(lastMatch, exp.length())));
		
		return ans;
	}
	
	/**
	 * Takes a list of terms and puts the operator into the first index (prefix form).
	 * If there are multiple operators only the first one will be handled
	 * @param terms a list of terms with one operator within
	 * @throws InvalidArgumentsException if the list of terms is in neither prefix form nor correct infix form
	 */
	private static void moveOperatorToPrefix(List<StringBuilder> terms) throws InvalidArgumentsException
	{
		int op = operatorLocation(terms);
		Operator operator = Operator.valueOf(terms.get(op).toString());
		if (op != 0 && operator.symbolPosition != op)
			throw new InvalidArgumentsException(String.format
			(
				"Operator %s found in position %d, should be 0 or %d",
				operator.displayText,
				op,
				operator.symbolPosition
			));
		StringBuilder temp = terms.get(op);
		terms.set(op, terms.get(0));
		terms.set(0, temp);
	}
	
	/**
	 * Finds the index of the first operator in the list of terms
	 * @param terms the terms to search
	 * @return the index of the operator
	 * @throws InvalidArgumentsException if there is no operator in the given terms
	 */
	private static int operatorLocation(List<StringBuilder> terms) throws InvalidArgumentsException
	{
		return IntStream.range(0, terms.size())
			.filter(i -> isOperator(terms.get(i).toString()))
			.findFirst()
			.orElseThrow(() -> new InvalidArgumentsException(terms.toString()));
	}
	
	/**
	 * Tests whether the given string is an operator, either the name or the display symbol
	 * @param s the string to test
	 * @return true if the string is an operator, false otherwise
	 */
	private static boolean isOperator(String s)
	{
		return Stream.of(Operator.values())
			.anyMatch(o -> s.equals(o.displayText) || s.equals(o.name()));
	}
	
	/**
	 * Finds the index of the next closing parenthesis on the same level as the given opening parenthesis.
	 * Returns -1 if the given expression and index does not have a corresponding closing parenthesis.
	 * @param exp the expression to search through
	 * @param startPos the index to start searching from
	 * @return either the index of the closing parenthesis or -1
	 */
	private static int findNextParen(StringBuilder exp, int startPos)
	{
		int bracketNum = 0; // keep track of what level we're on
		for (int i=startPos; i<exp.length(); ++i)
			if (exp.charAt(i) == '(')
				bracketNum++;
			else if (exp.charAt(i) == ')' && --bracketNum == 0)
				return i;
		return -1;
	}
}
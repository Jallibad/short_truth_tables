package logic;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import logic.transform.TransformSteps;

/**
 * An abstract class that represents a FOL statement.
 * Subclassed by Literal and Function 
 * @author Jallibad
 *
 */
public abstract class Expression implements Serializable
{
	private static final long serialVersionUID = -1298428615072603639L;
	private static final Logger LOGGER = Logger.getLogger(Expression.class.getName());
	static
	{
		LOGGER.setLevel(Level.ALL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		LOGGER.addHandler(handler);
		handler.setLevel(Level.ALL);
	}
	
	/**
	 * Creates a Set containing each variable that occurs in the expression
	 * @return the specified Set
	 */
	public abstract Set<Literal> getVariables();
	
	// TODO rewrite this JavaDoc to be more correct
	/**
	 * Checks if the Expressions have the same syntactic form, ignoring Literal names.
	 * The argument specifies the minimum complexity
	 * "A".matches("B"), and "(AND A B)".matches("(AND P Q)"), but !"(AND A B)".matches("(OR A B)")
	 * @param pattern the Expression to match against
	 * @return True if the Expressions match, false otherwise
	 */
	public abstract boolean matches(Expression pattern);

	/**
	 * Checks if the Expressions are the same other than literal names
	 * @param pattern the Expression to match against
	 * @return true if the Expressions match, false otherwise
	 */
	public abstract boolean equalWithoutLiterals(Expression pattern);
	
	/**
	 * Checks if the Expression is the the same as the string other than literal names
	 * @param pattern the Expression to match against
	 * @return true if the Expressions match, false otherwise
	 */
	public boolean equalWithoutLiterals(String pattern)
	{
		return equalWithoutLiterals(ExpParser.parseUnsafe(pattern));
	}
	
	public boolean matches(String pattern) // TODO write JavaDoc for this I'm lazy
	{
		return matches(ExpParser.parseUnsafe(pattern));
	}
	public abstract Optional<Map<Literal,Expression>> fillMatches(Expression e);
	
	/**
	 * A version of the expression with proper infix notation and symbols
	 * @return a formatted String
	 */
	public abstract String prettyPrint();
	
	/**
	 * Helper function that returns null if expression is a Literal, the operator if it is a Function
	 * @return the operator
	 */
	public abstract Operator getOperator();
	
	public abstract int complexity();
	
	/**
	 * Tests full equality, whether the two Expression have the exact same form.
	 * In other words, (AND A B) != (AND B A)
	 */
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract int hashCode();
	
	/**
	 * Checks whether two Expressions are simply logically equivalent, including commutativity and associativity
	 * @param other
	 * @return
	 */
	public abstract boolean simplyEquivalent(Expression other);
	
	public abstract Optional<TransformSteps> simplyEquivalentWithSteps(Expression other);
	
	public abstract Optional<TransformSteps> proveEquivalence(Expression other);
	
	/**
	 * Maps a predicate over the expression, testing if the operator is in the given list
	 * and if each of the terms matches the predicate.  This is intended to be used recursively. 
	 * @param p the predicate to test against
	 * @param allowedOperators a variadic Array of allowed operators
	 * @return true if the operator is in the list of allowed operators and for each term the predicate p is true
	 */
	public abstract boolean mapPredicate(Predicate<Expression> p, Operator... allowedOperators);
	
	public abstract Expression mapTerms(java.util.function.Function<Expression, Expression> toMap);
	
	public abstract boolean evaluate(Map<Literal, Boolean> settings);
}
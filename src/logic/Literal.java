package logic;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import logic.malformedexpression.InvalidArgumentsException;
import logic.malformedexpression.MalformedExpressionError;
import logic.transform.TransformSteps;

/**
 * This class represents a single variable such as "A" or "B".
 * @author Jallibad
 *
 */
public class Literal extends Expression
{
	private static final long serialVersionUID = 7226891007841491566L;
	public final String variableName;
	
	/**
	 * Constructs a new Literal with the given name
	 * @param variableName the name of the Literal
	 * @throws InvalidArgumentsException if variableName is an operator or operator symbol
	 */
	public Literal(String variableName) throws InvalidArgumentsException
	{
		if (Stream.of(Operator.values()).anyMatch(o -> o.displayText.equals(variableName) || o.name().equals(variableName)))
			throw new InvalidArgumentsException(variableName + " is an operator"); // TODO add error details
		this.variableName = variableName;
	}
	
	public static Literal createUnsafe(String variableName)
	{
		try
		{
			return new Literal(variableName);
		}
		catch (InvalidArgumentsException e)
		{
			throw new MalformedExpressionError(e.getMessage());
		}
	}
	
	@Override
	public Set<Literal> getVariables()
	{
		return Collections.singleton(this);
	}
	
	@Override
	public String toString()
	{
		return variableName;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Literal)
			return variableName.equals(((Literal) o).variableName);
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return variableName.hashCode();
	}

	@Override
	public boolean matches(Expression e)
	{
		return true; // A Literal matches any Expression
	}

	@Override
	public Optional<Map<Literal, Expression>> fillMatches(Expression e)
	{
		Map<Literal, Expression> ans = new HashMap<>();
		ans.put(this, e);
		return Optional.of(ans);
	}

	@Override
	public String prettyPrint()
	{
		return variableName;
	}

	@Override
	public Operator getOperator()
	{
		return null;
	}

	@Override
	public int complexity()
	{
		return 1;
	}

	@Override
	public boolean simplyEquivalent(Expression other)
	{
		return (other instanceof Literal) && ((Literal) other).variableName.equals(variableName);
	}
	
	@Override
	public Optional<TransformSteps> simplyEquivalentWithSteps(Expression other)
	{
		if (simplyEquivalent(other))
			return Optional.of(new TransformSteps(this));
		else
			return Optional.empty();
	}

	@Override
	public Optional<TransformSteps> proveEquivalence(Expression other)
	{
		if (simplyEquivalent(other))
			return Optional.of(new TransformSteps(this));
		else
			return Optional.empty();
	}

	@Override
	public boolean equalWithoutLiterals(Expression pattern)
	{
		return pattern instanceof Literal;
	}

	@Override
	public boolean mapPredicate(Predicate<Expression> p, Operator... op)
	{
		return p.test(this);
	}

	@Override
	public boolean evaluate(Map<Literal, Boolean> settings)
	{
		return settings.get(this);
	}

	@Override
	public Expression mapTerms(Function<Expression, Expression> toMap)
	{
		return this;
	}
}
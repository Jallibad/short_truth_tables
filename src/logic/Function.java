package logic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import logic.malformedexpression.InvalidArgumentsException;
import logic.malformedexpression.MalformedExpressionError;
import logic.malformedexpression.MalformedExpressionException;
import logic.transform.MiscTransform;
import logic.transform.NormalForm;
import logic.transform.TransformSteps;

/**
 * The Function class represent functions such as "(NEG A)" and "(AND A B)".
 * Functions are represented as an Operator and an ordered List of terms, with terms[0] being the first argument.
 * The Expression::create function is a simpler method of constructing Function instances, especially nested
 * ones rather than using the constructors here.
 * @author Jallibad
 *
 */
public class Function extends Expression
{
	private static final long serialVersionUID = 7003195412543405388L;
	private static final Logger LOGGER = Logger.getLogger(Function.class.getName());
	public final Operator operator;
	
	/**
	 * An ordered List of the arguments to the Function, with term[0] being the first argument.
	 * For instance with "(AND A B)" the list would contain [A, B].
	 */
	private List<Expression> terms;
	
	/**
	 * The basic constructor for the Function class.  Checks that the number
	 * terms matches up with the expected number of arguments.
	 * @param operator the operator for the new Function
	 * @param terms a List of the terms, a copy is made to avoid rep exposure
	 * @throws InvalidArgumentsException if the number of terms provided doesn't match the
	 * number of terms expected by the operator
	 */
	public Function(Operator operator, List<Expression> terms) throws InvalidArgumentsException
	{
		if (terms.size() != operator.numArguments)
		{
			throw new InvalidArgumentsException(String.format
			(
				"Operator \"%s\" expects %d arguments, %d were provided",
				operator,
				operator.numArguments,
				terms.size()
			));
		}
		this.operator = operator;
		this.terms = new ArrayList<>(terms);
	}
	
	/**
	 * A wrapper function to construct a new Function object.  Does not check that the number of terms are correct.
	 * @param operator the operator for the new Function
	 * @param terms a List of the terms, a copy is made to avoid rep exposure
	 */
	public static Expression constructUnsafe(Operator operator, List<Expression> terms)
	{
		try
		{
			return new Function(operator, terms);
		}
		catch (MalformedExpressionException e)
		{
			LOGGER.severe(e.getMessage());
			throw new MalformedExpressionError(e.getMessage());
		}
	}

	/**
	 * A wrapper constructor to make constructing Functions inline somewhat simpler.
	 * @param operator the Operator to be used in the Function
	 * @param terms an Expression[] consisting of the terms in order.  Uses variadic arguments.
	 * @throws MalformedExpressionException 
	 */
	public Function(Operator operator, Expression... terms) throws InvalidArgumentsException
	{
		this(operator, Arrays.asList(terms));
	}
	
	/**
	 * A wrapper constructor to make constructing Functions inline somewhat simpler.
	 * Each String in the second argument is parsed using Expression::create.
	 * @param operator the Operator to be used in the Function
	 * @param terms a String[] consisting of the terms in order.  Uses variadic arguments.
	 * @throws MalformedExpressionException 
	 */
	public Function(Operator operator, String... terms) throws InvalidArgumentsException
	{
		if (terms.length != operator.numArguments)
		{
			throw new InvalidArgumentsException(String.format
			(
				"Operator \"%s\" expects %d arguments, %d were provided",
				operator,
				operator.numArguments,
				terms.length
			));
		}
		this.operator = operator;
		this.terms = new ArrayList<>();
		for (String s : terms)
			this.terms.add(new Literal(s));
	}
	
	/**
	 * Getter method for the the terms or arguments to this function.
	 * Terms are in sorted order with getTerms().get(0) being the first argument.
	 * @return The List of terms, a copy is made to avoid rep exposure
	 */
	public List<Expression> getTerms()
	{
		return new ArrayList<>(terms); // Copy new list to avoid representation exposure
	}
	
	/**
	 * Gets the term[i] element of the terms List.
	 * It's slightly more efficient to use this function if the
	 * full list of terms isn't needed.
	 * @param i the index of the term, 0 is the first
	 * @return The i'th term of the terms List
	 */
	public Expression getTerm(int i)
	{
		return terms.get(i);
	}
	
	@Override
	public String toString()
	{
		StringBuilder ans = new StringBuilder(operator.toString());
		for (Expression e : terms)
			ans.append(" "+e);
		return "("+ans+")";
	}

	@Override
	public Set<Literal> getVariables()
	{
		Set<Literal> ans = new HashSet<>();
		for (Expression e : terms)
			ans.addAll(e.getVariables());
		return ans;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Function))
			return false;
		Function other = (Function) o;
		if (operator != other.operator)
			return false;
		for (int i=0; i<terms.size(); ++i)
			if (!terms.get(i).equals(other.getTerm(i)))
				return false;
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(operator, terms);
	}

	@Override
	public boolean matches(Expression e)
	{
		if (!(e instanceof Function))
			return false;
		Function other = (Function) e;
		if (operator != other.operator)
			return false;
		for (int i=0; i<terms.size(); ++i)
			if (!terms.get(i).matches(other.getTerm(i)))
				return false;
		return true;
	}

	@Override
	public Optional<Map<Literal, Expression>> fillMatches(Expression e)
	{
		if (operator != e.getOperator())
			return Optional.empty();
		Function other = (Function) e;
		Map<Literal, Expression> ans = new HashMap<>();
		for (int i=0; i<terms.size(); ++i)
		{
			Optional<Map<Literal, Expression>> subterms = terms.get(i).fillMatches(other.getTerm(i));
			subterms.ifPresent(ans::putAll);
			if (!subterms.isPresent())
				return Optional.empty();
		}
		return Optional.of(ans);
	}

	@Override
	public String prettyPrint()
	{
		if (operator == Operator.NEG)
		{
			if (terms.get(0) instanceof Function)
				return operator.displayText+"("+terms.get(0).prettyPrint()+")";
			else
				return operator.displayText+terms.get(0).prettyPrint();
		}
		
		StringBuilder ans = new StringBuilder();
		for (int i=0; i<terms.size()+1; ++i)
		{
			if (i == operator.symbolPosition)
			{
				ans.append(" "+operator.displayText);
				continue;
			}
			Expression currTerm = terms.get(i<operator.symbolPosition ? i : i-1); // Account for inserting the operator
			if (currTerm instanceof Literal || currTerm.getOperator() == Operator.NEG)
				ans.append(" "+currTerm.prettyPrint());
			else
				ans.append(" ("+currTerm.prettyPrint()+")");
		}
		return ans.substring(1);
	}

	@Override
	public Operator getOperator()
	{
		return operator;
	}

	@Override
	public int complexity()
	{
		// Sum of complexity of all terms + 1 for the operator
		return terms.stream().collect(Collectors.summingInt(Expression::complexity))+1;
	}

	@Override
	public boolean simplyEquivalent(Expression o)
	{
		return simplyEquivalentWithSteps(o).isPresent();
	}
	
	@Override
	public Optional<TransformSteps> simplyEquivalentWithSteps(Expression o)
	{
		if (o.getOperator() != operator)
			return Optional.empty();
		Function other = (Function) o;
		
		if
		(
			operator.hasTrait(OperatorTrait.COMMUTATIVE)
			&& terms.get(0).simplyEquivalent(other.terms.get(1))
			&& terms.get(1).simplyEquivalent(other.terms.get(0))
		)
		{
			TransformSteps ans = new TransformSteps(this);
			ans.addStep(MiscTransform.COMMUTE);
			return Optional.of(ans); // TODO commute
		}
		
		if (operator.hasTrait(OperatorTrait.ASSOCIATIVE))
		{
			// TODO implement
		}
		if (equals(other))
			return Optional.of(new TransformSteps(this));
		else
			return Optional.empty();
	}

	@Override
	public Optional<TransformSteps> proveEquivalence(Expression other)
	{
		TransformSteps ans = NormalForm.CONJUNCTIVE.transformWithSteps(this);
		TransformSteps secondHalf = NormalForm.CONJUNCTIVE.transformWithSteps(other);
		return ans
				.result()
				.simplyEquivalentWithSteps(secondHalf.result())
				.map
				(
					e -> ans
						.combine(e)
						.combine(secondHalf.reverse())
				);
	}
	
	@Override
	public Function mapTerms(java.util.function.Function<Expression, Expression> f)
	{
		return (Function) constructUnsafe(operator, terms.stream().map(f).collect(Collectors.toList()));
	}

	@Override
	public boolean equalWithoutLiterals(Expression pattern)
	{
		if (operator != pattern.getOperator())
			return false;
		Function other = (Function) pattern;
		for (int i=0; i<terms.size(); ++i)
			if (!terms.get(i).equalWithoutLiterals(other.getTerm(i)))
				return false;
		return true;
	}
	
	public boolean mapPredicate(Predicate<Expression> p, Operator... op)
	{
		return Arrays.asList(op).contains(operator) && terms.stream().allMatch(p);
	}

	@Override
	public boolean evaluate(Map<Literal, Boolean> settings)
	{
		boolean[][] truthTable = operator.truthTable;
		List<Boolean> evalTerms = terms.stream().map(t -> t.evaluate(settings)).collect(Collectors.toList());
		for (int i=0; i<truthTable.length; ++i)
		{
			boolean isValid = true;
			for (int x=0; x<truthTable[i].length-1; ++x)
				isValid &= evalTerms.get(x) == truthTable[i][x];
			if (isValid)
				return truthTable[i][truthTable[i].length-1];
		}
		return false;
	}
}
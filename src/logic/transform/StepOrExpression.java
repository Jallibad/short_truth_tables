package logic.transform;

import java.util.Optional;

import logic.Expression;

/**
 * A container object for use with TransformSteps.  StepOrExpression is guaranteed to contain
 * either a TransformStep or an Expression.
 * @author Jallibad
 *
 */
public class StepOrExpression
{
	private final Optional<TransformStep> step;
	private final Optional<Expression> exp;
	
	/**
	 * Creates a new object that contains the given TransformStep
	 * @param step the TransformStep to contain
	 */
	StepOrExpression(TransformStep step)
	{
		this.step = Optional.of(step);
		this.exp = Optional.empty();
	}
	
	/**
	 * Creates a new object that contains the given Expression
	 * @param exp the Expression to contain
	 */
	StepOrExpression(Expression exp)
	{
		this.step = Optional.empty();
		this.exp = Optional.of(exp);
	}
	
	/**
	 * A higher order function that takes two functions, one that accepts a TransformStep
	 * and one that accepts an Expression.  Both functions should return the same thing, and
	 * the StepOrExpression will choose which to apply based on its content, returning the result of
	 * the correct one. 
	 * @param stepMap the function to apply to a TransformStep
	 * @param expressionMap the function to apply to an Expression
	 * @return the result of applying the correct function to the contained object
	 */
	public <T> T mapOver(java.util.function.Function<TransformStep, T> stepMap, java.util.function.Function<Expression, T> expressionMap)
	{
		if (step.isPresent())
			return stepMap.apply(step.get());
		else if (exp.isPresent())
			return expressionMap.apply(exp.get());
		throw new Error("Neither step nor expression is present");
	}
	
	@Override
	public String toString()
	{
		if (step.isPresent())
			return "TransformStep: "+step.get();
		else if (exp.isPresent())
			return "Expression: "+exp.get();
		else
			return "Neither step nor expression is present";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof StepOrExpression))
			return false;
		StepOrExpression other = (StepOrExpression) o;
		return other.exp.equals(exp) && other.step.equals(step);
	}
	
	@Override
	public int hashCode()
	{
		if (step.isPresent())
			return step.hashCode();
		else
			return exp.hashCode();
	}
}
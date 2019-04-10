package logic.transform;

import java.io.Serializable;
import java.util.Map;

import logic.Expression;
import logic.Function;
import logic.Literal;

/**
 * An interface representing arbitrary transformations that can be applied to
 * Expressions, along with the InferenceRule steps applied to get the desired result
 * @author Jallibad
 *
 */
public interface Transform extends Serializable
{
	/**
	 * A simple transform that returns only the final result
	 * @param orig the expression to be transformed
	 * @return the resultant Expression
	 */
	public Expression transform(Expression orig);
	
	/**
	 * Transforms the given Expression, saving the intermediate steps along the way
	 * @param orig the expression to be transformed
	 * @return a TransformSteps object containing each intermediate InferenceRule
	 */
	public TransformSteps transformWithSteps(Expression orig);
	
	static Expression transform(Map<Literal,Expression> mapping, Expression e)
	{
		if (e instanceof Literal && mapping.containsKey(e))
			return mapping.get(e);
		else if (e instanceof Function)
			return ((Function) e).mapTerms(x -> transform(mapping, x));
		else
			throw new TransformNotApplicableError();
	}
}
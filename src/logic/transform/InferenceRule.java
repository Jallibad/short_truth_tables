package logic.transform;

import java.util.logging.Logger;

import logic.ExpParser;
import logic.Expression;

public enum InferenceRule implements BiDirectionalTransform
{
	// TODO all of these except for DOUBLE_NEGATION are backwards
	DE_MORGANS_OR("¬(P∨Q)", "(¬P)∧(¬Q)", "DeMorgan's or"),
	DE_MORGANS_AND("¬(P∧Q)", "(¬P)∨(¬Q)", "DeMorgan's and"),
	OR_DISTRIBUTION("P∨(Q∧R)", "(P∨Q)∧(P∨R)"),
	OR_DISTRIBUTION_FLIPPED("(Q∧R)∨P", "(P∨Q)∧(P∨R)"),
	AND_DISTRIBUTION("P∧(Q∨R)", "(P∧Q)∨(P∧R)"),
	AND_DISTRIBUTION_FLIPPED("(Q∨R)∧P", "(P∧Q)∨(P∧R)"),
	DOUBLE_NEGATION("¬¬P", "P");

	private final Expression left;
	private final Expression right;
	private final String name;

	/**
	 * InferenceRule constructor, takes the left and right expression, as well as a display name
	 * @param left the left expression, by convention more complex
	 * @param right the right expression, by convention less complex
	 * @param name the display name
	 */
	InferenceRule(String left, String right, String name)
	{
		// We have to use parseUnsafe because the enum constructor can't throw a checked exception
		this.left = ExpParser.parseUnsafe(left);
		this.right = ExpParser.parseUnsafe(right);
		this.name = name;
	}
	
	/**
	 * InferenceRule constructor, takes the left and right expression
	 * @param left the left expression, by convention more complex
	 * @param right the right expression, by convention less complex
	 */
	InferenceRule(String left, String right)
	{
		this.left = ExpParser.parseUnsafe(left);
		this.right = ExpParser.parseUnsafe(right);
		name = null;
	}
	
	public Expression transform(Expression orig)
	{
//		left.fillMatches(orig)
//			.ifPresent(m -> Transform.transform(m, right))
//			//.orElse(right.fillMatches(orig).orElse(null));
		if (left.matches(orig))
			return Transform.transform(left.fillMatches(orig).get(), right);
		else if (right.matches(orig))
			return Transform.transform(right.fillMatches(orig).get(), left);
		LOGGER.warning("An inference rule couldn't be successfully applied");
		return orig; // TODO this could be a terrible idea
	}
	
	@Override
	public TransformSteps transformWithSteps(Expression orig)
	{
		TransformSteps ans = new TransformSteps(orig);
		ans.addStep(this);
		return ans;
	}
	
	/**
	 * Returns the name of the inference rule.  By default that is the name of the enum with underscores
	 * replaced by spaces, and all in lowercase.  Some rules have specialized names though. 
	 */
	@Override
	public String toString()
	{
		if (name != null)
			return name;
		return name().replaceAll("_", " ").toLowerCase();
	}

	@Override
	public Expression left()
	{
		return left;
	}

	@Override
	public Expression right()
	{
		return right;
	}
	
	private static final Logger LOGGER = Logger.getLogger(Expression.class.getName());
}
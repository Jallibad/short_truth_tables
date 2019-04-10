package logic.transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import logic.ExpParser;
import logic.Expression;
import logic.malformedexpression.MalformedExpressionException;

public class Simplify implements Transform
{
	public static void main(String[] args) throws MalformedExpressionException
	{
		System.out.println(ACT.transform(ExpParser.parse("NEG ((NEG P) OR (NEG Q))")));
	}
	
	private static final long serialVersionUID = -5938246885231590898L;
	private static final List<BiDirectionalTransform> strategies = new ArrayList<>();
	static
	{
		strategies.add(InferenceRule.DOUBLE_NEGATION);
		strategies.add(InferenceRule.DE_MORGANS_AND);
		strategies.add(InferenceRule.DE_MORGANS_OR);
	}
	
	public static final Simplify ACT = new Simplify();
	
	private Simplify()
	{
		
	}

	@Override
	public Expression transform(Expression e)
	{
		return strategies.stream()
				.map(strat -> strat.transformLeft(e))
				.min(Comparator.comparing(simplified -> e.complexity()-simplified.complexity()))
				.orElse(e).mapTerms(ACT::transform);
	}

	@Override
	public TransformSteps transformWithSteps(Expression orig)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
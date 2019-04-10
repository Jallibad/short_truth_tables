package logic.transform;

import java.util.ArrayList;
import java.util.List;

import logic.Expression;
import logic.Function;
import logic.OperatorTrait;

public enum MiscTransform implements Transform
{
	ASSOCIATE,
	COMMUTE;

	@Override
	public Expression transform(Expression orig)
	{
		// TODO Auto-generated method stub
		switch (this)
		{
			case ASSOCIATE:
				break;
			case COMMUTE:
				if (orig.getOperator().hasTrait(OperatorTrait.COMMUTATIVE))
				{
					List<Expression> terms = new ArrayList<>();
					terms.add(((Function) orig).getTerm(1));
					terms.add(((Function) orig).getTerm(0));
					return Function.constructUnsafe(orig.getOperator(), terms);
				}
				else
					return null; 
			default:
				break;
		}
		return null;
	}

	@Override
	public TransformSteps transformWithSteps(Expression orig)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
package short_truth_tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import logic.ExpParser;
import logic.Expression;
import logic.Function;
import logic.malformedexpression.MalformedExpressionException;

public class Assignment
{
	public static void main(String[] args) throws MalformedExpressionException
	{
		Expression exp = ExpParser.parse("(P∨Q)∧P");
		System.out.println(exp);
		Assignment a = new Assignment(exp);
		for (Assignment b : a.assignments())
		{
			System.out.println(b);
			System.out.println(b.getParticiple());
		}
	}
	
	public Assignment(Expression exp)
	{
		this.exp = exp;
		setting = Optional.empty();
		subExpressions = new ArrayList<>();
		if (exp instanceof Function)
			for (Expression e : ((Function) exp).getTerms())
				subExpressions.add(new Assignment(e));
	}
	
	public List<Assignment> assignments()
	{
		List<Assignment> ans = new ArrayList<>(subExpressions);
		if (exp instanceof Function)
			ans.add(exp.getOperator().symbolPosition, this);
		else
			ans.add(this);
		return ans;
	}
	
	public String getParticiple()
	{
		if (exp instanceof Function)
			return exp.getOperator().displayText;
		return exp.prettyPrint();
	}

	private final Expression exp;
	private final List<Assignment> subExpressions;
	@SuppressWarnings("unused")
	private Optional<Boolean> setting;
}
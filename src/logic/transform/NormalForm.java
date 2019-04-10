package logic.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import logic.Expression;
import logic.Function;
import logic.Literal;
import logic.Operator;

/**
 * An enum representing the supported normal form transformations.  Subclass Transform.
 * @author Jallibad
 *
 */
public enum NormalForm implements Transform
{
	/**
	 * <p>
	 * Enum representing a transformation of conjunctive normal form
	 * </p>
	 * 
	 * Conjunctive normal form is a conjunction of one or more clauses,
	 * where a clause is a disjunction of literals
	 */
	CONJUNCTIVE,
	
	/**
	 * <p>
	 * Enum representing a transformation of disjunctive normal form
	 * </p>
	 * Disjunctive normal form is a disjunction of one or more clauses,
	 * where a clause is a conjunction of literals
	 * 
	 */
	DISJUNCTIVE,
	
	/**
	 * <p>
	 * Enum representing a transformation of negation normal form
	 * </p>
	 * 
	 * Negation normal form allows only conjunctions and disjunctions, as well as negations applied directly to literals.
	 */
	NEGATION;

	@Override
	public Expression transform(Expression orig)
	{
		switch (this)
		{
			case CONJUNCTIVE:
				// Put into NNF, then drive all "∨"s inwards.
				return transformHelper(NEGATION.transform(orig),
					InferenceRule.OR_DISTRIBUTION,
					InferenceRule.OR_DISTRIBUTION_FLIPPED
				);
			case DISJUNCTIVE:
				// Put into NNF, then drive all "∧"s inwards.
				return transformHelper(NEGATION.transform(orig),
					InferenceRule.AND_DISTRIBUTION,
					InferenceRule.AND_DISTRIBUTION_FLIPPED
				);
			case NEGATION:
				// Drive negations inwards using DeMorgan's laws, eliminate any double negations
				return transformHelper(orig,
					InferenceRule.DE_MORGANS_OR,
					InferenceRule.DE_MORGANS_AND,
					InferenceRule.DOUBLE_NEGATION
				);
			default:
				throw new UnsupportedOperationException("A normal form transform has been applied without an implementation");
		}
	}
	
	/**
	 * Checks if the expression is in either conjunctive, disjunctive, or negation normal form
	 * 
	 * @param e the Expression, who's form is being checked
	 * @return True if the expression is in the indicated form, otherwise False
	 */
	public boolean inForm(Expression e)
	{
		switch (this)
		{
			case CONJUNCTIVE:
				return
					// Either the expression should be disjunctions of (possibly negated) literals
					checkAll(Operator.OR, e) ||
					// Or it should be a conjunction with each clause in CNF
					e.mapPredicate(CONJUNCTIVE::inForm, Operator.AND);
				
			case DISJUNCTIVE:
				return
					// Same as CNF but with conjunctions and disjunctions flipped
					checkAll(Operator.AND, e) ||
					e.mapPredicate(DISJUNCTIVE::inForm, Operator.OR);
				
			case NEGATION:
				return
					// The expression should be either a (possibly negated) literal
					e instanceof Literal ||
					e.equalWithoutLiterals("¬A") ||
					// Or the expression is a conjunction or disjunction of clauses in NNF
					e.mapPredicate(NEGATION::inForm, Operator.AND, Operator.OR);
			default:
				throw new UnsupportedOperationException("A normal form has been checked without an implementation");
		}
	}
	
	/**
	 * Recursively checks if every subterm is either a (possibly negated) literal,
	 * or if the given operator expression is a function of the given operator and every clause
	 * matches the original criteria.
	 * @param op the allowed operator
	 * @param e the expression to check
	 * @return true if the expression consists of only negations and the specified operator
	 */
	private static boolean checkAll(Operator op, Expression e)
	{
		return
			e instanceof Literal ||
			e.equalWithoutLiterals("¬A") ||
			e.mapPredicate(t -> checkAll(op,t), op);
	}

	@Override
	public TransformSteps transformWithSteps(Expression orig)
	{
		switch (this)
		{
			case CONJUNCTIVE:
				return transformHelperWithSteps(NEGATION.transformWithSteps(orig), InferenceRule.OR_DISTRIBUTION);
			case DISJUNCTIVE:
				return transformHelperWithSteps(NEGATION.transformWithSteps(orig), InferenceRule.AND_DISTRIBUTION);
			case NEGATION:
				return transformHelperWithSteps(orig,
					InferenceRule.DE_MORGANS_OR,
					InferenceRule.DE_MORGANS_AND,
					InferenceRule.DOUBLE_NEGATION
				);
			default:
				throw new UnsupportedOperationException("A normal form transform has been applied without an implementation");
		}
	}
	
	private TransformSteps transformHelperWithSteps(TransformSteps orig, InferenceRule inferenceRules)
	{
		return orig.combine(transformHelperWithSteps(orig.result(), inferenceRules));
	}

	private Expression transformHelper(Expression orig, InferenceRule... inferenceRules)
	{
		for (InferenceRule i : inferenceRules)
			orig = i.transformLeft(orig);
		if (orig instanceof Function)
		{
			Function f = (Function) orig;
			List<Expression> normalFormTerms =
				f.getTerms().stream().map(t -> transformHelper(t, inferenceRules)).collect(Collectors.toList());
			orig = Function.constructUnsafe(f.operator, normalFormTerms);
		}
		return orig;
	}
	
	private TransformSteps transformHelperWithSteps(Expression orig, InferenceRule... inferenceRules)
	{
		TransformSteps steps = new TransformSteps(orig);
		for (InferenceRule i : inferenceRules)
			i.transformLeftWithSteps(steps);
		if (steps.result() instanceof Function)
		{
			Function f = (Function) steps.result();
			List<Expression> getTerms = f.getTerms();
			List<Expression> normalFormTerms = new ArrayList<>();
			for (int i=0; i<getTerms.size(); ++i)
			{
				TransformSteps partialSteps = transformHelperWithSteps(getTerms.get(i), inferenceRules);
				normalFormTerms.add(partialSteps.result());
				steps.combine(partialSteps, i);
			}
		}
		return steps;
	}
	
	@Override
	public String toString()
	{
		return name().toLowerCase()+" normal form";
	}
}
package logic.transform;

import java.io.Serializable;
import java.util.Objects;

import logic.Expression;

/**
 * An immutable container class representing a single transform, along with the expressions before and after
 * @author Jallibad
 *
 */
public class TransformStep implements Serializable
{
	private static final long serialVersionUID = -1364619449009668270L;
	public final Expression before;
	public final Transform step;
	public final Expression after;
	
	public TransformStep(Expression before, Transform step, Expression after)
	{
		this.step = step;
		this.before = before;
		this.after = after;
	}
	
	@Override
	public String toString()
	{
		return before+" --- "+step+" --- "+after;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof TransformStep))
			return false;
		TransformStep o = (TransformStep) other;
		return before.equals(o.before) && step.equals(o.step) && after.equals(o.after); 
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(before, step, after);
	}
}
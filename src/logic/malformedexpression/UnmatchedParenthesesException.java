package logic.malformedexpression;

/**
 * A subclass of MalformedExpressionException representing an expression with unbalanced parentheses.
 * @author Jallibad
 *
 */
public class UnmatchedParenthesesException extends MalformedExpressionException
{
	private static final long serialVersionUID = -4957127812448519949L;
	
	public final String exp;
	public final int location;
	
	public UnmatchedParenthesesException(String exp, int location)
	{
		super(String.format("Unmatched parentheses found in \"%s\" at index %d", exp, location));
		this.exp = exp;
		this.location = location;
	}
}
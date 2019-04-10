package logic.malformedexpression;

/**
 * A subclass of exception representing a malformed string being parsed to an expression.
 * Child classes represent more specific error conditions.
 * @author Jallibad
 *
 */
public class MalformedExpressionException extends Exception
{
	private static final long serialVersionUID = -4940968439633433319L;

	public MalformedExpressionException(String s)
	{
		super(s);
	}

	public MalformedExpressionException()
	{
		
	}
}
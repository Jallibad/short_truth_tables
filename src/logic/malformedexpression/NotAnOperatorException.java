package logic.malformedexpression;

/**
 * A subclass of MalformedExpressionException, representing an invalid operator string.
 * @author Jallibad
 *
 */
public class NotAnOperatorException extends MalformedExpressionException
{
	public final String operator;
	
	private static final long serialVersionUID = -8575123240896718843L;
	public NotAnOperatorException(String operator)
	{
		this.operator = operator;
	}
}
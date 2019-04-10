package logic.malformedexpression;

/**
 * Error representing an upcast MalformedExpressionException
 * @author Jallibad
 *
 */
public class MalformedExpressionError extends Error
{
	public MalformedExpressionError(String message)
	{
		super(message);
	}

	private static final long serialVersionUID = 882296948389778446L;
}
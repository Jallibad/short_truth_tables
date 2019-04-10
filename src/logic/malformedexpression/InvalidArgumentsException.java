package logic.malformedexpression;

/**
 * A subclass of MalformedExpressionException, representing an invalid function definition.
 * Either the wrong number of arguments, the wrong order, or some other similar problem.
 * @author Jallibad
 *
 */
public class InvalidArgumentsException extends MalformedExpressionException
{
	private static final long serialVersionUID = -208040771676620152L;
	
	public InvalidArgumentsException(String s)
	{
		super(s);
	}
}
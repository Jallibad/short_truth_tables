package short_truth_tables;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.ExpParser;
import logic.Expression;
import logic.Function;
import logic.Literal;

public class Test extends Application
{
	public static void thing(Expression exp)
	{
		if (exp instanceof Literal)
			System.out.println("Literal: " + exp);
		else
		{
			System.out.println("Operator: " + exp.getOperator());
			((Function) exp).getTerms().stream().forEach(Test::thing);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Expression exp = ExpParser.parse("P∨Q");
		System.out.println(exp);
		Assignment a = new Assignment(exp);
		for (Assignment b : a.assignments())
			System.out.println(b.getParticiple());
		Parent p = FXMLLoader.load(getClass().getResource("/AssignmentPicker.fxml"));
		Scene s = new Scene(p);
		
		primaryStage.setScene(s);
		primaryStage.show();
	}
}
package short_truth_tables;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AssignmentPickerController
{
	@FXML Label expression;
	
	@FXML
	public Button assignTrue;
	
	@FXML
	public Button assignFalse;
	
	@FXML
	public void assignTrue(ActionEvent event)
	{
		System.out.println("Assigning true");
	}
	
	@FXML
	public void assignFalse(ActionEvent event)
	{
		System.out.println("Assigning false");
	}
}
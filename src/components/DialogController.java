package components;

import components.model.Artist;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField newArtistName;

    public String processResult(){
        String newName = newArtistName.getText().trim();
        return newName;
    }
}

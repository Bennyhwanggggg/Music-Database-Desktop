package components;

import components.model.Album;
import components.model.Artist;
import components.model.Datasource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Optional;

public class Controller {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private TableView artistTable; // make it generic then we can add all data types to the table

    @FXML
    private ProgressBar progressBar;

    @FXML
    public void listArtists() {
        Task<ObservableList<Artist>> task = new GetAllArtistsTask();
        artistTable.itemsProperty().bind(task.valueProperty()); // bind the result to the property
        // progress bar show
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);
        // turn off progress bar whether succeeded or failed
        task.setOnSucceeded(e -> progressBar.setVisible(false));
        task.setOnFailed(e -> progressBar.setVisible(false));
        new Thread(task).start(); // use task to run task in background thread not UI thread
    }

    @FXML
    public void listAlbumsForArtist(){
        final Artist artist = (Artist) artistTable.getSelectionModel().getSelectedItem();
        if(artist == null){
            System.out.println("No artist selected");
            return;
        }
        Task<ObservableList<Album>> task = new Task<ObservableList<Album>>() {
            @Override
            protected ObservableList<Album> call() throws Exception {
                return FXCollections.observableArrayList(Datasource.getInstance().queryAlbumsForArtistId(artist.getId()));
            }
        };
        artistTable.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    @FXML
    public void updateArtist(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Update artist name");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            String newArtistName = controller.processResult();
            final  Artist artist = (Artist) artistTable.getSelectionModel().getSelectedItem();
            // final Artist artist = (Artist) artistTable.getItems().get(2);
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return Datasource.getInstance().updateArtistName(artist.getId(), newArtistName);
                }
            };
            task.setOnSucceeded(e -> {
                if(task.valueProperty().get()){
                    artist.setName(newArtistName);
                    artistTable.refresh();
                }
            });

            new Thread(task).start();
        }
    }
}


// function to get all list on start up and when user requests
class GetAllArtistsTask extends Task{

    // Observable list to make changes update
    @Override
    public ObservableList<Artist> call(){
        return FXCollections.observableArrayList(Datasource.getInstance().queryArtists(Datasource.ORDER_BY_ASC));
    }
}
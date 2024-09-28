/*
Short Description:  This program will display a form to the user who can log into a database, then execute a set
                    of SQL statements - a create table statement, 1000 insert statements, and finally a drop table
                    statement.  The program will compare the execution time between batched statements and non-batched
                    statements.
Author:  Brian Wiatrek
Date:  September 21, 2024
*/
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class BatchPerf extends Application {

    //create the connection variable
    private Connection connection;

    //create the structures used for status reporting
    private Label lblConnectionStatus = new Label("No connection now");
    private TextArea statusArea = new TextArea();

    //create the inputs for logging into the database
    private ComboBox<String> cboURL = new ComboBox<>();
    private ComboBox<String> cboDriver = new ComboBox<>();
    private TextField tfUsername = new TextField();
    private PasswordField pfPassword = new PasswordField();

    //create the buttons to do the actual work
    private Button btConnectDB = new Button("Connect to Database");
    private Button btnBatchUpdate = new Button("Batch Update");
    private Button btnNonBatchUpdate = new Button("Non Batch Update");
    private Button btnClose = new Button("Exit");

    public void start(Stage primaryStage){

        cboURL.getItems().addAll(FXCollections.observableArrayList(
                "jdbc:mysql://127.0.0.1/module6progassign"));
        cboURL.getSelectionModel().selectFirst();
        cboDriver.getItems().addAll(FXCollections.observableArrayList(
                "com.mysql.jdbc.Driver"));
        cboDriver.getSelectionModel().selectFirst();

        //set up the buttons that do the actions
        btConnectDB.setOnAction(e -> connectDB());
        btnBatchUpdate.setOnAction(e -> batchUpdate());
        btnNonBatchUpdate.setOnAction(e -> nonbatchUpdate());
        btnClose.setOnAction(e -> btnClose());

        //create a gridpane for the inputs
        GridPane dbgridPane = new GridPane();

        dbgridPane.add(new Label("JDBC Driver"), 0, 0);
        dbgridPane.add(new Label("Database URL"), 0, 1);
        dbgridPane.add(new Label("Username"), 0, 2);
        dbgridPane.add(new Label("Password"), 0, 3);

        dbgridPane.add(cboURL, 1, 0);
        dbgridPane.add(cboDriver,1,1);
        dbgridPane.add(tfUsername, 1, 2);
        dbgridPane.add(pfPassword,1,3);

        //set up the status reporting
        statusArea.setWrapText(true);
        statusArea.setEditable(false);

        HBox hBoxConnection = new HBox();
        hBoxConnection.getChildren().addAll(btConnectDB, btnBatchUpdate, btnNonBatchUpdate);

        VBox vBoxConnection = new VBox(5);
        vBoxConnection.getChildren().addAll(dbgridPane, hBoxConnection, lblConnectionStatus,
                statusArea, btnClose);

        Scene scene = new Scene(vBoxConnection, 380, 400);
        primaryStage.setTitle("Batch Performance Monitor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //this method connects to the database
    private void connectDB() {
        String driver = cboDriver.getSelectionModel().getSelectedItem();
        String url = cboURL.getSelectionModel().getSelectedItem();
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        try {
            connection = DriverManager.getConnection(url, username, password);
            lblConnectionStatus.setText("Connected to database");
        } catch (java.lang.Exception ex) {
            lblConnectionStatus.setText("Error connecting to database");
            throw new RuntimeException(ex);
        }
    }

    //this method runs the batch updates
    private void batchUpdate() {
        String sqlCreate = "create table temp(num1 double, num2 double, num3 double)";
        String sqlDrop = "drop table temp";
        int count = 0;
        try {
            long startTime = System.nanoTime();
            Statement statement = connection.createStatement();
            statement.addBatch(sqlCreate);
            while (count < 1000) {
                String sqlInsert = "insert into temp (num1, num2, num3) values (" + Math.random() + "," + Math.random() + "," + Math.random() + ")";
                statement.addBatch(sqlInsert);
                count++;
            }
            statement.addBatch(sqlDrop);
            int counter[] = statement.executeBatch();
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;
            statusArea.setText(statusArea.getText() + "Executed batch statements in " + elapsedTime + " nanoseconds\n");
            lblConnectionStatus.setText("Executed batch SQL statements");
        } catch (java.sql.SQLException ex) {
            lblConnectionStatus.setText("Error executing batch SQL statements");
            ex.printStackTrace();
        }

    }

    //this method runs the non-batch updates
    private void nonbatchUpdate() {
        String sqlCreate = "create table temp(num1 double, num2 double, num3 double)";
        String sqlDrop = "drop table temp";
        int count = 0;
        try {
            long startTime = System.nanoTime();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlCreate);
            while (count < 1000) {
                String sqlInsert = "insert into temp (num1, num2, num3) values (" + Math.random() + "," + Math.random() + "," + Math.random() + ")";
                statement.executeUpdate(sqlInsert);
                count++;
            }
            statement.executeUpdate(sqlDrop);
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;
            statusArea.setText(statusArea.getText() + "Executed non-batch statements in " + elapsedTime + " nano seconds\n");
            lblConnectionStatus.setText("Executed non-batch SQL statements");
        } catch (java.lang.Exception ex) {
            lblConnectionStatus.setText("Error executing non-batch SQL statements");
            ex.printStackTrace();
        }
    }

    //this method closes the connection when exiting the program
    private void btnClose() {
        try {
            if (connection != null) connection.close();
        } catch (java.lang.Exception ex) {
            lblConnectionStatus.setText("Error closing DB connection");
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);

    }
}
package Tomasulo;

import java.io.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.cell.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lixiaohan on 6/3/17.
 * UI Controller by dotkrnl on 6/7/17.
 */
public class MainController implements Initializable {

    @FXML private HBox mainHBox;

    @FXML private TableView registersTable;
    @FXML private TableView memoryTable;

    public RuntimeModel model = new RuntimeModel();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: Read from file dialog
        try {
            readInstructionsFromFile("/Users/dotkrnl/ins.txt");
        } catch (FileNotFoundException err) {
            System.err.println("File not found!");
        } catch (IOException err) {
            System.err.println("I/O failed!");
        }
        setMem();

        bindData();
        update();
    }

    private void bindData() {
        bindRegisters();
        bindMemory();
    }

    private void bindRegisters() {
        TableColumn regId = (TableColumn)registersTable.getColumns().get(0);
        TableColumn staId = (TableColumn)registersTable.getColumns().get(1);
        TableColumn data = (TableColumn)registersTable.getColumns().get(2);
        regId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("registerID"));
        staId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("stationID"));
        data.setCellValueFactory(
                new PropertyValueFactory<Register, String>("data"));
        registersTable.setItems(model.registers);
    }

    private void bindMemory() {
        TableColumn addresss = (TableColumn)memoryTable.getColumns().get(0);
        TableColumn data = (TableColumn)memoryTable.getColumns().get(1);
        addresss.setCellValueFactory(
                new PropertyValueFactory<Register, String>("address"));
        data.setCellValueFactory(
                new PropertyValueFactory<Register, String>("data"));
        data.setCellFactory(TextFieldTableCell.forTableColumn());
        data.setOnEditCommit((Event t) -> {
            TableColumn.CellEditEvent<MemoryCell, String> v =
                    (TableColumn.CellEditEvent<MemoryCell, String>)t;
            v.getRowValue().setData(v.getNewValue());
            update();
        });
        memoryTable.setItems(model.memory);
    }

    private void update() {
        registersTable.refresh();
        memoryTable.refresh();
        debug();
    }

    private void readInstructionsFromFile(String filename)
            throws IOException {
        // return false if open fails or operation are unformatted
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        String line;
        while ((line = br.readLine()) != null) {
            String[] insParse = line.split(" ");
            Instruction instruction = new Instruction(insParse[0]);
            instruction.parseArgs(insParse[1].split(","));
            model.instructionQueue.offer(instruction);
        }
    }

    private void setMem(){
        model.memory.get(0).setFloat(10);
        model.memory.get(4).setFloat(6);
        model.memory.get(16).setFloat(8);
    }

    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage)mainHBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void step(ActionEvent event) {
        model.tick();
        update();
    }

    private void debug() {
        System.out.println("== Debugging Registers ==");

        for (Register reg : model.registers) {
            System.out.println(reg.toString());
        }

        System.out.println("== Debugging Reserved stations ==");

        for (ReservedStation station : model.stations) {
            System.out.println(station.toString());
        }
    }
}

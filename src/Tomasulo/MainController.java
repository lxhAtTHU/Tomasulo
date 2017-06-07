package Tomasulo;

import java.io.*;

import javafx.collections.transformation.FilteredList;
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

    @FXML private TableView addStationTable;
    @FXML private TableView mulStationTable;
    @FXML private TableView loadStationTable;
    @FXML private TableView storeStationTable;
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
        bindALUStations(addStationTable,
                RuntimeModel.BASE_ADD_STATION,
                RuntimeModel.BASE_MUL_STATION);
        bindALUStations(mulStationTable,
                RuntimeModel.BASE_MUL_STATION,
                RuntimeModel.BASE_LOAD_STATION);
        bindMemoryStations(loadStationTable,
                RuntimeModel.BASE_LOAD_STATION,
                RuntimeModel.BASE_STORE_STATION);
        bindMemoryStations(storeStationTable,
                RuntimeModel.BASE_STORE_STATION,
                RuntimeModel.BASE_END);
        bindRegisters();
        bindMemory();
    }

    private void bindALUStations(TableView table, int begin, int end) {
        TableColumn staId = (TableColumn)table.getColumns().get(0);
        TableColumn op = (TableColumn)table.getColumns().get(1);
        TableColumn busy = (TableColumn)table.getColumns().get(2);
        TableColumn leftC = (TableColumn)table.getColumns().get(3);
        TableColumn totC = (TableColumn)table.getColumns().get(4);
        TableColumn v1 = (TableColumn)table.getColumns().get(5);
        TableColumn v2 = (TableColumn)table.getColumns().get(6);
        staId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("stationId"));
        op.setCellValueFactory(
                new PropertyValueFactory<Register, String>("operation"));
        busy.setCellValueFactory(
                new PropertyValueFactory<Register, String>("busy"));
        leftC.setCellValueFactory(
                new PropertyValueFactory<Register, String>("circleLeft"));
        totC.setCellValueFactory(
                new PropertyValueFactory<Register, String>("circleTotalNeed"));
        v1.setCellValueFactory(
                new PropertyValueFactory<Register, String>("value1"));
        v2.setCellValueFactory(
                new PropertyValueFactory<Register, String>("value2"));
        FilteredList<ReservedStation> filteredData =
                new FilteredList<>(model.stations,
                        p -> p.staId >= begin && p.staId < end);
        table.setItems(filteredData);
    }


    private void bindMemoryStations(TableView table, int begin, int end) {
        TableColumn staId = (TableColumn)table.getColumns().get(0);
        TableColumn busy = (TableColumn)table.getColumns().get(1);
        TableColumn address = (TableColumn)table.getColumns().get(2);
        TableColumn v1 = (TableColumn)table.getColumns().get(3);
        staId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("stationId"));
        busy.setCellValueFactory(
                new PropertyValueFactory<Register, String>("busy"));
        address.setCellValueFactory(
                new PropertyValueFactory<Register, String>("address"));
        v1.setCellValueFactory(
                new PropertyValueFactory<Register, String>("value1"));
        FilteredList<ReservedStation> filteredData =
                new FilteredList<>(model.stations,
                        p -> p.staId >= begin && p.staId < end);
        table.setItems(filteredData);
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
        addStationTable.refresh();
        mulStationTable.refresh();
        loadStationTable.refresh();
        storeStationTable.refresh();
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

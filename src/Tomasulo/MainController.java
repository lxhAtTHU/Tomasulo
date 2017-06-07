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
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by lixiaohan on 6/3/17.
 * UI Controller by dotkrnl on 6/7/17.
 */
public class MainController implements Initializable {

    @FXML private HBox mainHBox;
    @FXML private ProgressBar clockBar;

    @FXML private TableView instructionTable;
    @FXML private TableView addStationTable;
    @FXML private TableView mulStationTable;
    @FXML private TableView loadStationTable;
    @FXML private TableView storeStationTable;
    @FXML private TableView registersTable;
    @FXML private TableView memoryTable;

    private RuntimeModel model = null;
    private int totalSteps = -1;
    private ArrayList<Instruction> instructions = new ArrayList<>();

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
        start();
    }

    public void start() {
        model = new RuntimeModel();
        model.addInstructions(instructions);
        totalSteps = guessStep();

        setMem(); // TODO: read from memory file
        bindData();
        update();
    }

    private void bindData() {
        bindInstructionTable();
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

    private void bindInstructionTable() {
        String[] fields = {"operation", "destination", "source",
                "emit", "done", "writeBack"};
        bindTable(fields, instructionTable);
        instructionTable.setItems(model.instructions);
    }

    private void bindALUStations(TableView table, int begin, int end) {
        String[] fields = {"stationId", "operation", "busy",
                "circleLeft", "circleTotalNeed", "value1", "value2"};
        bindTable(fields, table);
        FilteredList<ReservedStation> filteredData =
                new FilteredList<>(model.stations,
                        p -> p.staId >= begin && p.staId < end);
        table.setItems(filteredData);
    }


    private void bindMemoryStations(TableView table, int begin, int end) {
        String[] fields = {"stationId", "busy", "circleLeft", "circleTotalNeed",
                "address", "value1"};
        bindTable(fields, table);
        FilteredList<ReservedStation> filteredData =
                new FilteredList<>(model.stations,
                        p -> p.staId >= begin && p.staId < end);
        table.setItems(filteredData);
    }

    private void bindRegisters() {
        String[] fields = {"registerID", "stationID", "data"};
        bindTable(fields, registersTable);
        registersTable.setItems(model.registers);
    }

    private void bindMemory() {
        String[] fields = {"address", "data"};
        TableColumn data = (TableColumn)memoryTable.getColumns().get(1);
        bindTable(fields, memoryTable);
        data.setCellFactory(TextFieldTableCell.forTableColumn());
        data.setOnEditCommit((Event t) -> {
            TableColumn.CellEditEvent<MemoryCell, String> v =
                    (TableColumn.CellEditEvent<MemoryCell, String>)t;
            v.getRowValue().setData(v.getNewValue());
            update();
        });
        memoryTable.setItems(model.memory);
    }

    private void bindTable(String[] fields, TableView table) {
        for (int i = 0; i < fields.length; i++) {
            TableColumn col = (TableColumn)table.getColumns().get(i);
            col.setCellValueFactory(new PropertyValueFactory
                    <Instruction, String>(fields[i]));
        }
    }

    private void update() {
        instructionTable.refresh();
        addStationTable.refresh();
        mulStationTable.refresh();
        loadStationTable.refresh();
        storeStationTable.refresh();
        registersTable.refresh();
        memoryTable.refresh();
        clockBar.setProgress((double)model.clock / totalSteps);
        debug();
    }

    private void readInstructionsFromFile(String filename)
            throws IOException {
        instructions.clear();
        // return false if open fails or operation are unformatted
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        String line;
        while ((line = br.readLine()) != null) {
            String[] insParse = line.split(" ");
            Instruction instruction = new Instruction(insParse[0]);
            instruction.parseArgs(insParse[1].split(","));
            instructions.add(instruction);
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

    private int guessStep() {
        int steps = 0;
        RuntimeModel model = new RuntimeModel();
        model.addInstructions(instructions);
        while (!model.finished()) {
            steps += 1;
            model.tick();
        }
        return steps;
    }

    private void debug() {
        System.out.println("== Debugging Instructions ==");
        for (Instruction instruction : model.instructions) {
            System.out.println(instruction.toString());
        }

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

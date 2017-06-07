package Tomasulo;

import java.io.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.cell.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.util.Duration;

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
    @FXML private Menu clockLabel;
    @FXML private Button startButton;
    @FXML private Button stepButton;
    @FXML private Button playButton;

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

    private Timeline playing;
    private boolean isPlaying = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playing = new Timeline(new KeyFrame(Duration.millis(500), ae -> {
            if (model.finished()) togglePlay();
            step(null);
        }));
        playing.setCycleCount(Animation.INDEFINITE);
        update();
    }

    public void start() {
        model = new RuntimeModel();
        model.addInstructions(instructions);
        totalSteps = guessStep();

        playing.stop();
        isPlaying = false;

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

        boolean noData = instructions.isEmpty();

        if (!noData) {
            clockBar.setProgress((double) model.clock / totalSteps);
            clockLabel.setText("第 " + model.clock + " 步");
        }

        if (isPlaying) {
            playButton.setText("暂停");
        } else {
            playButton.setText("播放");
        }

        if (noData) {
            startButton.setDisable(true);
        } else {
            startButton.setDisable(false);
        }

        if (noData || model.finished()) {
            playButton.setDisable(true);
            stepButton.setDisable(true);
        } else {
            playButton.setDisable(false);
            stepButton.setDisable(false);
        }
        //debug();
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

    private void setMem() {
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
        if (model != null && !model.finished())
            model.tick();
        update();
    }

    @FXML
    private void play(ActionEvent event) {
        togglePlay();
    }

    @FXML
    private void openInstruction(ActionEvent event) {
        Stage stage = (Stage)mainHBox.getScene().getWindow();
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择指令文件");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                readInstructionsFromFile(file.getPath());
                start();
            }
        } catch (FileNotFoundException err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件未找到！");
            alert.setHeaderText("加载失败！");
            alert.setContentText("奇怪，你的文件不见了。");
            alert.showAndWait();
        } catch (IOException err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("文件读取失败！");
            alert.setHeaderText("加载失败！");
            alert.setContentText("奇怪，你的文件读不出来。");
            alert.showAndWait();
        } catch (IllegalArgumentException err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("不是有效文件！");
            alert.setHeaderText("加载失败！");
            alert.setContentText("你的文件好像不是有效指令文件呢。");
            alert.showAndWait();
        }
    }

    private void togglePlay() {
        if (isPlaying) {
            playing.stop();
            isPlaying = false;
        } else {
            playing.play();
            isPlaying = true;
        }
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

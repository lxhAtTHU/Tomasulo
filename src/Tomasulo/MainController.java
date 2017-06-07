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
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * Created by lixiaohan on 6/3/17.
 * UI by dotkrnl on 6/7/17
 */
public class MainController implements Initializable {

    @FXML private HBox mainHBox;
    @FXML private MenuItem menuCloseItem;

    @FXML private TableView registersTable;
    @FXML private TableView memoryTable;

    public RuntimeModel model = new RuntimeModel();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindRegisters();
        bindMemory();

        init();
        boolean read = readInsFromFile("/Users/dotkrnl/ins.txt");
        if (!read) return;
        setMem(); // for testing
        update();
    }

    private void bindRegisters() {
        TableColumn regId = (TableColumn)registersTable.getColumns().get(0);
        TableColumn staId = (TableColumn)registersTable.getColumns().get(1);
        TableColumn data = (TableColumn)registersTable.getColumns().get(2);
        regId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("regID"));
        staId.setCellValueFactory(
                new PropertyValueFactory<Register, String>("stationID"));
        data.setCellValueFactory(
                new PropertyValueFactory<Register, String>("data"));
        registersTable.setItems(model.registers);
    }

    private void bindMemory() {
        TableColumn addr = (TableColumn)memoryTable.getColumns().get(0);
        TableColumn data = (TableColumn)memoryTable.getColumns().get(1);
        addr.setCellValueFactory(
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

    private void debug() {
        System.out.println("== Debugging Registers ==");

        for (Register reg : model.registers) {
            System.out.println(
                    reg.getRegID() + ": Station = " + reg.getStationID()
                    + ", Data = " + reg.getData());
        }

        System.out.println("== Debugging Reserved stations ==");

        for(int i=0; i<model.stations.length; i++){
            String x = String.valueOf(i)+": "
                    + String.valueOf(model.stations[i].is_busy)+" "
                    + String.valueOf(model.stations[i].ins) +" "
                    + String.valueOf(model.stations[i].circle_left) +" "
                    + String.valueOf(model.stations[i].circle_total_need);
            System.out.println(x);
        }
    }

    private void init(){
        model.ins_queue = new LinkedList<>();
        // dequeue:poll()  get head ele:peek()
        for(int i=0; i<model.stations.length; i++){
            model.stations[i] = new ReservedStation();
        }
    }
    private boolean readInsFromFile(String filename){
        // return false if open fails or ins are unformatted
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            String line = "";
            while((line=br.readLine()) != null){
                String[] ins = line.split(" ");
                String[] ops = ins[1].split(",");
                if(ins[0].equals(model.add) || ins[0].equals(model.sub) || ins[0].equals(model.multi) || ins[0].equals(model.div)){
                    int dst = Integer.parseInt(ops[0].replace("F", ""));
                    int op1 = Integer.parseInt(ops[1].replace("F", ""));
                    int op2 = Integer.parseInt(ops[2].replace("F", ""));
                    Instruction instruction = new Instruction(ins[0], dst, op1, op2);
                    model.ins_queue.offer(instruction);
                }
                else if(ins[0].equals(model.load) ){
                    int reg = Integer.parseInt(ops[0].replace("F", ""));
                    int addr = Integer.parseInt(ops[1]);
                    Instruction instruction = new Instruction(ins[0], reg, 0, 0);
                    instruction.addr = addr;
                    model.ins_queue.offer(instruction);
                }
                else if(ins[0].equals(model.store)){
                    int src_reg = Integer.parseInt(ops[0].replace("F", ""));
                    int addr = Integer.parseInt(ops[1]);
                    Instruction instruction = new Instruction(ins[0], 0, src_reg, 0);
                    instruction.addr = addr;
                    model.ins_queue.offer(instruction);
                }
                else {
                    return false;
                }
            }
        } catch (IOException e){
            System.out.println("IO error");
            return false;
        }
        return true;
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
    private void run(ActionEvent event) {
        model.tick();
        update();
    }
}

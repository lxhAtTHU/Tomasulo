package Tomasulo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/7/17
 * Modified by liyr on 6/8/17
 */
public class RuntimeModel {

    static public int NUM_ADD_STATION = 3, NUM_MUL_STATION = 2;
    static public int NUM_LOAD_STATION = 3, NUM_STORE_STATION = 3;

    static public int NUM_REGISTER = 16, NUM_MEMORY = 4096;

    public int clock = 0;

    public ObservableList<Instruction> instructions =
            FXCollections.observableArrayList();
    public Queue<Instruction> instructionQueue =
            new LinkedList<>();
    public ObservableList<Register> registers =
            FXCollections.observableArrayList();
    public ObservableList<MemoryCell> memory =
            FXCollections.observableArrayList();

    public ObservableList<ReservedStation> stations =
            FXCollections.observableArrayList();

    // For ALU ID see ID_*_STATION
    public int[] aluWorkingOn = {-1, -1, -1, -1};

    public RuntimeModel() {
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < NUM_REGISTER; i++)
            registers.add(new Register(i));
        for (int i = 0; i < NUM_MEMORY; i++)
            memory.add(new MemoryCell(i));
        for (int i = 0; i < NUM_STATIONS; i++)
            stations.add(new ReservedStation(i));
    }

    public void tick() {
        clock++;

        Instruction instruction = instructionQueue.peek();
        if (instruction != null) emitInstruction(instruction);
        // return STALL_STATION if no empty station

        // update ALU infomation. if finished, inform those waiting
        updateALU(BASE_ADD_STATION, NUM_ADD_STATION, ID_ADD_STATION);
        updateALU(BASE_MUL_STATION, NUM_MUL_STATION, ID_MUL_STATION);
        updateALU(BASE_LOAD_STATION, NUM_LOAD_STATION, ID_LOAD_STATION);
        updateALU(BASE_STORE_STATION, NUM_STORE_STATION, ID_STORE_STATION);

        startNewWork(BASE_ADD_STATION, NUM_ADD_STATION, ID_ADD_STATION);
        startNewWork(BASE_MUL_STATION, NUM_MUL_STATION, ID_MUL_STATION);
        startNewWork(BASE_LOAD_STATION, NUM_LOAD_STATION, ID_LOAD_STATION);
        startNewWork(BASE_STORE_STATION, NUM_STORE_STATION, ID_STORE_STATION);
    }

    public boolean finished() {
        for (Instruction instruction : instructions) {
            if (!instruction.writeback) return false;
        }
        return true;
    }

    public void addInstructions(ArrayList<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            addInstruction(instruction);
        }
    }

    public void addInstruction(Instruction instruction) {
        Instruction cloned = instruction.clone();
        instructions.add(cloned);
        instructionQueue.offer(cloned);
    }

    private int emitInstruction(Instruction nextInstruction) {
        switch (nextInstruction.operation) {
            case ADDD: case SUBD:
                return tryStation(BASE_ADD_STATION, NUM_ADD_STATION);
            case MULTD: case DIVD:
                return tryStation(BASE_MUL_STATION, NUM_MUL_STATION);
            case LD:
                if (hasAddressInMemBuffer(nextInstruction.addr))
                    return STALL_STATION;
                return tryStation(BASE_LOAD_STATION, NUM_LOAD_STATION);
            case ST:
                if (hasAddressInMemBuffer(nextInstruction.addr))
                    return STALL_STATION;
                return tryStation(BASE_STORE_STATION, NUM_STORE_STATION);
            default:
                return STALL_STATION;
        }
    }

    private int tryStation(int base, int num) {
        for (int idx = base; idx < (base + num); idx++) {
            ReservedStation station = stations.get(idx);
            if (station.instruction == null) {
                // Station available, emit to this station
                Instruction nextInstruction = instructionQueue.poll();
                addInstructionToStation(idx, nextInstruction);
                nextInstruction.emit = true;
                return idx;  // Use this station
            }
        }
        return STALL_STATION;
    }

    private boolean hasAddressInMemBuffer(int address) {
        for (int i = BASE_LOAD_STATION; i < BASE_END; i++) {
            ReservedStation station = stations.get(i);
            if (station.instruction == null &&
                    address == station.address) {
                return true;
            }
        }
        return false;
    }

    private void addInstructionToStation(int idx, Instruction instruction) {
        ReservedStation station = stations.get(idx);
        station.instruction = instruction;

        switch (instruction.operation) {
            case LD:
                stations.get(idx).address = instruction.addr;
                configureLoadStation(idx, instruction);
                break;
            case ST:
                stations.get(idx).address = instruction.addr;
                configureStoreStation(idx, instruction);
                break;
            default:
                configureCalcStation(idx, instruction);
        }
    }

    private void configureLoadStation(int idx, Instruction instruction) {
        // set waiting and register
        registers.get(instruction.dstRegId).resStaId = idx;
        stations.get(idx).regWaited.add(instruction.dstRegId);
    }

    private void configureStoreStation(int idx, Instruction instruction) {
        int srcReg = instruction.op1RegId;
        // operation in res_sta[idx] needs data from reg[srcReg]
        // check if data need is ready.
        // if ready, set v
        // else set r and register in station and set isBusy
        if (registers.get(srcReg).resStaId == -1) {
            stations.get(idx).v1 = registers.get(srcReg).data;
            stations.get(idx).r1 = -1;
        } else {
            stations.get(idx).r1 = registers.get(srcReg).resStaId;
            stations.get(registers.get(srcReg).resStaId)
                    .resStaWaited.add(idx);
            stations.get(idx).isBusy = true;
        }
    }

    private void configureCalcStation(int idx, Instruction instruction) {
        // Three args.

        // Set srcReg check if waiting for data from reg
        int srcReg = instruction.op1RegId;
        if (registers.get(srcReg).resStaId == -1) {
            stations.get(idx).v1 = registers.get(srcReg).data;
            stations.get(idx).r1 = -1;
        } else {
            stations.get(idx).r1 = registers.get(srcReg).resStaId;
            stations.get(registers.get(srcReg).resStaId)
                    .resStaWaited.add(idx);
            stations.get(idx).isBusy = true;
        }

        srcReg = instruction.op2RegId;
        if (registers.get(srcReg).resStaId == -1) {
            stations.get(idx).v2 = registers.get(srcReg).data;
            stations.get(idx).r2 = -1;
        } else {
            stations.get(idx).r2 = registers.get(srcReg).resStaId;
            stations.get(registers.get(srcReg).resStaId)
                    .resStaWaited.add(idx);
            stations.get(idx).isBusy = true;
        }

        if (stations.get(idx).r1 + stations.get(idx).r2 == -2)
            stations.get(idx).isBusy = false;

        // Set dstReg waiting and register
        registers.get(instruction.dstRegId).resStaId = idx;
        stations.get(idx).regWaited.add(instruction.dstRegId);
    }

    private void updateALU(int base, int num, int aluIndex) {
        int currExec = aluWorkingOn[aluIndex];
        if (currExec == -1) return;

        ReservedStation station = stations.get(currExec);

        station.circleLeft -= 1;

        // ALU calculation finished
        if (station.circleLeft == 0) {
            station.instruction.done = true;
        }

        // Writeback finished
        if (station.circleLeft == -1) {
            // Inform who need result
            float res = operation(station);
            inform(res, currExec);
            station.instruction.writeback = true;
            // Reset station & ALU status
            stations.set(currExec, new ReservedStation(currExec));
            aluWorkingOn[aluIndex] = -1;
        }
    }

    private float operation(ReservedStation rs) {
        switch (rs.instruction.operation) {
            case ADDD: return rs.v1 + rs.v2;
            case SUBD: return rs.v1 - rs.v2;
            case MULTD: return rs.v1 * rs.v2;
            case DIVD: return rs.v1 / rs.v2;
            case LD: return memory.get(rs.address).getFloat();
            case ST: memory.get(rs.address).setFloat(rs.v1); return 0;
            default: return -1;
        }
    }

    private void inform(float res, int rs_index) {
        // stations[rs_index] has a result res
        ReservedStation rs = stations.get(rs_index);
        for (Integer i : rs.regWaited) {
            if (registers.get(i).resStaId == rs_index) {
                registers.get(i).data = res;
                registers.get(i).resStaId = -1;
            }
        }
        for (Integer i : rs.resStaWaited) {
            if (stations.get(i).r1 == rs_index) {
                stations.get(i).v1 = res;
                stations.get(i).r1 = -1;
            }
            if (stations.get(i).r2 == rs_index) {
                stations.get(i).v2 = res;
                stations.get(i).r2 = -1;
            }
            if (stations.get(i).instruction.operation ==
                    Instruction.Operation.ST) {
                if (stations.get(i).r1 == -1) {
                    stations.get(i).isBusy = false;
                }
            } else if (stations.get(i).r1 + stations.get(i).r2 == -2) {
                stations.get(i).isBusy = false;
            }
        }
    }

    private void startNewWork(int base, int num, int alu_index) {
        if (aluWorkingOn[alu_index] != -1)
            return;  // still working

        int waited = -1, ind = -1;
        for (int i = base; i < base + num; i++) {
            ReservedStation rs = stations.get(i);
            if (rs.instruction != null && !rs.isBusy && rs.resStaWaited.size() > waited) {
                // can be executed
                ind = i;
                waited = rs.resStaWaited.size();
            }
        }
        if(ind != -1) {
            ReservedStation rs = stations.get(ind);
            rs.isBusy = true;
            rs.circleLeft = rs.instruction.getCycle();
            aluWorkingOn[alu_index] = ind;
            return;
        }
        // no operation executed
    }

    // Utils calculations
    static public int NUM_STATIONS = NUM_ADD_STATION + NUM_MUL_STATION +
            NUM_LOAD_STATION + NUM_STORE_STATION;

    static public int BASE_ADD_STATION = 0;
    static public int BASE_MUL_STATION = BASE_ADD_STATION + NUM_ADD_STATION;
    static public int BASE_LOAD_STATION = BASE_MUL_STATION + NUM_MUL_STATION;
    static public int BASE_STORE_STATION = BASE_LOAD_STATION + NUM_STORE_STATION;
    static public int BASE_END = BASE_STORE_STATION + NUM_STORE_STATION;

    static public int ID_ADD_STATION = 0, ID_MUL_STATION = 1;
    static public int ID_LOAD_STATION = 2, ID_STORE_STATION = 3;

    static final int STALL_STATION = -1;

}

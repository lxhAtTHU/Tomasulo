package Tomasulo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Queue;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/7/17
 */
public class RuntimeModel {

    static public String add = "ADDD", sub = "SUBD", multi = "MULTD",
            div = "DIVD", load = "LD", store = "ST";
    static public int num_a = 3, num_m = 2, num_l = 3, num_s = 3, num_reg =
            16, num_mem = 4096;
    static public int base_a = 0, base_m = 3, base_l = 5, base_s = 8;

    public Queue<Instruction> ins_queue;
    public ObservableList<Register> registers =
            FXCollections.observableArrayList();
    public ObservableList<MemoryCell> memory =
            FXCollections.observableArrayList();

    public ReservedStation[] stations =
            new ReservedStation[num_a + num_m + num_l + num_s];

    public int clock = 0;
    public int[] alu_exec = {-1, -1, -1, -1}; // a, m, l, s

    public RuntimeModel() {
        for (int i = 0; i < num_reg; i++) {
            registers.add(new Register(i));
        }
        for (int i = 0; i < num_mem; i++) {
            memory.add(new MemoryCell(i));
        }
    }

    private int checkResStations(Instruction next_ins) {
        if (next_ins.ins.equals(add) || next_ins.ins.equals(sub)) {
            return tryResStation(base_a, num_a);
        } else if (next_ins.ins.equals(multi) || next_ins.ins.equals(div)) {
            return tryResStation(base_m, num_m);
        } else if (next_ins.ins.equals(load)) {
            if (hasAddrInMemBuffer(next_ins.addr))
                return -1;
            return tryResStation(base_l, num_l);
        } else if (next_ins.ins.equals(store)) {
            if (hasAddrInMemBuffer(next_ins.addr))
                return -1;
            return tryResStation(base_s, num_s);
        } else {
            return -1;
        }
    }

    public void tick() {
        clock++;
        // fetch ins
        Instruction next_ins = ins_queue.peek();
        int station_id;
        if (next_ins != null)
            station_id = checkResStations(next_ins); // return -1 if no empty station
        // update alu infomation. if finished, inform those waiting
        updateALU(base_a, num_a, 0);
        updateALU(base_m, num_m, 1);
        updateALU(base_l, num_l, 2);
        updateALU(base_s, num_s, 3);
        startNewWork(base_a, num_a, 0);
        startNewWork(base_m, num_m, 1);
        startNewWork(base_l, num_l, 2);
        startNewWork(base_s, num_s, 3);
    }

    private boolean hasAddrInMemBuffer(int addr) {
        for (int i = base_l; i < base_l + num_l + num_s; i++) {
            if (!stations[i].ins.equals("") && (addr == stations[i].addr))
                return true;
        }
        return false;
    }

    private int tryResStation(int base, int num) {
        for (int i = base; i < (base + num); i++) {
            if (stations[i].ins.equals("")) {
                // empty
                Instruction next_ins = ins_queue.poll();
                addIns(i, next_ins);
                return i;
            }
        }
        return -1;
    }

    public void addIns(int rs, Instruction instruction) {
        stations[rs].ins = instruction.ins;
        stations[rs].addr = instruction.addr;

        if (stations[rs].ins.equals(load)) {
            // set waiting and register
            registers.get(instruction.dst_reg_id).res_sta_id = rs;
            stations[rs].reg_waited.add(instruction.dst_reg_id);
            stations[rs].is_busy = false;
        } else if (stations[rs].ins.equals(store)) {
            int src_reg = instruction.op1_reg_id;
            // ins in res_sta[rs] needs data from reg[src_reg]
            // check if data need is ready.
            // if ready, set v
            // else set r and register in station and set is_busy
            if (registers.get(src_reg).res_sta_id == -1) {
                stations[rs].v1 = registers.get(src_reg).data;
                stations[rs].r1 = -1;
                stations[rs].is_busy = false;
            } else {
                stations[rs].r1 = registers.get(src_reg).res_sta_id;
                stations[registers.get(src_reg).res_sta_id].res_sta_waited
                        .add(rs);
                stations[rs].is_busy = true;
            }
        } else {
            // three ops
            // set dst reg waiting and register
            registers.get(instruction.dst_reg_id).res_sta_id = rs;
            stations[rs].reg_waited.add(instruction.dst_reg_id);
            // check if waiting for data from reg
            int src_reg = instruction.op1_reg_id;
            if (registers.get(src_reg).res_sta_id == -1) {
                stations[rs].v1 = registers.get(src_reg).data;
                stations[rs].r1 = -1;
            } else {
                stations[rs].r1 = registers.get(src_reg).res_sta_id;
                stations[registers.get(src_reg).res_sta_id].res_sta_waited
                        .add(rs);
                stations[rs].is_busy = true;
            }

            src_reg = instruction.op2_reg_id;
            if (registers.get(src_reg).res_sta_id == -1) {
                stations[rs].v2 = registers.get(src_reg).data;
                stations[rs].r2 = -1;
            } else {
                stations[rs].r2 = registers.get(src_reg).res_sta_id;
                stations[registers.get(src_reg).res_sta_id].res_sta_waited
                        .add(rs);
                stations[rs].is_busy = true;
            }
            if (stations[rs].r1 + stations[rs].r2 == -2)
                stations[rs].is_busy = false;
        }
        setTotalCircle(rs);
    }

    private void setTotalCircle(int id) {
        ReservedStation rs = stations[id];
        if (rs.ins.equals(add) || rs.ins.equals(sub) || rs.ins.equals(load) || rs.ins.equals(store)) {
            rs.circle_total_need = 2;
        } else if (rs.ins.equals(multi)) {
            rs.circle_total_need = 10;
        } else rs.circle_total_need = 40;
    }

    private void updateALU(int base, int num, int alu_index) {
        int curr_exec = alu_exec[alu_index];
        if (curr_exec != -1) {
            stations[curr_exec].circle_left -= 1;

            if (stations[curr_exec].circle_left == -1) {
                // not 0 because 1 circle is needed for writing back
                // alu finished
                // inform who need result
                float res = operation(stations[curr_exec]);
                inform(res, curr_exec);
                // update variable
                stations[curr_exec].ins = "";
                stations[curr_exec].is_busy = false;
                alu_exec[alu_index] = -1;
            }
        }
    }

    private float operation(ReservedStation rs) {
        if (rs.ins.equals(add)) {
            return rs.v1 + rs.v2;
        } else if (rs.ins.equals(sub)) {
            return rs.v1 - rs.v2;
        } else if (rs.ins.equals(multi)) {
            return rs.v1 * rs.v2;
        } else if (rs.ins.equals(div)) {
            return rs.v1 / rs.v2;
        } else if (rs.ins.equals(load)) {
            return memory.get(rs.addr).getFloat();
        } else if (rs.ins.equals(store)) {
            memory.get(rs.addr).setFloat(rs.v1);
            return 0;
        } else {
            return -1;
        }
    }

    private void inform(float res, int rs_index) {
        // stations[rs_index] has a result res
        ReservedStation rs = stations[rs_index];
        for (Integer i : rs.reg_waited) {
            if (registers.get(i).res_sta_id == rs_index) {
                registers.get(i).data = res;
                registers.get(i).res_sta_id = -1;
            }
        }
        for (Integer i : rs.res_sta_waited) {
            if (stations[i].r1 == rs_index) {
                stations[i].v1 = res;
                stations[i].r1 = -1;
            }
            if (stations[i].r2 == rs_index) {
                stations[i].v2 = res;
                stations[i].r2 = -1;
            }
            if (stations[i].ins.equals(store)) {
                if (stations[i].r1 == -1) {
                    stations[i].is_busy = false;
                }
            } else if (stations[i].r1 + stations[i].r2 == -2) {
                stations[i].is_busy = false;
            }
        }
    }

    private void startNewWork(int base, int num, int alu_index) {
        if (alu_exec[alu_index] != -1)
            return;
        for (int i = base; i < base + num; i++) {
            ReservedStation rs = stations[i];
            if (!rs.ins.equals("") && !rs.is_busy) {
                // can be executed
                rs.is_busy = true;
                rs.circle_left = rs.circle_total_need;
                alu_exec[alu_index] = i;
                return;
            }
        }
        // no ins executed
        alu_exec[alu_index] = -1;
        return;
    }

}

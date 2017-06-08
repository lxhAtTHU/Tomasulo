/**
 * Created by lixiaohan on 2017/6/3.
 */
import javax.sound.sampled.ReverbType;
import java.math.*;
import java.io.*;
import java.util.*;


public class Tomasulo {

    static public String add="ADDD", sub="SUBD", multi="MULTD", div="DIVD", load="LD", store="ST";

    public Queue<Instruction> ins_queue;
    public Register[] registers = new Register[16];
    static public int num_a=3, num_m=2, num_l=3, num_s=3;
    static public int base_a=0, base_m=3, base_l=5, base_s=8;
    public ReservedStation[] stations = new ReservedStation[num_a+num_m+num_l+num_s];

    public float[] mem = new float[4096];
    public int clock = 0;
    public int[] alu_exec = {-1, -1, -1, -1}; // a, m, l, s

    public static void main(String[] agrs)
    throws Exception
    {
        Tomasulo tomasulo = new Tomasulo();
    }
    public Tomasulo()
    throws Exception
    {
        // main func starts here
        init();
        boolean read = readInsFromFile("ins.txt");
        if(!read){
            info("Error in reading ins from file");
            return;
        }
        setMem(); // for testing
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(br.readLine().length()==0){
            clock ++;
            info("clock: " + String.valueOf(clock));
            // fetch ins
            Instruction next_ins = ins_queue.peek();
            int station_id;
            if(next_ins != null)
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
            info();
            // update UI
        }
    }


    private void info(String x){
        // encapsulate print function
        System.out.println(x);
    }
    private void info(int x){
        // encapsulate print function
        System.out.println(x);
    }
    private void info(float x){
        // encapsulate print function
        System.out.println(x);
    }
    private void info(){
        // registers
        for(int i=0; i<registers.length; i++){
            String x = String.valueOf(i)+": "
                    + String.valueOf(registers[i].res_sta_id) +" "
                    + String.valueOf(registers[i].data);
            info(x);
        }
        // reserved stations
        for(int i=0; i<stations.length; i++){
            String x = String.valueOf(i)+": "
                    + String.valueOf(stations[i].is_busy)+" "
                    + String.valueOf(stations[i].ins) +" "
                    + String.valueOf(stations[i].circle_left) +" "
                    + String.valueOf(stations[i].circle_total_need);
            info(x);
        }
    }
    private void init(){
        ins_queue = new LinkedList<>();
        // dequeue:poll()  get head ele:peek()
        for(int i=0; i<registers.length; i++){
            registers[i] = new Register();
        }
        for(int i=0; i<stations.length; i++){
            stations[i] = new ReservedStation();
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
                if(ins[0].equals(add) || ins[0].equals(sub) || ins[0].equals(multi) || ins[0].equals(div)){
                    int dst = Integer.parseInt(ops[0].replace("F", ""));
                    int op1 = Integer.parseInt(ops[1].replace("F", ""));
                    int op2 = Integer.parseInt(ops[2].replace("F", ""));
                    Instruction instruction = new Instruction(ins[0], dst, op1, op2);
                    ins_queue.offer(instruction);
                }
                else if(ins[0].equals(load) ){
                    int reg = Integer.parseInt(ops[0].replace("F", ""));
                    int addr = Integer.parseInt(ops[1]);
                    Instruction instruction = new Instruction(ins[0], reg, 0, 0);
                    instruction.addr = addr;
                    ins_queue.offer(instruction);
                }
                else if(ins[0].equals(store)){
                    int src_reg = Integer.parseInt(ops[0].replace("F", ""));
                    int addr = Integer.parseInt(ops[1]);
                    Instruction instruction = new Instruction(ins[0], 0, src_reg, 0);
                    instruction.addr = addr;
                    ins_queue.offer(instruction);
                }
                else {
                    return false;
                }
            }
        }catch (IOException e){
            info("IO error");
            return false;
        }
        return true;
    }
    private void setMem(){
        mem[0] = 10;
        mem[4] = 6;
        mem[16] = 8;
    }
    private int checkResStations(Instruction next_ins) {
        if (next_ins.ins.equals(add) || next_ins.ins.equals(sub)) {
            return tryResStation(base_a, num_a);
        } else if (next_ins.ins.equals(multi) || next_ins.ins.equals(div)) {
            return tryResStation(base_m, num_m);
        } else if (next_ins.ins.equals(load)) {
            if(hasAddrInMemBuffer(next_ins.addr))
                return -1;
            return tryResStation(base_l, num_l);
        } else if (next_ins.ins.equals(store)) {
            if(hasAddrInMemBuffer(next_ins.addr))
                return -1;
            return tryResStation(base_s, num_s);
        } else {
            return -1;
        }
    }
    private boolean hasAddrInMemBuffer(int addr){
        for(int i=base_l; i<base_l+num_l+num_s; i++){
            if(!stations[i].ins.equals("") && (addr == stations[i].addr))
                return true;
        }
        return false;
    }
    private int tryResStation(int base, int num){
        for(int i=base; i<(base+num); i++){
            if(stations[i].ins.equals("")){
                // empty
                Instruction next_ins = ins_queue.poll();
                addIns(i, next_ins);
                return i;
            }
        }
        return -1;
    }
    public void addIns(int rs, Instruction instruction){
        stations[rs].ins = instruction.ins;
        stations[rs].addr = instruction.addr;

        if(stations[rs].ins.equals(load)){
            // set waiting and register
            registers[instruction.dst_reg_id].res_sta_id = rs;
            stations[rs].reg_waited.add(instruction.dst_reg_id);
            stations[rs].is_busy = false;
        }
        else if(stations[rs].ins.equals(store)){
            int src_reg = instruction.op1_reg_id;
            // ins in res_sta[rs] needs data from reg[src_reg]
            // check if data need is ready.
            // if ready, set v
            // else set r and register in station and set is_busy
            if(registers[src_reg].res_sta_id == -1){
                stations[rs].v1 = registers[src_reg].data;
                stations[rs].r1 = -1;
                stations[rs].is_busy = false;
            }else{
                stations[rs].r1 = registers[src_reg].res_sta_id;
                stations[registers[src_reg].res_sta_id].res_sta_waited.add(rs);
                stations[rs].is_busy = true;
            }
        }
        else{
            // three ops
            // set dst reg waiting and register
            registers[instruction.dst_reg_id].res_sta_id = rs;
            stations[rs].reg_waited.add(instruction.dst_reg_id);
            // check if waiting for data from reg
            int src_reg = instruction.op1_reg_id;
            if(registers[src_reg].res_sta_id == -1){
                stations[rs].v1 = registers[src_reg].data;
                stations[rs].r1 = -1;
            }else{
                stations[rs].r1 = registers[src_reg].res_sta_id;
                stations[registers[src_reg].res_sta_id].res_sta_waited.add(rs);
                stations[rs].is_busy = true;
            }

            src_reg = instruction.op2_reg_id;
            if(registers[src_reg].res_sta_id == -1){
                stations[rs].v2 = registers[src_reg].data;
                stations[rs].r2 = -1;
            }else{
                stations[rs].r2 = registers[src_reg].res_sta_id;
                stations[registers[src_reg].res_sta_id].res_sta_waited.add(rs);
                stations[rs].is_busy = true;
            }
            if(stations[rs].r1+stations[rs].r2 == -2)
                stations[rs].is_busy = false;
        }
        setTotalCircle(rs);
    }
    private void setTotalCircle(int id){
        ReservedStation rs = stations[id];
        if(rs.ins.equals(add) || rs.ins.equals(sub) || rs.ins.equals(load) || rs.ins.equals(store)){
            rs.circle_total_need = 2;
        }
        else if(rs.ins.equals(multi)){
            rs.circle_total_need = 10;
        }
        else rs.circle_total_need = 40;
    }
    private void updateALU(int base, int num, int alu_index){
        int curr_exec = alu_exec[alu_index];
        if(curr_exec != -1){
            stations[curr_exec].circle_left -= 1;

            if(stations[curr_exec].circle_left == -1){
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
            return rs.v1+rs.v2;
        } else if (rs.ins.equals(sub)) {
            return rs.v1-rs.v2;
        } else if (rs.ins.equals(multi)) {
            return rs.v1*rs.v2;
        } else if (rs.ins.equals(div)) {
            return rs.v1/rs.v2;
        } else if (rs.ins.equals(load)) {
            return mem[rs.addr];
        } else if (rs.ins.equals(store)) {
            mem[rs.addr] = rs.v1;
            return 0;
        } else {
            return -1;
        }
    }
    private void inform(float res, int rs_index){
        // stations[rs_index] has a result res
        ReservedStation rs = stations[rs_index];
        for(Integer i : rs.reg_waited){
            if(registers[i].res_sta_id == rs_index){
                registers[i].data = res;
                registers[i].res_sta_id = -1;
            }
        }
        for(Integer i : rs.res_sta_waited){
            if(stations[i].r1 == rs_index){
                stations[i].v1 = res;
                stations[i].r1 = -1;
            }
            if(stations[i].r2 == rs_index){
                stations[i].v2 = res;
                stations[i].r2 = -1;
            }
            if(stations[i].ins.equals(store)){
                if(stations[i].r1==-1){
                    stations[i].is_busy = false;
                }
            }
             else if(stations[i].r1+stations[i].r2==-2){
                stations[i].is_busy = false;
            }
        }
    }
    private void startNewWork(int base, int num, int alu_index){
        if(alu_exec[alu_index] != -1)
            return;
        for(int i=base; i<base+num; i++){
            ReservedStation rs = stations[i];
            if(!rs.ins.equals("") && !rs.is_busy){
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

class Instruction{
    static public String add="ADDD", sub="SUBD", multi="MULTD", div="DIVD", load="LD", store="ST";
    public String ins;
    public int dst_reg_id, op1_reg_id, op2_reg_id;
    public int addr;
    public Instruction(String i, int dst, int op1, int op2){
        this.ins = i;
        this.dst_reg_id = dst;
        this.op1_reg_id = op1;
        this.op2_reg_id = op2;
    }

}
class Register{
    public int res_sta_id; // subscript of reserved station if data is not ready, or else -1
    public float data; // data held by this register.
    // only one of two variables above can be valid at the same time
    public Register(){
        res_sta_id = -1;
        data = 0;
    }
}
class ReservedStation{
    int circle_left, circle_total_need;
    String ins; // "" if empty
    int r1, r2; // subscript of reserved station if data is not ready, or else -1
    float v1, v2; // data if data is valid
    // only r1 and v1 are used if load||store ins
    boolean is_busy;
    int addr; // for load&store ins
    Vector<Integer> reg_waited;
    Vector<Integer> res_sta_waited;
    public ReservedStation(){
        ins = "";
        circle_left = 0;
        is_busy = false;
        reg_waited = new Vector();
        res_sta_waited = new Vector();
    }

}
/**
 * Created by lixiaohan on 2017/6/3.
 */
import java.math.*;
import java.io.*;
import java.util.*;


public class Tomasulo {

    static String add="ADDD", sub="SUBD", multi="MULTD", div="DIVD", load="LD", store="ST";

    Queue<Instruction> ins_queue;
    Register[] registers = new Register[16];
    static int num_a=3, num_m=2, num_l=3, num_s=3;
    static int base_a=0, base_m=3, base_l=5, base_s=8;
    ReservedStation[] stations = new ReservedStation[num_a+num_m+num_l+num_s];

    byte[] mem = new byte[4096];
    int clock = 0;
    int add_alu_exec=-1, multi_alu_exec=-1; // id of reserved station which holds the ins executed now

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
            info(clock);
            // fetch ins
            Instruction next_ins = ins_queue.peek();
            int station_id = checkResStations(next_ins); // return -1 if no empty station
            // update alu infomation. if finished, inform those waiting

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
                else if(ins[0].equals(load) || ins[0].equals(store)){
                    int reg = Integer.parseInt(ops[0].replace("F", ""));
                    int addr = Integer.parseInt(ops[1]);
                    Instruction instruction = new Instruction(ins[0], reg, addr);
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
            return tryResStation(base_a, num_a, next_ins);
        } else if (next_ins.ins.equals(multi) || next_ins.ins.equals(div)) {
            return tryResStation(base_m, num_m, next_ins);
        } else if (next_ins.ins.equals(load)) {
            return tryResStation(base_l, num_l, next_ins);
        } else if (next_ins.ins.equals(store)) {
            return tryResStation(base_s, num_s, next_ins);
        } else {
            return -1;
        }
    }
    private int tryResStation(int base, int num, Instruction next_ins){
        for(int i=base; i<(base+num); i++){
            if(stations[i].ins.equals("")){
                // empty
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
        }
        else if(stations[rs].ins.equals(store)){
            int src_reg = instruction.dst_reg_id;
            // ins in res_sta[rs] needs data from reg[src_reg]
            // check if data need is ready.
            // if ready, set v
            // else set r and register in station and set is_busy
            if(registers[src_reg].res_sta_id == -1){
                stations[rs].v1 = registers[src_reg].data;
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
            }else{
                stations[rs].r1 = registers[src_reg].res_sta_id;
                stations[registers[src_reg].res_sta_id].res_sta_waited.add(rs);
                stations[rs].is_busy = true;
            }

            src_reg = instruction.op2_reg_id;
            if(registers[src_reg].res_sta_id == -1){
                stations[rs].v2 = registers[src_reg].data;
            }else{
                stations[rs].r2 = registers[src_reg].res_sta_id;
                stations[registers[src_reg].res_sta_id].res_sta_waited.add(rs);
                stations[rs].is_busy = true;
            }
        }
        setTotalCircle(rs);
    }
    private void setTotalCircle(int id){
        ReservedStation rs = stations[id];
        if(rs.ins.equals(add) || rs.ins.equals(sub) || rs.equals(load) || rs.equals(store)){
            rs.circle_total_need = 2;
        }
        else if(rs.ins.equals(multi)){
            rs.circle_total_need = 10;
        }
        else rs.circle_total_need = 40;
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
    public Instruction(String i, int reg, int addr){
        this.ins = i;
        this.dst_reg_id = reg;
        this.addr = addr;
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
    Vector reg_waited;
    Vector res_sta_waited;
    public ReservedStation(){
        ins = "";
        circle_left = 0;
        is_busy = false;
        reg_waited = new Vector<Integer>();
        res_sta_waited = new Vector<Integer>();
    }

}
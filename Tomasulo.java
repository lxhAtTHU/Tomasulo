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
    ReservedStation[] stations = new ReservedStation[3+2+3+3];
        // add*3 multi*2 load*3 store*3
    static int base_a=0, base_m=3, base_l=5, base_s=8;

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
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(br.readLine().length()==0){
            clock ++;
            info(clock);
            // fetch ins


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
}

class Instruction{
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
    int circle_left;
    String ins; // "" if empty
    int r1, r2; // subscript of reserved station if data is not ready, or else -1
    float v1, v2; // data if data is valid
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
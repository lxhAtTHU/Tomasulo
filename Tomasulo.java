/**
 * Created by lixiaohan on 2017/6/3.
 */
import java.math.*;
import java.io.*;
import java.util.*;

class Instruction{
    public String ins;
    public int op1_reg_id, op2_reg_id, op3_reg_id;
    public Instruction(String i, int op1, int op2, int op3){
        this.ins = i;
        this.op1_reg_id = op1;
        this.op2_reg_id = op2;
        this.op3_reg_id = op3;
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
public class Tomasulo {

    Queue<Instruction> ins_queue;
    Register[] registers = new Register[16];
    ReservedStation[] stations = new ReservedStation[3+2+3+3];
        // add*3 multi*2 load*3 store*3

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

}

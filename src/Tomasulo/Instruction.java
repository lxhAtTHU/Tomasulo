package Tomasulo;

/**
 * Created by lixiaohan on 6/3/17.
 */
public class Instruction {
    static public String add = "ADDD", sub = "SUBD", multi = "MULTD",
            div = "DIVD", load = "LD", store = "ST";
    public String ins;
    public int dst_reg_id, op1_reg_id, op2_reg_id;
    public int addr;

    public Instruction(String i, int dst, int op1, int op2) {
        this.ins = i;
        this.dst_reg_id = dst;
        this.op1_reg_id = op1;
        this.op2_reg_id = op2;
    }
}

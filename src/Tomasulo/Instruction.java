package Tomasulo;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/8/17
 */
public class Instruction {

    public enum Operation {
        EMPTY,
        ADDD, SUBD,
        MULTD, DIVD,
        LD, ST
    };

    public Operation operation;

    public int dstRegId;
    public int op1RegId, op2RegId;
    public int addr;

    public Instruction(String op) {
        initialize(Operation.valueOf(op.toUpperCase()));
    }

    public Instruction(Operation op) {
        initialize(op);
    }

    public void initialize(Operation op) {
        this.operation = op;
    }

    public int getCycle() {
        switch (operation) {
            case ADDD: case SUBD:
            case LD: case ST:
                return 2;
            case MULTD:
                return 10;
            case DIVD:
                return 40;
            default:
                return 0;
        }
    }

    public void parseArgs(String[] args) {
        switch (operation) {
            case ADDD: case SUBD:
            case MULTD: case DIVD:
                dstRegId = Register.getIDFromName(args[0]);
                op1RegId = Register.getIDFromName(args[1]);
                op2RegId = Register.getIDFromName(args[2]);
                addr = 0;
                break;
            case LD:
                dstRegId = Register.getIDFromName(args[0]);
                op1RegId = 0;
                op2RegId = 0;
                addr = Integer.parseInt(args[1]);
                break;
            case ST:
                dstRegId = 0;
                op1RegId = Register.getIDFromName(args[0]);
                op2RegId = 0;
                addr = Integer.parseInt(args[1]);
                break;
            default:
                dstRegId = 0;
                op1RegId = 0;
                op2RegId = 0;
                addr = 0;
        }
    }
}

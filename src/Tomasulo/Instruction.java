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

    public Operation operation = Operation.EMPTY;

    public int dstRegId = -1;
    public int op1RegId = -1, op2RegId = -1;
    public int addr = -1;

    public boolean emit = false;
    public boolean done = false;
    public boolean writeback = false;

    public Instruction(String op) {
        initialize(Operation.valueOf(op.toUpperCase()));
    }

    public Instruction(Operation op) {
        initialize(op);
    }

    public void initialize(Operation op) {
        this.operation = op;
    }

    public String getOperation() {
        if (operation == Operation.EMPTY) return "";
        else return operation.toString();
    }

    public String getDestination() {
        if (operation == Operation.ST) return "@" + String.valueOf(addr);
        else return String.valueOf(dstRegId);
    }

    public String getSource() {
        if (operation == Operation.LD) return "@" + String.valueOf(addr);
        else return String.valueOf(op1RegId) + ", " + String.valueOf(op2RegId);
    }

    public String getEmit() {
        return emit ? ">" : "";
    }

    public String getDone() {
        return done ? ">" : "";
    }

    public String getWriteBack() {
        return writeback ? ">" : "";
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

    @Override
    public String toString() {
        return getOperation() + " " +
                getDestination() + " <- " + getSource() +
                " [" + getEmit() + getDone() + getWriteBack() +  "]";
    }

    @Override
    protected Instruction clone() {
        Instruction cloned = new Instruction(operation);
        cloned.dstRegId = dstRegId;
        cloned.op1RegId = op1RegId;
        cloned.op2RegId = op2RegId;
        cloned.addr = addr;
        cloned.emit = emit;
        cloned.done = done;
        cloned.writeback = writeback;
        return cloned;
    }
}

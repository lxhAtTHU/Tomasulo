package Tomasulo;

import java.util.*;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/8/17
 */
public class ReservedStation {
    public Instruction.Operation operation = Instruction.Operation.EMPTY;

    public int staId = 0;
    public int r1 = -2, r2 = -2;
    // subscript of reserved station if data is not ready, or else -1
    public float v1 = 0, v2 = 0;
    // data if data is valid
    // NOTE: only r1 and v1 are used in LD || ST operation
    public int address = -1; // for LD & ST operation

    public boolean isBusy = false;
    public Vector<Integer> regWaited = new Vector();
    public Vector<Integer> resStaWaited = new Vector();

    public int circleLeft = -1, circleTotalNeed = 0;

    public ReservedStation(int idx) {
        staId = idx;
    }

    public String getStationId() {
        return String.valueOf(staId);
    }

    public String getOperation() {
        if (operation == Instruction.Operation.EMPTY) return "";
        else return operation.toString();
    }

    public String getBusy() {
        if (operation == Instruction.Operation.EMPTY) return "";
        else return isBusy ? "忙" : "可用";
    }

    public String getValue1() {
        return getValue(r1, v1);
    }

    public String getValue2() {
        return getValue(r2, v2);
    }

    private String getValue(int r, float v) {
        if (r == -2) return "";
        else if (r == -1) return String.valueOf(v);
        else return "<[" + String.valueOf(r) + "]";
    }

    public String getAddress() {
        if (address == -1) return "";
        else return String.valueOf(address);
    }

    public String getCircleLeft() {
        if (operation == Instruction.Operation.EMPTY) return "";
        else if (circleLeft == -1) return "初";
        else return String.valueOf(circleLeft);
    }

    public String getCircleTotalNeed() {
        if (operation == Instruction.Operation.EMPTY) return "";
        else return String.valueOf(circleTotalNeed);
    }

    @Override
    public String toString() {
        if (operation == Instruction.Operation.EMPTY) {
            return getStationId() + ": " + getOperation();
        } else {
            return getStationId() + ": " + getOperation() + " " +
                    getValue1() + " " + getValue2() +
                    " @" + getAddress() + " " + getBusy() + " " +
                    "[" + getCircleLeft() + "/" + getCircleTotalNeed() + "]";
        }
    }
}

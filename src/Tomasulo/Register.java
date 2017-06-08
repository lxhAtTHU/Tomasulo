package Tomasulo;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/7/17.
 */
public class Register {
    public int regId;
    // ID of the register

    public int resStaId;
    // subscript of reserved station if data is not ready, or else -1
    public float data;
    // data held by this register.
    // NOTE: only one of two variables above can be valid at the same time

    public Register(int id) {
        regId = id;
        resStaId = -1;
        data = (float)0.;
    }

    public String getRegisterID() {
        return "F" + String.valueOf(regId);
    }

    public String getStationID() {
        if (resStaId == -1) return "";
        else return "<[" + String.valueOf(resStaId) + "]";
    }

    public String getData() {
        if (resStaId != -1) return "";
        else return String.valueOf(data);
    }

    static public int getIDFromName(String name) {
        return Integer.parseInt(name.replace("F", ""));
    }

    @Override
    public String toString() {
        return getRegisterID() + ": " +
                (resStaId != -1 ? "Station = " + getStationID() : "") +
                (resStaId == -1 ? "Data = " + getData() : "");
    }
}

package Tomasulo;

/**
 * Created by lixiaohan on 6/3/17.
 * Updated by dotkrnl on 6/7/17.
 */
public class Register {
    public int reg_id;
    // ID of the register

    public int res_sta_id;
    // subscript of reserved station if data is not ready, or else -1
    public float data;
    // data held by this register.
    // NOTE: only one of two variables above can be valid at the same time

    public Register(int id) {
        reg_id = id;
        res_sta_id = -1;
        data = (float)0.;
    }

    public String getRegID() {
        return "F" + String.valueOf(reg_id);
    }

    public String getStationID() {
        if (res_sta_id == -1) return "N/A";
        else return String.valueOf(res_sta_id);
    }

    public String getData() {
        if (res_sta_id != -1) return "N/A";
        else return String.valueOf(data);
    }
}

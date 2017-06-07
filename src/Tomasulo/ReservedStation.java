package Tomasulo;

import java.util.*;

/**
 * Created by lixiaohan on 6/3/17.
 */
public class ReservedStation {
    int circle_left, circle_total_need;
    String ins; // "" if empty
    int r1, r2; // subscript of reserved station if data is not ready, or else -1
    float v1, v2; // data if data is valid
    // only r1 and v1 are used if load||store ins

    boolean is_busy;
    int addr; // for load&store ins
    Vector<Integer> reg_waited;
    Vector<Integer> res_sta_waited;

    public ReservedStation() {
        ins = "";
        circle_left = 0;
        is_busy = false;
        reg_waited = new Vector();
        res_sta_waited = new Vector();
    }
}

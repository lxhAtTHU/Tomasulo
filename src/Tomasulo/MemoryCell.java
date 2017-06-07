package Tomasulo;

/**
 * Created by dotkrnl on 6/7/17.
 */
public class MemoryCell {
    private int address;
    private float data;

    public MemoryCell(int addr) {
        address = addr;
        data = (float)0.;
    }

    public String getAddress() {
        return String.valueOf(address);
    }

    public float getFloat() {
        return data;
    }

    public void setFloat(float d) {
        data = d;
    }

    public String getData() {
        return String.valueOf(data);
    }

    public void setData(String d) {
        try {
            data = Float.parseFloat(d);
        } catch (NumberFormatException e) {
            data = Float.NaN;
        }
    }
}

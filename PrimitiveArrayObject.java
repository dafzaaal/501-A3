import java.util.Arrays;

public class PrimitiveArrayObject {
    int[] integerArray;

    public PrimitiveArrayObject() {};

    @Override
    public String toString() {
        return Arrays.toString(integerArray);
    }
}

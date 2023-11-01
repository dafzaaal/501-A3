public class SimpleObject {
    int intValue;
    double doubleValue;
    char charValue;

    // Empty constructor
    public SimpleObject() {}

    @Override
    public String toString() {
        return "SimpleObject { " +
            "intValue=" + intValue +
            ", doubleValue=" + doubleValue +
            ", charValue=" + charValue +
            " }";
    }
}

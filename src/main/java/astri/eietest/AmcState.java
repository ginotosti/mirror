package astri.eietest;

public enum AmcState {
    OFFLINE(0),
    LOADED(1),
    STANDBY(2),
    ONLINE(3),
    MAINTENANCE(4),
    FAULT(5),
    UNKNOWN(6);
    private final int value;
    private AmcState(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }  
}

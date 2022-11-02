package astri.eietest;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MirrorSegmentController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AmcConnector client = null;

    private double centerOffset = 0.0;
    private double centerPos = 0.0;
    private double tiltX = 0.0;
    private double tiltY = 0.0;
    private double mirrorMaxPos = 0.0;
    private double mirrorMinPos = 0.0;
    private short segmentIndex = 1;
    private short motorStartIndex = 1;
    private short motorEndIndex = 3;
    private int mirrorStatus;
    private int mirrorHomed;
    private double mirrorAxis1Pos = 0.0;
    private double mirrorAxis1MaxPos = 0.0;
    private double mirrorAxis1MinPos = 0.0;
    private double mirrorAxis2Pos = 0.0;
    private double mirrorAxis2MaxPos = 0.0;
    private double mirrorAxis2MinPos = 0.0;
    private double mirrorAxis3Pos = 0.0;
    private double mirrorAxis3MaxPos = 0.0;
    private double mirrorAxis3MinPos = 0.0;

    private final short MAXINDEX = 18;
    private final short MININDEX = 1;
    private String mirror = "M1";

    // private String nodePrefix = "ns=2";

    private Map<Integer, MotorController> motor = new HashMap<>();
    private Map<Integer, Integer> motorMap = new HashMap<>();
    private MotorConfiguration motorConf;
    private double maxTiltY;
    private double maxTiltX;
    private double maxPos;

    public MirrorSegmentController(String mirror, short index, AmcConnector client) {
        this.client = client;
        this.mirror = mirror;
        short val=1;
        short mol=3;
        motorConf = new MotorConfiguration();
        if (index >= MININDEX && index <= MAXINDEX) {
            this.segmentIndex = index;

            motorStartIndex = Integer.valueOf((segmentIndex - val) * mol + val).shortValue();
            motorEndIndex = Integer.valueOf(motorStartIndex + 3).shortValue();
        }
    }

    public void setMotorConf(MotorConfiguration motorConf) {
        this.motorConf = motorConf;
    }

    public void assignMotors() {
        System.out.println("Mirror Segment:" + segmentIndex);
        for (short i = motorStartIndex; i < motorEndIndex; i++) {
            MotorController controller = new MotorController(client);
            System.out.println("Mirror:" + mirror + " Assigning Motor:" + i);
            if (controller != null) {
                controller.setMotorIndex(i);
                controller.assignToMirror(mirror);
                controller.assignToSegment(segmentIndex);
                controller.setMotorConfiguration(motorConf);
                motor.put((int)i, controller);
                motorMap.put(Integer.valueOf(i - motorStartIndex), (int)i);
            }
        }
    }

    public MotorController getMotor1() {
        return getMotor(motorMap.get(0).shortValue());
    }

    public MotorController getMotor2() {
        return getMotor(motorMap.get(1).shortValue());
    }

    public MotorController getMotor3() {
        return getMotor(motorMap.get(2).shortValue());
    }

    public short getMotor1Index() {
        return motorMap.get(0).shortValue();
    }

    public short getMotor2Index() {
        return motorMap.get(1).shortValue();
    }

    public short getMotor3Index() {
        return motorMap.get(2).shortValue();
    }

    public MotorController getMotor(short index) {
        if (index >= motorStartIndex && index <= motorEndIndex)
            return motor.get(Integer.valueOf(index));
        else {
            return null;
        }
    }

    public double getCenterOffset() {
        NodeId nodeId = client.formMirrorSetNode("CENTER_OFFSET");
        DataValue value = client.readMirrorValue(segmentIndex, nodeId);
        centerOffset = (double) value.getValue().getValue();
        return centerOffset;
    }

    public void setCenterOffset(double centerOffset) {
        client.executeMirrorSet(segmentIndex, "CENTER_OFFSET", centerOffset);
        this.centerOffset = centerOffset;
    }

    public double getCenterPos() {
        DataValue value = client.executeMirrorGet(segmentIndex, "CENTER_POS");
        
        centerPos = (double) value.getValue().getValue();
        return centerPos;
    }

    public void setCenterPos(double centerPos) {
        client.executeMirrorSet(segmentIndex, "CENTER_POS", centerPos);
        this.centerPos = centerPos;
    }

    public double getTiltX() {
        DataValue val=client.executeMirrorGet(segmentIndex, "TILT_X");
        tiltX=(double) val.getValue().getValue();
        return tiltX;
    }

    public void setTiltX(double tiltX) {
        client.executeMirrorSet(segmentIndex, "TILT_X", tiltX);
        this.tiltX = tiltX;
    }

    public double getTiltY() {
        DataValue val=client.executeMirrorGet(segmentIndex, "TILT_Y");
        tiltY=(double) val.getValue().getValue();
        return tiltY;
    }

    public void setTiltY(double tiltY) {
        client.executeMirrorSet(segmentIndex, "TILT_Y", tiltY);
        this.tiltY = tiltY;
    }

    public void setMaxTiltY(double tiltY) {
        client.executeMirrorSet(segmentIndex, "TILT_Y_MAX", tiltY);
        this.maxTiltY = tiltY;
    }

    public void setMaxTiltX(double tiltX) {
        client.executeMirrorSet(segmentIndex, "TILT_X_MAX", tiltY);
        this.maxTiltX = tiltX;
    }


    public int getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(short segmentIndex) {
        this.segmentIndex = segmentIndex;
    }

    public String getMirror() {
        return mirror;
    }

    public void setMirror(String mirror) {
        this.mirror = mirror;
    }

    public int getMirrorStatus() {
        DataValue val=client.executeMirrorGet(segmentIndex, "STATUS");
        mirrorStatus=(int) ((short)val.getValue().getValue());
        return mirrorStatus;
    }

    public void setMirrorStatus(int mirrorStatus) {
        this.mirrorStatus = mirrorStatus;
    }

    public double getMirrorAxis1Pos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS1_POS");
        mirrorAxis1Pos=(double) val.getValue().getValue();
        return mirrorAxis1Pos;
    }

    public double getMirrorAxis1MaxPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS1_MAXPOS");
        mirrorAxis1MaxPos=(double) val.getValue().getValue();
        return mirrorAxis1MaxPos;
    }

    public double getMirrorMaxPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "MAX_POS");
        mirrorMaxPos=(double) val.getValue().getValue();
        return mirrorMaxPos;
    }

    public void setMirrorMaxPos(double mirrorMaxPos) {
        client.executeMirrorSet(segmentIndex, "MAX_POS", mirrorMaxPos);
        this.mirrorMaxPos = mirrorMaxPos;
    }

    public double getMirrorAxis1MinPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS1_MINPOS");
        mirrorAxis1MinPos=(double) val.getValue().getValue();
        return mirrorAxis1MinPos;
    }

    public void setMirrorMinPos(double mirrorMinPos) {
        client.executeMirrorSet(segmentIndex, "MIN_POS", mirrorAxis1MaxPos);
        this.mirrorMinPos = mirrorMinPos;
    }

    public double getMirrorAxis2Pos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS2_POS");
        mirrorAxis2Pos=(double) val.getValue().getValue();
        return mirrorAxis2Pos;
    }

    public double getMirrorAxis2MaxPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS2_MAXPOS");
        mirrorAxis2MaxPos=(double) val.getValue().getValue();
        return mirrorAxis2MaxPos;
    }

    public double getMirrorAxis2MinPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS2_MINPOS");
        mirrorAxis2MinPos=(double) val.getValue().getValue();
        return mirrorAxis2MinPos;
    }

    public double getMirrorAxis3Pos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS3_POS");
        mirrorAxis3Pos=(double) val.getValue().getValue();
        return mirrorAxis3Pos;
    }

    public double getMirrorAxis3MaxPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS3_MAXPOS");
        mirrorAxis3MaxPos=(double) val.getValue().getValue();
        return mirrorAxis3MaxPos;
    }

    public double getMirrorAxis3MinPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "AXIS3_MINPOS");
        mirrorAxis3MinPos=(double) val.getValue().getValue();
        return mirrorAxis3MinPos;
    }

    public double getMirrorMinPos() {
        DataValue val=client.executeMirrorGet(segmentIndex, "MIN_POS");
        mirrorMinPos=(double) val.getValue().getValue();
        return mirrorMinPos;
    }

    public int getMirrorHomed() {
        DataValue val=client.executeMirrorGet(segmentIndex, "HOMED");
        mirrorMinPos=(int)((short) val.getValue().getValue());
        return mirrorHomed;
    }

    public void cmdDisableMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"DISABLE_MIRROR",val);
    }

    public void cmdEnableMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"ENABLE_MIRROR",val);
    }

    public void cmdMoveMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"MOVE_MIRROR",val);
    }
    public void cmdOffsetMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"OFFSET_MIRROR",val);
    }

    public void cmdStartHomingMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"START_HOMING_MIRROR",val);
    }

    public void cmdStopHomingMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"STOP_HOMING_MIRROR",val);
    }

    public void cmdStopMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"STOP_MIRROR",val);
    }

    public void cmdTilXMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"TILTX_MIRROR",val);
    }
    public void cmdTilYMirror() {
        boolean val = true;
        client.executeMirrorCmd(segmentIndex,"TILTY_MIRROR",val);
    }
    /*
     * CMD_AMC_M1_START_HOMING_MIRROR
     * CMD_AMC_M1_OFFSET_MIRROR
     * CMD_AMC_M1_MOVE_MIRROR
     * CMD_AMC_M1_TILTX_MIRROR
     * CMD_AMC_M1_TILTY_MIRROR
     * CMD_AMC_M1_DISABLE_MIRROR
     * CMD_AMC_M1_ENABLE_MIRROR
     */
}

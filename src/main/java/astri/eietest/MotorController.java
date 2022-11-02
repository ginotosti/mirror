package astri.eietest;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorController {

    private MotorConfiguration mConfiguration;
    private AmcConnector client = null;
    public short motorIndex = 1;
    private final int MAXINDEX = 54;
    private final int MININDEX = 1;
    /*
     * private double motorVelLow = 0.0;
     * private double motorVelHigh = 0.0;
     * private double motorAcc = 0.0;
     * private double motorDec = 0.0;
     */
    private double motorPos = 0.0;
    private double motorVel = 0.0;
    private double motorCurrent = 0.0;
    /*
     * private double motorVelHoming = 0.0;
     * private int upperLimitStatus = 1;
     * private int lowerLimitStatus = 1;
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private int segIdx = 1;
    private String mirror = "M1";
    private final String getPrefix = "MOTOR_INFO";
    private String nodePrefix = "ns=2";
    private String setPrefix = "MOTOR.MOTORS";
    private String elementNodeIndexPrefix = "_MOTOR.INDEX";
    private String nodeIndex = "";
    private NodeId nodeIndexId;
    private double motorOffset;
    private boolean isMaintenance = false;

    public MotorController(AmcConnector client) {
        this.client = client;

    }

    public boolean isMaintenance() {
        return isMaintenance;
    }

    public void setMaintenance(boolean isMaintenance) {
        this.isMaintenance = isMaintenance;
    }

    public void setNodePrefix(String nodePrefix) {
        this.nodePrefix = nodePrefix;
    }

    public MotorConfiguration getMotorConfiguration() {
        return mConfiguration;
    }

    public void setMotorConfiguration(MotorConfiguration mConfiguration) {
        this.mConfiguration = mConfiguration;
    }

    public int getMotorIndex() {
        return motorIndex;
    }

    public void setMotorIndex(short motorIndex) {
        System.out.println("Selected motor:" + motorIndex);
        if (motorIndex >= MININDEX && motorIndex <= MAXINDEX) {
            this.motorIndex = motorIndex;
            client.setMotorIndex(motorIndex);
        }
    }

    public void assignToSegment(int idx) {
        segIdx = idx;
    }

    public void assignToMirror(String mir) {
        mirror = mir;
        nodeIndex = nodePrefix + ";s=SET_" + mirror + elementNodeIndexPrefix;
        nodeIndexId = NodeId.parse(nodeIndex);
    }

    /*
     * public double getMotorVelLow() {
     * // NodeId nodeId =
     * // NodeId.parse(nodePrefix+";s=SET_"+mirror+"_MOTOR.MOTORS.VEL_LOW");
     * NodeId nodeId = client.formMotorSetNode("VEL_LOW");
     * DataValue value = client.readMotorValue(motorIndex, nodeId);
     * motorVelLow = (double) value.getValue().getValue();
     * return motorVelLow;
     * }
     * 
     * public double getMotorVelHigh() {
     * // NodeId nodeId =
     * // NodeId.parse(nodePrefix+";s=SET_"+mirror+"_MOTOR.MOTORS.VEL_HIGH");
     * NodeId nodeId = client.formMotorSetNode("VEL_HIGH");
     * DataValue value = client.readMotorValue(motorIndex, nodeId);
     * motorVelHigh = (double) value.getValue().getValue();
     * return motorVelHigh;
     * }
     * 
     * public double getMotorAcc() {
     * // NodeId nodeId =
     * // NodeId.parse(nodePrefix+";s=SET_"+mirror+"_MOTOR.MOTORS.ACC");
     * NodeId nodeId = client.formMotorSetNode("ACC");
     * DataValue value = client.readMotorValue(motorIndex, nodeId);
     * motorAcc = (double) value.getValue().getValue();
     * return motorAcc;
     * }
     * 
     * public double getMotorDec() {
     * // NodeId nodeId =
     * // NodeId.parse(nodePrefix+";s=SET_"+mirror+"_MOTOR.MOTORS.DEC");
     * NodeId nodeId = client.formMotorSetNode("DEC");
     * DataValue value = client.readMotorValue(motorIndex, nodeId);
     * motorDec = (double) value.getValue().getValue();
     * return motorDec;
     * }
     */

    public double getMotorPos() {
        DataValue value = client.executeMotorGet(motorIndex, "POS");
        motorPos = (double) value.getValue().getValue();
        return motorPos;
    }

    public double getMotorCurrent() {
        DataValue value = client.executeMotorGet(motorIndex, "CURRENT");
        motorCurrent = (double) value.getValue().getValue();
        return motorCurrent;
    }

    /*
     * public int getUpperLimitStatus() {
     * DataValue value = client.executeMotorGet(motorIndex, "UPPER_SWITCH_STATUS");
     * upperLimitStatus = (int) ((short) value.getValue().getValue());
     * return upperLimitStatus;
     * }
     * 
     * public int getLowerLimitStatus() {
     * DataValue value = client.executeMotorGet(motorIndex, "LOWER_SWITCH_STATUS");
     * lowerLimitStatus = (int)( (short)value.getValue().getValue());
     * return lowerLimitStatus;
     * }
     */

    public double getMotorVel() {
        DataValue value = client.executeMotorGet(motorIndex, "VEL");
        motorVel = (double) value.getValue().getValue();
        return motorVel;
    }

    public void setMotorOffset(double motorOffset) {
        client.executeMotorSet(motorIndex, "OFFSET", motorOffset);
        this.motorOffset = motorOffset;

    }

    public void setMotorPos(double motorPos) {
        client.executeMotorSet(motorIndex, "POS", motorOffset);
        this.motorPos = motorPos;

    }

    public void cmdDisableMotor() {
        if (isMaintenance) {
            boolean val = true;
            client.executeMotorCmd(motorIndex, "DISABLE", val);
        }
    }

    public void cmdEnableMotor() {
        boolean val = true;
        client.executeMotorCmd(motorIndex, "ENABLE", val);
    }

    public void cmdMoveMotor() {
        if (isMaintenance) {
            boolean val = true;
            client.executeMotorCmd(motorIndex, "MOVE", val);
        }
    }

    public void cmdStopMotor() {
        if (isMaintenance) {
            boolean val = true;
            client.executeMotorCmd(motorIndex, "STOP", val);
        }
    }

    public void cmdOffsetMotor() {
        if (isMaintenance) {
            boolean val = true;
            client.executeMotorCmd(motorIndex, "OFFSET", val);
        }
    }

    /*
     * public double getMotorVelHoming() {
     * // NodeId nodeId =
     * // NodeId.parse(nodePrefix+";s=SET_"+mirror+"_MOTOR.MOTORS.VEL_HOMING");
     * NodeId nodeId = client.formMotorSetNode("VEL_HOMING");
     * DataValue value = client.readMotorValue(motorIndex, nodeId);
     * motorVelHoming = (double) value.getValue().getValue();
     * return motorVelHoming;
     * }
     * 
     * public void setMotorVelLow(double motorVelLow) {
     * client.executeMotorSet(motorIndex, "VEL_LOW", motorVelLow);
     * this.motorVelLow = motorVelLow;
     * 
     * }
     * 
     * public void setMotorVelHigh(double motorVelHigh) {
     * client.executeMotorSet(motorIndex, "VEL_HIGH", motorVelHigh);
     * this.motorVelHigh = motorVelHigh;
     * }
     * 
     * public void setMotorAcc(double motorAcc) {
     * client.executeMotorSet(motorIndex, "ACC", motorAcc);
     * this.motorAcc = motorAcc;
     * }
     * 
     * public void setMotorDec(double motorDec) {
     * client.executeMotorSet(motorIndex, "DEC", motorDec);
     * this.motorDec = motorDec;
     * }
     * 
     * public void setMotorVel(double motorVel) {
     * client.executeMotorSet(motorIndex, "VEL", motorVel);
     * this.motorVel = motorVel;
     * }
     * 
     * public void setMotorVelHoming(double motorVelHoming) {
     * client.executeMotorSet(motorIndex, "VEL_HOMING", motorVelHoming);
     * this.motorVelHoming = motorVelHoming;
     * }
     */

    public MotorConfiguration readCurrentConfiguration() {/*
                                                           * NodeId nodeId = client.formMotorSetNode("MAX_ACC");
                                                           * DataValue value = client.readMotorValue(motorIndex,
                                                           * nodeId);
                                                           * mConfiguration.motorMaxAcc = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MIN_ACC");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMinAcc = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MAX_DEC");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMaxDec = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MIN_DEC");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMinDec = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MIN_POS");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMinPos = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MAX_POS");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMinPos = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MAX_VEL");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMaxVel = (double)
                                                           * value.getValue().getValue();
                                                           * nodeId = client.formMotorSetNode("MIN_VEL");
                                                           * value = client.readMotorValue(motorIndex, nodeId);
                                                           * mConfiguration.motorMinVel = (double)
                                                           * value.getValue().getValue();
                                                           */
        return mConfiguration;
    }

    public void writeConfiguration(MotorConfiguration conf) {
        /*
         * NodeId nodeId = client.formMotorSetNode("MAX_ACC");
         * client.writeMotorValue(motorIndex, nodeId,mConfiguration.motorMaxAcc);
         * //mConfiguration.motorMaxAcc = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MIN_ACC");
         * client.writeMotorValue(motorIndex, nodeId,mConfiguration.motorMinAcc);
         * //mConfiguration.motorMinAcc = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MAX_DEC");
         * client.writeMotorValue(motorIndex, nodeId, mConfiguration.motorMaxDec);
         * //mConfiguration.motorMaxDec = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MIN_DEC");
         * client.writeMotorValue(motorIndex, nodeId, mConfiguration.motorMinDec);
         * //mConfiguration.motorMinDec = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MIN_POS");
         * client.writeMotorValue(motorIndex, nodeId,mConfiguration.motorMinPos);
         * //mConfiguration.motorMinPos = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MAX_POS");
         * client.writeMotorValue(motorIndex, nodeId, mConfiguration.motorMinPos);
         * //mConfiguration.motorMinPos = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MAX_VEL");
         * client.writeMotorValue(motorIndex, nodeId,mConfiguration.motorMaxVel);
         * //mConfiguration.motorMaxVel = (double) value.getValue().getValue();
         * nodeId = client.formMotorSetNode("MIN_VEL");
         * client.writeMotorValue(motorIndex, nodeId,mConfiguration.motorMinVel);
         * //mConfiguration.motorMinVel = (double) value.getValue().getValue();
         */

    }
    /*
     * CMD_AMC_M1_OFFSET_MOTOR
     * CMD_AMC_M1_MOVE_MOTOR
     * CMD_AMC_M1_STOP_MOTOR
     * CMD_AMC_M1_ENABLE_MOTOR
     * CMD_AMC_M1_DISABLE_MOTOR
     */

}

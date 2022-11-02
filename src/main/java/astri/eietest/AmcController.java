package astri.eietest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import astri.icd.AstrimaIcd;
import astri.icd.AstrimaIcdDataPoint;

public class AmcController {

    private AmcState m1State;
    private AmcState m2State;
    private String primaryMirrorName;
    private String secondaryMirrorName;
    private MirrorController m1Controller;
    private MirrorController m2Controller;
    private String host;
    private int port;
    private String path;
    private int nM1Segments;
    private int nM2Segments;
    private AmcConnector primaryMirrorConnector = null;
    private AmcConnector secondaryMirrorConnector = null;
    private MotorConfiguration m1MotorConfiguration;
    private MotorConfiguration m2MotorConfiguration;
    private Properties prop = new Properties();
    private Properties primaryConfProps;
    private Properties secondaryConfProps;
    private short primaryMirrorErrorNumber;
    private String primaryMirrorErrorDesciption;
    private short primaryMirrorErrorRecoveredNumber;
    private String primaryMirrorErrorRecoveredDesciption;
    private short primaryMirrorStatus;
    private boolean primaryMirrorHomeStatus;
    private short primaryMirrorState;
    private short secondaryMirrorErrorNumber;
    private short secondaryMirrorErrorRecoveredNumber;
    private String secondaryMirrorErrorDesciption;
    private String secondaryMirrorErrorRecoveredDesciption;
    private int secondaryMirrorStatus;
    private MirrorStatus secondaryMirrorHomeStatus;
    private short secondaryMirrorState;
    private List<double[]> primaryMirrorSegmentsPos=new ArrayList<>();
    AstrimaIcd icd;
    private String astriIcdFile;
    List<NodeId> primaryMirrorMonitoredNodes = new ArrayList<>();
    List<NodeId> secondaryMirrorMonitoredNodes = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(AmcController.class);
    private String nodePrefix="ns=4";

    public AmcController() {

        loadConfiguration();
        primaryMirrorConnector = new AmcConnector(host, port, path);
        primaryMirrorConnector.setNodePrefix(nodePrefix);
        primaryMirrorConnector.assignToMirror(primaryMirrorName);
       
        secondaryMirrorConnector = new AmcConnector(host, port, path);
        secondaryMirrorConnector.setNodePrefix(nodePrefix);
        secondaryMirrorConnector.assignToMirror(secondaryMirrorName);
        icd = new AstrimaIcd(astriIcdFile);
        loadAstriIcdFile();
        createM1Controller();
        createM2Controller();
        
    }

    private void loadAstriIcdFile() {
        icd=new AstrimaIcd(astriIcdFile);
        Map<String, AstrimaIcdDataPoint> getpoints = icd.getMonitoringPoints();
        getpoints.forEach((name, val) -> {
            if (val.getOpcUaNode().contains("M1") && val.getIsMonitored().contains("Y"))
                primaryMirrorMonitoredNodes.add(NodeId.parse(val.getOpcUaNode().replace("ns=4",nodePrefix)));
            if (val.getOpcUaNode().contains("M2") && val.getIsMonitored().contains("Y"))
                secondaryMirrorMonitoredNodes.add(NodeId.parse(val.getOpcUaNode().replace("ns=4", nodePrefix)));
        });
        icd.close();
        logger.info("Loaded M1 and M2 Monitored data point");
    }

    private void createM1Controller() {
        m1Controller = new MirrorController(primaryMirrorName, nM1Segments, primaryMirrorConnector);
        m1MotorConfiguration = new MotorConfiguration(primaryConfProps);
        m1Controller.setLogger(logger);
        m1Controller.setMotorConf(m1MotorConfiguration);

        m1Controller.createSegmentControllers();
    }

    private void createM2Controller() {
        m2Controller = new MirrorController(secondaryMirrorName, nM2Segments, secondaryMirrorConnector);
        m2MotorConfiguration = new MotorConfiguration(secondaryConfProps);
        m2Controller.setMotorConf(m2MotorConfiguration);
        m2Controller.createSegmentControllers();
    }

    private void loadConfiguration() {

        try {
            // load a properties file from class path, inside static method
            prop.load(AmcController.class.getClassLoader().getResourceAsStream("config.properties"));

            // get the property value and print it out
            System.out.println(prop.getProperty("opcua.host"));
            System.out.println(prop.getProperty("opcua.port"));
            System.out.println(prop.getProperty("opcua.path"));
            this.host = prop.getProperty("opcua.host");
            this.path = prop.getProperty("opcua.path");
            this.port = Integer.parseInt(prop.getProperty("opcua.port"));
            this.primaryMirrorName = prop.getProperty("mirror.primary.name");
            this.secondaryMirrorName = prop.getProperty("mirror.secondary.name");
            this.nM1Segments = Integer.parseInt(prop.getProperty("mirror.primary.segments"));
            this.nM2Segments = Integer.parseInt(prop.getProperty("mirror.secondary.segments"));
            this.astriIcdFile = prop.getProperty("assembly.icdfilename");
            this.nodePrefix="ns="+prop.getProperty("opcua.namespace");
            System.out.println(nodePrefix);
            System.out.println("" + nM1Segments);
            secondaryConfProps = getPropSubset("mirror.secondary");
            primaryConfProps = getPropSubset("mirror.primary");
            logger.info("Loaded Controllere connfiguration");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Properties getPropSubset(String prefix) {
        Properties result = new Properties();
        String key;
        for (Enumeration e = prop.propertyNames(); e.hasMoreElements();) {
            key = (String) e.nextElement();
            // keep full prefix in result, also copy direct matches
            if (key.startsWith(prefix)) {
                String key1 = key.substring(prefix.length() + 1, key.length());
                // System.out.println(key1+"=Double.parseDouble(prop.getProperty(\""+key1+"\"));");
                result.setProperty(key1, prop.getProperty(key));
            }
        }
        return result;
    }

    public String getPrimaryMirrorName() {
        return primaryMirrorName;
    }

    public MirrorController getPrimaryMirrorController() {
        //m1Controller.getSegmentController(1).getMotor1();
        return m1Controller;
    }

    public int getNumberOfPrimaryMirrorSegments() {
        return nM1Segments;
    }

    public String getSecondaryMirrorName() {
        return secondaryMirrorName;
    }

    public MirrorController getSecondaryMirrorController() {
        return m2Controller;
    }

    public int getNumberOfSecondaryMirrorSegments() {
        return nM2Segments;
    }

    public AmcConnector getPrimaryMirrorConnector() {
        return primaryMirrorConnector;
    }

    public AmcConnector getSecondaryMirrorConnector() {
        return secondaryMirrorConnector;
    }

    public short getPrimaryMirrorErrorNumber() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "ERROR_NUMBER");
        primaryMirrorErrorNumber = (short) value.getValue().getValue();
        return primaryMirrorErrorNumber;
    }

    public short getSecondaryMirrorErrorNumber() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName, "ERROR_NUMBER");
        secondaryMirrorErrorNumber = (short) value.getValue().getValue();
        return secondaryMirrorErrorNumber;
    }

    public short getPrimaryMirrorErrorRecoveredNumber() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "ERROR_RECOVERED_NUMBER");
        primaryMirrorErrorRecoveredNumber = (short) value.getValue().getValue();
        return primaryMirrorErrorRecoveredNumber;
    }

    public String getPrimaryMirrorErrorDescription() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "ERROR_DESCRIPTION");
        primaryMirrorErrorDesciption = (String) value.getValue().getValue();
        return primaryMirrorErrorDesciption;
    }

    public String getPrimaryMirrorErrorRecoveredDescription() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "ERROR_RECOVERED_DESCRIPTION");
        primaryMirrorErrorRecoveredDesciption = (String) value.getValue().getValue();
        return primaryMirrorErrorRecoveredDesciption;
    }

    public MirrorStatus getPrimaryMirrorStatus() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "STATUS");
        int val = (int)((short)value.getValue().getValue());
        if(val>=0 && val<=5)
            return MirrorStatus.values()[val];
        return MirrorStatus.UNKNOWN;
    }

    public MirrorHome getPrimaryMirrorHomeStatus() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "HOME_STATUS");
        int val = 0;
        if(((boolean)value.getValue().getValue())){
            val=1;
        }else{
            val=0;
        }
        if(val>=0 && val<=1)
            return MirrorHome.values()[(int)val];
        return MirrorHome.UNKNOWN;
    }

    public MirrorStatePhase getPrimaryMirrorStatePhase() {
        DataValue value = primaryMirrorConnector.executeAmcInfoGet(primaryMirrorName, "STATE_PHASE");
        int val = (int)((short) value.getValue().getValue());
        if(val>=0 && val<=3)
            return MirrorStatePhase.values()[val];
        return MirrorStatePhase.UNKNOWN;
    }

    public AmcState getPrimaryMirrorState() {
        return m1Controller.getMirrorState();
    }

    public short getSecondaryMirrorErrorRecoveredNumber() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName, "ERROR_RECOVERED_NUMBER");
        secondaryMirrorErrorRecoveredNumber = (short) value.getValue().getValue();
        return secondaryMirrorErrorRecoveredNumber;
    }

    public String getSecondaryMirrorErrorDescription() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName, "ERROR_DESCRIPTION");
        secondaryMirrorErrorDesciption = (String) value.getValue().getValue();
        return secondaryMirrorErrorDesciption;
    }

    public String getSecondaryMirrorErrorRecoveredDescription() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName,
                "ERROR_RECOVERED_DESCRIPTION");
        secondaryMirrorErrorRecoveredDesciption = (String) value.getValue().getValue();
        return secondaryMirrorErrorRecoveredDesciption;
    }

    public MirrorStatus getSecondaryMirrorStatus() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName, "STATUS");
        int val = (int)((short) value.getValue().getValue());
        return MirrorStatus.values()[val];
    }

    public MirrorHome getSecondaryMirrorHomeStatus() {
        DataValue value = secondaryMirrorConnector.executeAmcInfoGet(secondaryMirrorName, "HOME_STATUS");
        int val = (int) ((short) value.getValue().getValue());
        return MirrorHome.values()[val];
    }

    public String getPrimaryMirrorSegmentsPos(){
        return m1Controller.getM1Pos();
    }


    /*public List<double[]> getPrimaryMirrorSegmentsPos(){
        primaryMirrorSegmentsPos.clear();
        double[] pos=m1Controller.getAllMirrorPos();
        primaryMirrorSegmentsPos.addAll(Arrays.asList(pos));
        return primaryMirrorSegmentsPos;
    }*/

    
    public void amcEmergencyStop(boolean value) {
        primaryMirrorConnector.executeAmcCmd("EMERGENCY_STOP", value);
    }


    public void primaryMirrorGoLoaded() {
       m1Controller.goLoaded();
    }

    public void primaryMirrorGoOnline() {
        m1Controller.goOnline();
    }

    public void primaryMirrorGoMaintenance() {
        m1Controller.goMaintenance();
    }

    public void primaryMirrorGoStandby() {
        m1Controller.goStandby();
    }

    public void secondaryMirrorGoLoaded() {
        m2Controller.goLoaded();
    }

    public void secondaryMirrorGoStandby() {
        m2Controller.goStandby();
    }


    public void secondaryMirrorGoOnline() {
        m2Controller.goOnline();
    }

    public void secondaryMirrorGoMaintenance() {
        m2Controller.goMaintenance();
    }

    public void primaryMirrorStartHomingAllSegments() {
        m1Controller.startHomingAllSegments();
    }

    public void primaryMirrorStopHomingAllSegments() {
        m1Controller.stopHomingAllSegments();
    }

    public void primaryMirrorEnableAllSegments() {
        m1Controller.stopHomingAllSegments();
    }

    public void primaryMirrorDisableAllSegments() {
        m1Controller.disableAllSegments();
    }

    public void primaryMirrorMovePistonSegment(short idx, double pos){
        m1Controller.movePistonSegment(idx, pos);
    }

    public void primaryMirrorMoveTiltXSegment(short idx, double pos){
        m1Controller.moveTiltXSegment(idx, pos);
    }

    public void primaryMirrorMoveTiltYSegment(short idx, double pos){
        m1Controller.moveTiltYSegment(idx, pos);
    }

    public void primaryMirrorMoveOffsetSegment(short idx, double pos){
        m1Controller.moveTiltYSegment(idx, pos);
    }

    public void primaryMirrorHomeSegment(short idx){
        m1Controller.HomeSegment(idx);;
    }

    public void primaryMirrorStopHomeSegment(short idx){
        m1Controller.stopHomeSegment(idx);
    }

    public void primaryMirrorStopSegmentMotion(short idx){
        m1Controller.stopSegmentMotion(idx);
    }

    public void updateM1Pos(){
        ;
    }

    public void close() {
        primaryMirrorConnector.disconnect();
        secondaryMirrorConnector.disconnect();
    }
}

package astri.eietest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.sdk.server.subscriptions.Subscription.State;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;

public class MirrorController {

    private AmcConnector client;
    private String name = "";
    private int numberOfSegments = 0;
    private List<MirrorSegmentController> segController = new ArrayList<>();
    private String m1Pos="";
    private MotorConfiguration motorConf;
    private AmcState state;
    private Logger logger;

    ChangeListener myListener = new ChangeListener() {
        @Override
        public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {

            // Each item in the dataItems list has a corresponding value at
            // the same index in the dataValues list.
            // Some items may appear multiple times if the item has a queue
            // size greater than 1 and the value changed more than once within
            // the publishing interval of the subscription.
            // The items and values appear in the order of the changes.
            int size = dataItems.size();
            for (int i = 0; i < size; i++) {
                ManagedDataItem item=dataItems.get(i);
                DataValue value= dataValues.get(i);
                logger.info(
                        "received:" +
                                item.getReadValueId().getNodeId() +":" +value.getValue().getValue()+":"+value.getSourceTime().getJavaTime());
            }
        }
    };



    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MirrorController(String name, int nSegments, AmcConnector amc) {
        this.name = name;
        this.numberOfSegments = nSegments;
        this.client = amc;
        // createSegmentControllers();
    }

    public AmcConnector getClient() {
        return client;
    }

    public void setClient(AmcConnector client) {
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSegments() {
        return numberOfSegments;
    }

    public void setNumberOfSegments(int numberOfSegments) {
        this.numberOfSegments = numberOfSegments;
    }

    public void setMotorConf(MotorConfiguration motorConf) {
        this.motorConf = motorConf;
    }

    public void createSegmentControllers() {
        int ii;
        for (short i = 0; i < numberOfSegments; i++) {
            ii=i+1;
            MirrorSegmentController msc = new MirrorSegmentController(name, Integer.valueOf(ii).shortValue(), client);
            //msc.setMotorConf(motorConf);
            msc.assignMotors();
            segController.add(msc);
            System.out.println("Created Mirror controller:" + (i + 1));
        }
    }

    public MirrorSegmentController getSegmentController(int idx) {
        if (idx >= 1 && idx <= numberOfSegments)
            return segController.get(idx - 1);
        return null;
    }

    public void startHomingAllSegments() {
        boolean val = true;
        client.executeMirrorCmd("START_HOMING_ALL",val);
    }

    public void stopHomingAllSegments() {
        boolean val = true;
        client.executeMirrorCmd("STOP_HOMING_ALL",val);
    }

    public void enableAllSegments() {
        boolean val = true;
        client.executeMirrorCmd("ENABLE_ALL",val);
    }

    public void disableAllSegments() {
        boolean val = true;
        client.executeMirrorCmd("DISABLE_ALL",val);
    }

    public void movePistonSegment(int idx, double pos){
        segController.get(idx - 1).setCenterPos(pos);
        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        segController.get(idx - 1).cmdMoveMirror();
    }

    public void moveTiltXSegment(int idx, double pos){
        segController.get(idx - 1).setTiltX(pos);
        segController.get(idx - 1).cmdTilXMirror();
    }

    public void moveTiltYSegment(int idx, double pos){
        segController.get(idx - 1).setTiltY(pos);
        segController.get(idx - 1).cmdTilXMirror();
    }

    public void moveOffsetSegment(int idx, double pos){
        segController.get(idx - 1).setCenterOffset(pos);
        segController.get(idx - 1).cmdOffsetMirror();
    }

    public void HomeSegment(int idx){
        segController.get(idx - 1).cmdStartHomingMirror();
    }

    public void stopHomeSegment(int idx){
        segController.get(idx - 1).cmdStopHomingMirror();
    }

    public void stopSegmentMotion(int idx){
        segController.get(idx - 1).cmdStopMirror();
    }


    public void goLoaded() {
        boolean val = true;
        client.executeAmcMode("LOADED",val);
    }

    public void goOnline() {
        boolean val = true;
        client.executeAmcMode("ONLINE",val);
    }

    public void goMaintenance() {
        boolean val = true;
        client.executeAmcMode("MAINTENANCE",val);
    }

    public void goStandby() {
        boolean val = true;
        client.executeAmcMode("STANDBY",val);
    }

    public AmcState getMirrorState() {
        DataValue value = client.executeAmcInfoGet(name, "STATE");
        short sta = (short) value.getValue().getValue();
        if(sta>=0 && sta<=5)
            return (state=AmcState.values()[sta]);
        else 
            return AmcState.UNKNOWN;
    }

    private void getAllMirrorPos(){
        double pos=0;
        double tx=0;
        double ty=0;
        String tmp;
        m1Pos="\n             Pos.     TiltX   TiltY";
        for(int i=0;i<numberOfSegments;i++){
            pos=segController.get(i).getCenterPos();
            tx=segController.get(i).getTiltX();
            ty=segController.get(i).getTiltY();
            tmp=""+(i+1);
            if(i<10) 
                tmp="0"+i;
            m1Pos+="\nSeg."+tmp+": "+String.format("%5.1f ",pos)+String.format("%5.1f ",tx)+String.format("%5.1f ",ty);
        }
        logger.info(m1Pos);
        //return pos;
    }

    public String getM1Pos() {
        getAllMirrorPos();
        return m1Pos;
    }

    

    public void startSubscription(List<NodeId> monitoredNodes){
        try {
           client.getClient().createManagedSubscription(monitoredNodes,myListener);
        } catch (UaException e) {
            e.printStackTrace();
        }
    }
    /*
     * CMD_AMC_M1_START_HOMING_ALL
     * CMD_AMC_M1_STOP_HOMING_ALL
     * CMD_AMC_M1_STOP_HOMING_MIRROR
     * CMD_AMC_M1_STOP_MIRROR
     * CMD_AMC_M1_DISABLE_ALL
     * CMD_AMC_M1_ENABLE_ALL
     * 
     */

}

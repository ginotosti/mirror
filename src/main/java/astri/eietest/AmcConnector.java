package astri.eietest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import astri.icd.AstrimaIcd;
import astri.icd.AstrimaIcdDataPoint;

/**
 * Hello world!
 *
 */
public class AmcConnector {

    private MiloClient client;
    private int motorIndex;
    private int mirrorIndex;
    private String mirror = "M1";
    private final String getMotorPrefix = "MOTOR_INFO";
    private final String getMirrorPrefix = "MIRROR_INFO";
    private String nodePrefix = "";
    private String setMotorPrefix = "MOTOR.MOTORS";
    private String setMirrorPrefix = "MIRROR";
    private String nodeMotorIndexPrefix = "_MOTOR.INDEX";
    private String nodeMirrorIndexPrefix = "_MIRROR.INDEX";
    private String nodeMotorIndex = "";
    private NodeId nodeMotorIndexId;
    private String nodeMirrorIndex;
    private NodeId nodeMirrorIndexId;
    private String cmdMirrorPrefix = setMirrorPrefix;
    private String cmdMotorPrefix = "MOTOR";
    private String cmdAmcPrefix = "AMC";
    private static final Logger logger = LoggerFactory.getLogger(AmcConnector.class);

    public AmcConnector(String host, int port, String path) {
        try {
            client = new MiloClient(host, port, path);
            client.connect();
        } catch (UaException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public MiloClient getClient() {
        return client;
    }

    public String getNodePrefix() {
        return nodePrefix;
    }

    public void setNodePrefix(String nodePrefix) {
        this.nodePrefix = nodePrefix;
        System.out.println("AMCCOnnnn:"+nodePrefix);
    }

    public Object getMotorIndex() {
        return motorIndex;
    }

    public void setMotorIndex(int motorIndex) {
        this.motorIndex = motorIndex;
    }

    public String getMirror() {
        return mirror;
    }

    public void assignToMirror(String mirror) {
        this.mirror = mirror;
        nodeMotorIndex = nodePrefix + ";s=SET_" + mirror + nodeMotorIndexPrefix;
        nodeMotorIndexId = NodeId.parse(nodeMotorIndex);
        nodeMirrorIndex = nodePrefix + ";s=SET_" + mirror + nodeMirrorIndexPrefix;
        nodeMirrorIndexId = NodeId.parse(nodeMirrorIndex);
        System.out.println("AAAAAA:"+nodeMirrorIndexId);
    }

    private NodeId getNodeId(String node) {
        System.out.println(node);
        NodeId nodeId = NodeId.parse(node);
        return nodeId;
    }

    public NodeId formMotorGetNode(String command) {
        String prefix = mirror + "_" + getMotorPrefix + "." + command.toUpperCase();
        String node = nodePrefix + ";s=" + prefix;
        return getNodeId(node);
    }

    public NodeId formAmcModeNode(String command) {
        // String prefix = mirror + "_" + getMotorPrefix + "." + command.toUpperCase();
        String node = nodePrefix + ";s=CMD_MODE."+mirror+"_GO_"+command;
        return getNodeId(node);
    }

    public NodeId formAmcInfoGetNode(String mir, String command) {
        String prefix = mir + "_" + command.toUpperCase();
        String node = nodePrefix + ";s=AMC_INFO." + prefix;
        return getNodeId(node);
    }

    public NodeId formAmcUtilGetNode(String command) {
        String prefix = command.toUpperCase();
        String node = nodePrefix + ";s=AMC_UTILITIES." + prefix;
        return getNodeId(node);
    }

    public NodeId formAmcCmdNode(String command) {
        String node = nodePrefix + ";s=CMD_AMC." + command.toUpperCase();
        return getNodeId(node);
    }

    public NodeId formMotorSetNode(String command) {
        String prefix = mirror + "_" + setMotorPrefix + "." + command.toUpperCase();
        String node = nodePrefix + ";s=SET_" + prefix;
        return getNodeId(node);
    }

    public NodeId formMirrorGetNode(String command) {
        String prefix = mirror + "_" + getMirrorPrefix + "." + command.toUpperCase();
        String node = nodePrefix + ";s=" + prefix;
        return getNodeId(node);
    }

    public NodeId formMirrorSetNode(String command) {
        String prefix = mirror + "_" + setMirrorPrefix + "." + command.toUpperCase();
        String node = nodePrefix + ";s=SET_" + prefix;
        return getNodeId(node);
    }

    public NodeId formMirrorCmdNode(String command) {
        String prefix = mirror + "." + command.toUpperCase();
        String node = nodePrefix + ";s=CMD_" + prefix;
        return getNodeId(node);
    }

    public NodeId formMotorCmdNode(String command) {
        String prefix = mirror + "." + command.toUpperCase() + "_" + cmdMotorPrefix;
        String node = nodePrefix + ";s=CMD_" + prefix;
        return getNodeId(node);
    }

    private DataValue readValue(short idx, NodeId nodeIdx, NodeId nodeId) {

        DataValue value = null;

        writeValue(nodeIdx, idx);
        value = readValue(nodeId);
        System.out.println(value);

        return value;
    }

    private void writeValue(short midx, NodeId nodeIdx, NodeId nodeId, Object value) {
        DataValue result = null;
        System.out.println("Write Value:" + midx +" "+nodeIdx+ nodeId);
        writeValue(nodeIdx, midx);
        
        writeValue(nodeId, value);
        result = readValue(nodeId);
        System.out.println("Written Value:" + result);

    }

    public DataValue readMotorValue(short midx, NodeId nodeId) {
        motorIndex = midx;
        return readValue(midx, nodeMotorIndexId, nodeId);
    }

    public void writeMotorValue(short midx, NodeId nodeId, Object value) {
        motorIndex = midx;
        writeValue(midx, nodeMotorIndexId, nodeId, value);
    }

    public DataValue executeMotorGet(short midx, String command) {
        motorIndex = midx;
        NodeId nodeId = formMotorGetNode(command);
        DataValue value = readMotorValue(midx, nodeId);
        return value;
    }

    private DataValue readValue(NodeId nodeId) {
        DataValue value = null;
        try {
            value = client.readNode(nodeId);
            System.out.println(value);
        } catch (UaException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    public DataValue executeAmcInfoGet(String mir, String command) {

        NodeId nodeId = formAmcInfoGetNode(mir, command);
        DataValue value = readValue(nodeId);
        return value;
    }

    public DataValue executeAmcUtilGet(String command) {

        NodeId nodeId = formAmcUtilGetNode(command);
        DataValue value = readValue(nodeId);
        return value;
    }

    public void executeMotorSet(short midx, String command, Object value) {
        motorIndex = midx;
        NodeId nodeId = formMotorSetNode(command);
        writeMotorValue(midx, nodeId, value);
    }

    public void executeMotorCmd(short midx, String command, Object value) {
        motorIndex = midx;
        NodeId nodeId = formMotorCmdNode(command);
        writeMotorValue(midx, nodeId, value);
    }

    public void executeMirrorCmd(short midx, String command, Object value) {
        mirrorIndex = midx;
        NodeId nodeId = formMirrorCmdNode(command);
        writeMirrorValue(midx, nodeId, value);
    }

    public void executeMirrorCmd(String command, Object value) {
        NodeId nodeId = formMirrorCmdNode(command);
        writeValue(nodeId, value);
    }

    public void writeValue(NodeId nodeId, Object value) {
        try {
            client.writeValue(nodeId, value);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void writeValue(NodeId nodeId, short value) {
        try {
            client.writeValue(nodeId, value);
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void executeAmcMode(String command,Object value) {
        // motorIndex = midx;
        NodeId nodeId = formAmcModeNode(command);
        writeValue(nodeId, value);
    }

    public void executeAmcCmd(String command, Object value) {
        // motorIndex = midx;
        NodeId nodeId = formAmcCmdNode(command);
        writeValue(nodeId, value);
    }

    public DataValue readMirrorValue(short midx, NodeId nodeId) {
        mirrorIndex = midx;
        return readValue(midx, nodeMirrorIndexId, nodeId);
    }

    public void writeMirrorValue(short midx, NodeId nodeId, Object value) {
        mirrorIndex = midx;
        writeValue(midx, nodeMirrorIndexId, nodeId, value);
    }

    public DataValue executeMirrorGet(short midx, String command) {
        mirrorIndex = midx;
        NodeId nodeId = formMirrorGetNode(command);
        DataValue value = readMirrorValue(midx, nodeId);
        return value;
    }

    public void executeMirrorSet(short midx, String command, Object value) {
        mirrorIndex = midx;
        NodeId nodeId = formMirrorSetNode(command);
        logger.info("adding:"+nodeId+" "+midx);
        writeMirrorValue(midx, nodeId, value);
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
        // String url="tcp.opc://127.0.0.1:42555/OPCUA/AMA_AMC_Server";
       /*  AstrimaIcd icd = new AstrimaIcd("ICD_AMC_V10_2022_09_14c.xlsx");
        String url = icd.getOpcUaServerAddress();
        Map<String, AstrimaIcdDataPoint> getpoints = icd.getMonitoringPoints();
        List<NodeId> nodes=new ArrayList<>();
        getpoints.forEach((name,val)->{
            //logger.info("adding:"+val.getOperationModes()[0]);
            if(val.getOpcUaNode().contains("M1") && val.getIsMonitored().contains("Y"))
                nodes.add(NodeId.parse(val.getOpcUaNode().replace("=4","=4")));
        });*/
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

        //AmcConnector amc = new AmcConnector("192.168.2.2", 4840, "");
        MiloClient tm = new MiloClient("127.0.0.1",52523,"/OPCUA/AMA_AMC_Server");
        //amc.assignToMirror("M1");

        // Thread.sleep(5000);
        //NodeId node = NodeId.parse("ns=4;s=AMC_INFO.M1_ERROR_NUMBER");
        //amc.getClient().createManagedSubscription(nodes,myListener);
        // tm.createSubsciption(node);
        tm.writeValue(NodeId.parse("ns=2;s=CMD_MODE.M1_GO_STANDBY"),true);
        short a =(6);
        tm.writeValue(NodeId.parse("ns=2;s=SET_M1_MIRROR.INDEX"),a);
        DataValue valu=tm.readNode(NodeId.parse("ns=2;s=SET_M1_MIRROR.INDEX"));
        logger.info("adding:"+valu.getValue().getValue());
        double pos=5.0;
        //amc.executeMirrorSet(2, "CENTER_POS",pos);
        double val=1.1;
        tm.writeValue(NodeId.parse("ns=2;s=SET_M1_MIRROR.TILT_Y"),val);
        valu=tm.readNode(NodeId.parse("ns=2;s=SET_M1_MIRROR.TILT_Y"));
        logger.info("adding:"+valu.getValue().getValue());
        // tm.testReadAsync();
        // tm.getServerInfo();
        // tm.readNode();
        Thread.sleep(2000);
        //amc.disconnect();
        tm.disconnect();

    }

    
}

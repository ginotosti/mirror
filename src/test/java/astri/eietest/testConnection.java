package astri.eietest;

import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class testConnection {
    private static final Logger logger = LoggerFactory.getLogger(testConnection.class);
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {

        MiloClient tm = new MiloClient("10.10.1.56",4840,"");

        double val=1.5;
        tm.writeValue(NodeId.parse("ns=4;s=MCS_SET_POINTING.TARGET_DEC"),val);
        DataValue valu=tm.readNode(NodeId.parse("ns=4;s=MCS_MOTORS_INFO.AZ_ENCODER_RPM_MAX"));
        logger.info("adding:"+valu.getValue().getValue());
        tm.disconnect();
    }
    
}

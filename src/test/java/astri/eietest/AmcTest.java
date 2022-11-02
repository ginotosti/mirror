package astri.eietest;

import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.junit.Test;

import astri.icd.AstrimaIcd;

/**
 * Unit test for simple App.
 */
public class AmcTest {
    public static void main(String[] args) throws UaException, InterruptedException, ExecutionException {
        Locale.setDefault(new Locale("en", "US"));
        // String url="tcp.opc://127.0.0.1:42555/OPCUA/AMA_AMC_Server";
       /*  AstrimaIcd icd = new AstrimaIcd("ICD_AMC_V10_2022_09_14.xlsx");
        String url = icd.getOpcUaServerAddress();
        icd.close();
        MotorConfiguration motConf=new MotorConfiguration();
        motConf.motorMaxAcc=10.0;

        AmcConnector tm = new AmcConnector("127.0.0.1", 52523, "/OPCUA/AMA_AMC_Server");
        tm.setNodePrefix("ns=2");
        tm.assignToMirror("M1");
        MotorController mc = new MotorController(tm);
        //MotorConfiguration mConf = new MotorConfiguration();
        mc.setMotorConfiguration(motConf);
        mc.assignToSegment(1);
        mc.setMotorIndex(1);
        //MirrorSegmentController msc = new MirrorSegmentController("M1", 1, tm);
        MirrorController msc = new MirrorController("M1", 3, tm);
        msc.setMotorConf(motConf);
        msc.createSegmentControllers();
        //msc.getMotor(1).writeConfiguration(motConf);
        //MotorConfiguration a = msc.getMotor(1).readCurrentConfiguration();
        msc.getSegmentController(1).setCenterOffset(15.5d);
        //double b= msc.getSegmentController(1).getCenterOffset();
        double  b= msc.getSegmentController(3).getMotor1().getMotorPos();
        System.out.println(b);
        /*msc.getSegmentController(2).getMotor(1).writeConfiguration(motConf);
        MotorConfiguration a = msc.getSegmentController(2).getMotor(2).readCurrentConfiguration();
        System.out.println(a);
        tm.disconnect();
        */
        AmcController amc =new AmcController();
        amc.getPrimaryMirrorController().goOnline();
        amc.getPrimaryMirrorController().getMirrorState();

        //amc.getSecondaryMirrorController().getSegmentController(1).getMotor1().getMotorPos();
        amc.getPrimaryMirrorErrorNumber();
        amc.getPrimaryMirrorSegmentsPos();
        //Thread.sleep(10000);
        amc.close();
        
    }
}

package astri.eietest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MotorConfiguration {

    public double motorMaxAcc = 0.0;
    public double motorMaxDec = 0.0;
    public double motorMaxVel = 0.0;
    public double motorMaxPos = 0.0;
    public double motorMinAcc = 0.0;
    public double motorMinDec = 0.0;
    public double motorMinVel = 0.0;
    public double motorMinPos = 0.0;
    public double motorVelHoming = 0.0;

    public MotorConfiguration() {

    }

    public MotorConfiguration(Properties prop) {
        motorVelHoming = Double.parseDouble(prop.getProperty("motorVelHoming"));
        motorMinVel = Double.parseDouble(prop.getProperty("motorMinVel"));
        motorMinDec = Double.parseDouble(prop.getProperty("motorMinDec"));
        motorMaxAcc = Double.parseDouble(prop.getProperty("motorMaxAcc"));
        motorMaxPos = Double.parseDouble(prop.getProperty("motorMaxPos"));
        motorMaxVel = Double.parseDouble(prop.getProperty("motorMaxVel"));
        motorMaxDec = Double.parseDouble(prop.getProperty("motorMaxDec"));
        motorMinAcc = Double.parseDouble(prop.getProperty("motorMinAcc"));
        motorMinPos = Double.parseDouble(prop.getProperty("motorMinPos"));
    }

    @Override
    public String toString() {
        return "MotorConfiguration [motorMaxAcc=" + motorMaxAcc + ", motorMaxDec=" + motorMaxDec + ", motorMaxVel="
                + motorMaxVel + ", motorMaxPos=" + motorMaxPos + ", motorMinAcc=" + motorMinAcc + ", motorMinDec="
                + motorMinDec + ", motorMinVel=" + motorMinVel + ", motorMinPos=" + motorMinPos + ", motorVelHoming="
                + motorVelHoming + "]";
    }

}

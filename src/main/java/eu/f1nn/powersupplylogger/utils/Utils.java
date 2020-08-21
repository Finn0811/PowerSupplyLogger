package eu.f1nn.powersupplylogger.utils;

/**
 * Created by Finn on 16.08.2020.
 */
public class Utils {

    public static double calculateVoltage(double raw) {
        return (raw * 3.3) / 1023;
    }

    public static double calculateInputVoltage(double raw, int r2, int rTotal) {
        return (raw * rTotal) / r2;
    }

    public static double calculateCurrent(double value) {
//        return ((value - 28) / 1023.0) * 20;
//        return (0.0631 * value) + 2.5197;
        return (value - 2.5197) / 0.0631;
    }
}

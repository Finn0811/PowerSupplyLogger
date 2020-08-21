package eu.f1nn.powersupplylogger.controller;

import com.pi4j.io.gpio.GpioPinAnalogInput;
import eu.f1nn.powersupplylogger.model.MeasureType;
import eu.f1nn.powersupplylogger.model.Measurement;
import eu.f1nn.powersupplylogger.model.PowerSupply;
import eu.f1nn.powersupplylogger.utils.Utils;

import java.text.DecimalFormat;


/**
 * Created by Finn on 16.08.2020.
 */
public class VoltageMeasureTask extends MeasureTask {

    public VoltageMeasureTask(PowerSupply powerSupply, String serverUrl, DecimalFormat df, GpioPinAnalogInput input) {
        super(powerSupply, serverUrl, df, input);
    }

    public void run() {
        this.getLogger().info("Measuring voltage...");

        this.takeSamples();

        double averageValue = this.getAverage();

        this.getLogger().info("Average value: " + averageValue);

        double voltageDivider = Utils.calculateVoltage(averageValue);
        double inputVoltage = Utils.calculateInputVoltage(voltageDivider, 1000, 11000);

        this.getLogger().info(averageValue + " -> " + this.getDf().format(voltageDivider) + "V -> " + this.getDf().format(inputVoltage) + "V");
        this.getLogger().info("--------------------------------");

        this.addMeasurement(new Measurement(this.getPowerSupply(), inputVoltage, MeasureType.VOLTAGE, System.currentTimeMillis()));
        this.sendMeasurementsToServer();
    }
}

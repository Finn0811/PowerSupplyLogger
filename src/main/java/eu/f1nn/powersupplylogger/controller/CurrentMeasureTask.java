package eu.f1nn.powersupplylogger.controller;

import com.pi4j.io.gpio.GpioPinAnalogInput;
import eu.f1nn.powersupplylogger.model.MeasureType;
import eu.f1nn.powersupplylogger.model.Measurement;
import eu.f1nn.powersupplylogger.model.PowerSupply;
import eu.f1nn.powersupplylogger.utils.Utils;

import java.text.DecimalFormat;


/**
 * Created by Finn on 17.08.2020.
 */
public class CurrentMeasureTask extends MeasureTask {

    public CurrentMeasureTask(PowerSupply powerSupply, String serverUrl, DecimalFormat df, GpioPinAnalogInput input) {
        super(powerSupply, serverUrl, df, input);
    }

    public void run() {
        this.getLogger().info("Measuring current...");

        this.takeSamples();

        double averageValue = this.getAverage();

        this.getLogger().info("Average value: " + averageValue);

        double voltage = Utils.calculateVoltage(averageValue);
        double current = Utils.calculateCurrent(voltage);

        this.getLogger().info(averageValue + " -> " +  this.getDf().format(voltage) + "V -> " +  this.getDf().format(current) + "A");
        this.getLogger().info("--------------------------------");

        this.addMeasurement(new Measurement(this.getPowerSupply(), current, MeasureType.CURRENT, System.currentTimeMillis()));
        this.sendMeasurementsToServer();
    }
}

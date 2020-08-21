package eu.f1nn.powersupplylogger.model;

import java.io.Serializable;

/**
 * Created by Finn on 10.07.2020.
 */
public class Measurement implements Serializable {
    public int power_supply;
    public double value;
    public MeasureType type;
    public long timestamp;

    public Measurement(PowerSupply powerSupply, double value, MeasureType type, long timestamp) {
        this.power_supply = powerSupply.id;
        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
    }
}

package eu.f1nn.powersupplylogger.model;

import java.io.Serializable;

/**
 * Created by Finn on 10.07.2020.
 */
public class PowerSupply implements Serializable {
    public int id;
    public String name;
    public float batteryCapacity;

    public PowerSupply(int id, String name, float batteryCapacity) {
        this.id = id;
        this.name = name;
        this.batteryCapacity = batteryCapacity;
    }
}

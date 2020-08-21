package eu.f1nn.powersupplylogger.controller;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by Finn on 16.08.2020.
 */
public class RelaisController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private GpioPinDigitalOutput batteryFaultPin;
    private GpioPinDigitalOutput acFaultPin;

    private List<GpioPinDigitalOutput> activePins = new LinkedList<>();

    public RelaisController(GpioPinDigitalOutput batteryFaultPin, GpioPinDigitalOutput acFaultPin) {
        this.batteryFaultPin = batteryFaultPin;
        this.acFaultPin = acFaultPin;
    }

    public void triggerBatteryFault() {
        this.triggerPin(this.batteryFaultPin);
    }

    public void triggerAcFault() {
        this.triggerPin(this.acFaultPin);
    }

    private void triggerPin(GpioPinDigitalOutput pin) {
        if (activePins.contains(pin)) {
            logger.warn(pin.getName() + " is already active. Ignoring...");
            return;
        }

        pin.high();
        activePins.add(pin);

        logger.info("Triggered " + pin.getName());

        scheduler.schedule(() -> {
            pin.low();
            activePins.remove(pin);
        }, 10, TimeUnit.SECONDS);
    }
}

package eu.f1nn.powersupplylogger;

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import eu.f1nn.powersupplylogger.controller.CurrentMeasureTask;
import eu.f1nn.powersupplylogger.controller.RelaisController;
import eu.f1nn.powersupplylogger.controller.VoltageMeasureTask;
import eu.f1nn.powersupplylogger.model.PowerSupply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Finn on 10.07.2020.
 */
public class PowerSupplyLogger {
    private static final Logger logger = LoggerFactory.getLogger(PowerSupplyLogger.class);
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final GpioController gpio = GpioFactory.getInstance();
    private static AdcGpioProvider provider;

    public static void main(String[] args) {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.saveDefaultConfig(PowerSupplyLogger.class.getResourceAsStream("config.yml"));
        configHandler.loadConfig();

        df.setRoundingMode(RoundingMode.CEILING);

        try {
            provider = new MCP3008GpioProvider(SpiChannel.CS0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PowerSupply powerSupply = new PowerSupply(configHandler.getConfig().powerSupplyId, configHandler.getConfig().powerSupplyName, configHandler.getConfig().batteryCapacity);

        GpioPinAnalogInput currentInput = gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH1, "CurrentInput-CH1");
        GpioPinAnalogInput voltageInput = gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH2, "VoltageInput-CH2");

        GpioPinDigitalInput isAlarmInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_DOWN);

        CurrentMeasureTask currentMeasureTask = new CurrentMeasureTask(powerSupply, configHandler.getConfig().serverUrl + "/rest/measurements", df, currentInput, isAlarmInput);
        VoltageMeasureTask voltageMeasureTask = new VoltageMeasureTask(powerSupply, configHandler.getConfig().serverUrl + "/rest/measurements", df, voltageInput, isAlarmInput);

        scheduler.scheduleAtFixedRate(currentMeasureTask, 0, 120, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(voltageMeasureTask, 0, 120, TimeUnit.SECONDS);

        GpioPinDigitalOutput batteryFaultPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "batteryFault");
        GpioPinDigitalOutput acFaultPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "acFault");

        RelaisController relaisController = new RelaisController(batteryFaultPin, acFaultPin);

        MqttListener mqttListener = new MqttListener(configHandler.getConfig().mqttUrl, configHandler.getConfig().powerSupplyName,
                configHandler.getConfig().powerSupplyId, relaisController);
        mqttListener.connect();

        logger.info("PowerSupplyLogger started...");
    }
}

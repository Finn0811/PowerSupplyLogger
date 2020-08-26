package eu.f1nn.powersupplylogger.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import eu.f1nn.powersupplylogger.model.Measurement;
import eu.f1nn.powersupplylogger.model.PowerSupply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;


/**
 * Created by Finn on 16.08.2020.
 */
public abstract class MeasureTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    private DecimalFormat df;
    private GpioPinAnalogInput input;
    private GpioPinDigitalInput isAlarmInput;
    private PowerSupply powerSupply;

    private List<Measurement> measurements = new LinkedList<>();
    private String serverUrl;

    public MeasureTask(PowerSupply powerSupply, String serverUrl, DecimalFormat df, GpioPinAnalogInput input, GpioPinDigitalInput isAlarmInput) {
        this.powerSupply = powerSupply;
        this.serverUrl = serverUrl;
        this.df = df;
        this.input = input;
        this.isAlarmInput = isAlarmInput;
    }

    public double takeSamples() {
        List<Double> samples = new LinkedList<>();

        IntStream.range(0, 10).forEach(i -> {
            double value = input.getValue();

            samples.add(value);
            logger.info("Took sample (" + value + ")");
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        AtomicReference<Double> total = new AtomicReference<>(0.0);
        samples.forEach((sample) -> total.set(total.get() + sample));

        return total.get() / samples.size();
    }

    public boolean sendMeasurementsToServer() {
        new LinkedList<>(this.measurements).forEach(measurement -> {
            try {
                this.sendMeasurementToServer(measurement);
                this.measurements.remove(measurement);
            } catch (Exception e) {
                logger.error("Could not send measurement (#" + measurement.timestamp + ") to server. Saving for next request.");
            }
        });
        return true;
    }

    public void sendMeasurementToServer(Measurement measurement) throws Exception {
        // https://www.baeldung.com/httpurlconnection-post

        URL url = new URL(this.serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(GSON.toJson(measurement).getBytes(StandardCharsets.UTF_8));
        }

        int code = connection.getResponseCode();

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;
        while ((line = in.readLine()) != null)
            response.append(line);
        in.close();

        logger.info(code + ": " + response.toString());


    }

    public Logger getLogger() {
        return this.logger;
    }

    public DecimalFormat getDf() {
        return this.df;
    }


    public PowerSupply getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(PowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurement) {
        this.measurements = measurement;
    }

    public void addMeasurement(Measurement measurement) {
        this.measurements.add(measurement);
    }

    public GpioPinDigitalInput getIsAlarmInput() {
        return isAlarmInput;
    }

    public void setIsAlarmInput(GpioPinDigitalInput isAlarmInput) {
        this.isAlarmInput = isAlarmInput;
    }

    public boolean isAlarmMeasurement() {
        return this.isAlarmInput.getState().isHigh();
    }
}

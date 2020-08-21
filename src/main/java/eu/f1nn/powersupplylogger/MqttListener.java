package eu.f1nn.powersupplylogger;

import com.google.gson.Gson;
import eu.f1nn.powersupplylogger.controller.RelaisController;
import eu.f1nn.powersupplylogger.model.MqttControlMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Finn on 19.08.2020.
 */
public class MqttListener implements MqttCallback {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String topic = "powersupply/trigger/";
    private final String broker;
    private final String clientId;
    private final int powerSupplyId;
    private final RelaisController relaisController;

    public MqttListener(String broker, String clientId, int powerSupplyId, RelaisController relaisController) {
        this.broker = broker;
        this.clientId = clientId;
        this.powerSupplyId = powerSupplyId;
        this.relaisController = relaisController;
    }

    public void connect() {
        try {
            MqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);
            logger.info("Connecting to broker: " + broker);
            client.connect(connOpts);
            logger.info("Connected");

            client.setCallback(this);
            client.subscribe(this.topic);
        } catch (
                MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        logger.info(mqttMessage.toString());

        Gson gson = new Gson();
        MqttControlMessage mqttControlMessage = gson.fromJson(mqttMessage.toString(), MqttControlMessage.class);

        if (mqttControlMessage.getId() == this.powerSupplyId) {
            if (mqttControlMessage.getTrigger().equalsIgnoreCase("batteryFault")) {
                relaisController.triggerBatteryFault();
            }

            if (mqttControlMessage.getTrigger().equalsIgnoreCase("acFault")) {
                relaisController.triggerAcFault();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}

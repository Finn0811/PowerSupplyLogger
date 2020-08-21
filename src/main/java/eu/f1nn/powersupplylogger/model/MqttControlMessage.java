package eu.f1nn.powersupplylogger.model;

/**
 * Created by Finn on 19.08.2020.
 */
public class MqttControlMessage {
    private int id;
    private String trigger;

    public MqttControlMessage() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
}

// model/DeliveryAgent.java
package model;

public class DeliveryAgent {
    public int agentId;
    public String name;
    public boolean available;

    public DeliveryAgent(int agentId, String name, boolean available) {
        this.agentId = agentId;
        this.name = name;
        this.available = available;
    }
}
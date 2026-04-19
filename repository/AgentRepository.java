// repository/AgentRepository.java
package repository;

import model.DeliveryAgent;
import java.util.*;

public class AgentRepository {

    private List<DeliveryAgent> agents = new ArrayList<>();

    public AgentRepository() {
        // dummy agents
        agents.add(new DeliveryAgent(1, "Ravi", true));
        agents.add(new DeliveryAgent(2, "Amit", true));
        agents.add(new DeliveryAgent(3, "John", false));
    }

    public DeliveryAgent findAvailableAgent() {
        for (DeliveryAgent a : agents) {
            if (a.available) return a;
        }
        return null;
    }

    public void markUnavailable(int agentId) {
        for (DeliveryAgent a : agents) {
            if (a.agentId == agentId) {
                a.available = false;
            }
        }
    }

    public DeliveryAgent findAgentById(int agentId) {
        for (DeliveryAgent a : agents) {
            if (a.agentId == agentId) return a;
        }
        return null;
    }
}
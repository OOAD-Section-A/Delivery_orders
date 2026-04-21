// service/DeliveryStatusManager.java
package service;

import java.util.*;

public class DeliveryStatusManager {

    private static final Map<String, List<String>> validTransitions = new HashMap<>();

    static {
        validTransitions.put("CREATED", Arrays.asList("PACKED"));
        validTransitions.put("PACKED", Arrays.asList("DISPATCHED"));
        validTransitions.put("DISPATCHED", Arrays.asList("OUT_FOR_DELIVERY"));
        validTransitions.put("OUT_FOR_DELIVERY", Arrays.asList("DELIVERED"));
        validTransitions.put("DELIVERED", new ArrayList<>());
    }

    public boolean isValidTransition(String current, String next) {
        return validTransitions.containsKey(current) &&
               validTransitions.get(current).contains(next);
    }
}
// repository/DeliveryRepository.java
package repository;

import model.DeliveryOrder;
import java.util.HashMap;

public class DeliveryRepository {

    private HashMap<Integer, DeliveryOrder> db = new HashMap<>();

    public void save(DeliveryOrder order) {
        db.put(order.deliveryId, order);
    }

    public DeliveryOrder find(int deliveryId) {
        return db.get(deliveryId);
    }
}
// integration/PackingWarehouseAdapter.java
package integration;

import integration.IDeliveryPackingGateway.PackedJobInfo;

import java.util.List;

/**
 * Adapter that bridges the delivery subsystem's {@link WarehouseService}
 * interface to the packing subsystem's {@link IDeliveryPackingGateway}.
 *
 * <p>This replaces {@link MockWarehouseService} for production use.
 * Instead of blindly returning {@code true}, it queries the packing
 * gateway to verify that a real packed job exists for the given order.</p>
 *
 * <p><b>Design Pattern — Adapter (Structural):</b> Translates between
 * the delivery subsystem's API ({@code isPacked(int orderId)}) and the
 * packing subsystem's API ({@code getPackedJobs()} returning
 * {@code PackedJobInfo} with {@code String orderId}).</p>
 *
 * <p><b>SOLID — Dependency Inversion:</b> Depends on the
 * {@code IDeliveryPackingGateway} abstraction, not on the concrete
 * {@code DeliveryPackingGateway} from the packing JAR.</p>
 */
public class PackingWarehouseAdapter implements WarehouseService {

    private final IDeliveryPackingGateway packingGateway;

    /**
     * @param packingGateway the packing subsystem gateway to query
     */
    public PackingWarehouseAdapter(IDeliveryPackingGateway packingGateway) {
        this.packingGateway = packingGateway;
        System.out.println("[PackingWarehouseAdapter] Connected to packing gateway.");
    }

    /**
     * Checks whether the given order has been packed by querying the
     * packing subsystem for a job with a matching order ID in PACKED status.
     *
     * <p><b>Type bridging:</b> The delivery subsystem uses {@code int orderId}
     * while the packing subsystem uses {@code String orderId}. This method
     * converts via {@code String.valueOf(orderId)}.</p>
     *
     * @param orderId the delivery order ID to check
     * @return {@code true} if the packing subsystem has a PACKED job
     *         for this order
     */
    @Override
    public boolean isPacked(int orderId) {
        String orderIdStr = String.valueOf(orderId);

        List<PackedJobInfo> packedJobs = packingGateway.getPackedJobs();

        boolean packed = packedJobs.stream()
                .anyMatch(job -> orderIdStr.equals(job.getOrderId()));

        if (packed) {
            System.out.println("[PackingWarehouseAdapter] ✅ Order " + orderId
                    + " confirmed packed by packing subsystem.");
        } else {
            System.out.println("[PackingWarehouseAdapter] ⚠ Order " + orderId
                    + " NOT found in packed jobs. (" + packedJobs.size()
                    + " packed jobs available)");
        }

        return packed;
    }
}

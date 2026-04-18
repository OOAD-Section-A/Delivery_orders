// integration/IDeliveryPackingGateway.java
package integration;

import java.util.List;
import java.util.Optional;

/**
 * Delivery-facing integration contract for consuming packed artifacts
 * from the Packing subsystem.
 *
 * <p><b>Source:</b> This is a local copy of the interface defined in the
 * packing-repairs-receiptmanagement repository at
 * {@code com.scm.packing.integration.delivery.IDeliveryPackingGateway}.
 * When the packing JAR is on the classpath, use the JAR's version instead.</p>
 *
 * <p><b>Design Pattern — Adapter (Structural):</b> Our adapters
 * (e.g. {@code PackingWarehouseAdapter}) depend on this interface,
 * not on the concrete gateway class.</p>
 */
public interface IDeliveryPackingGateway {

    /**
     * Returns all packing jobs that have reached PACKED status
     * and are ready for delivery pickup.
     */
    List<PackedJobInfo> getPackedJobs();

    /**
     * Returns a single packed job if it exists and is in PACKED status.
     */
    Optional<PackedJobInfo> getPackedJob(String jobId);

    /**
     * Returns the generated barcode string for a packed job.
     */
    Optional<String> getBarcodeForJob(String jobId);

    /**
     * Returns all packing units (pallets) created by the packing subsystem.
     */
    List<PackingUnitInfo> getAllPackingUnits();

    // -----------------------------------------------------------------
    // Nested record-style classes — lightweight DTOs that mirror the
    // packing subsystem's domain objects without requiring the JAR.
    // -----------------------------------------------------------------

    /**
     * Lightweight representation of a packed job.
     */
    class PackedJobInfo {
        private final String jobId;
        private final String orderId;
        private final String status;
        private final int progress;
        private final int itemCount;

        public PackedJobInfo(String jobId, String orderId, String status,
                             int progress, int itemCount) {
            this.jobId = jobId;
            this.orderId = orderId;
            this.status = status;
            this.progress = progress;
            this.itemCount = itemCount;
        }

        public String getJobId()   { return jobId; }
        public String getOrderId() { return orderId; }
        public String getStatus()  { return status; }
        public int getProgress()   { return progress; }
        public int getItemCount()  { return itemCount; }

        @Override
        public String toString() {
            return String.format("PackedJob[%s, order=%s, status=%s, %d%%]",
                    jobId, orderId, status, progress);
        }
    }

    /**
     * Lightweight representation of a packing unit (pallet).
     */
    class PackingUnitInfo {
        private final String unitId;
        private final int jobCount;
        private final double totalWeightKg;

        public PackingUnitInfo(String unitId, int jobCount, double totalWeightKg) {
            this.unitId = unitId;
            this.jobCount = jobCount;
            this.totalWeightKg = totalWeightKg;
        }

        public String getUnitId()        { return unitId; }
        public int getJobCount()         { return jobCount; }
        public double getTotalWeightKg() { return totalWeightKg; }

        @Override
        public String toString() {
            return String.format("Pallet[%s, %d jobs, %.2f kg]",
                    unitId, jobCount, totalWeightKg);
        }
    }
}

// integration/StubDeliveryPackingGateway.java
package integration;

import integration.IDeliveryPackingGateway.PackedJobInfo;
import integration.IDeliveryPackingGateway.PackingUnitInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Test stub that simulates the packing subsystem's
 * {@code DeliveryPackingGateway} with in-memory sample data.
 *
 * <p><b>Usage:</b> Use this for standalone testing of the delivery
 * subsystem. When the real packing JAR is on the classpath, replace
 * this with {@code new DeliveryPackingGateway(packingModel)}.</p>
 *
 * <p><b>Design Pattern — Stub / Test Double:</b> Provides deterministic
 * data for integration testing without requiring a live packing system.</p>
 */
public class StubDeliveryPackingGateway implements IDeliveryPackingGateway {

    private final Map<String, PackedJobInfo> packedJobs = new LinkedHashMap<>();
    private final Map<String, String> barcodes = new LinkedHashMap<>();
    private final List<PackingUnitInfo> pallets = new ArrayList<>();

    /**
     * Creates a stub gateway pre-loaded with sample packed jobs.
     */
    public StubDeliveryPackingGateway() {
        // Sample packed jobs — orderId matches what Main.java uses
        addPackedJob("WMS-PKJ-0001", "101", "PACKED", 100, 3,
                "SCM|WMS-PKJ-0001|101|20260418-120000");
        addPackedJob("WMS-PKJ-0002", "102", "PACKED", 100, 2,
                "SCM|WMS-PKJ-0002|102|20260418-120500");
        addPackedJob("WMS-PKJ-0003", "103", "PACKING", 65, 4, null);

        // Sample pallet
        pallets.add(new PackingUnitInfo("PLT-001", 2, 12.50));
    }

    /**
     * Adds a packed job to the stub data. Useful for custom test scenarios.
     */
    public void addPackedJob(String jobId, String orderId, String status,
                             int progress, int itemCount, String barcode) {
        packedJobs.put(jobId, new PackedJobInfo(jobId, orderId, status, progress, itemCount));
        if (barcode != null) {
            barcodes.put(jobId, barcode);
        }
    }

    @Override
    public List<PackedJobInfo> getPackedJobs() {
        return packedJobs.values().stream()
                .filter(j -> "PACKED".equals(j.getStatus()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<PackedJobInfo> getPackedJob(String jobId) {
        PackedJobInfo job = packedJobs.get(jobId);
        if (job == null || !"PACKED".equals(job.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(job);
    }

    @Override
    public Optional<String> getBarcodeForJob(String jobId) {
        return Optional.ofNullable(barcodes.get(jobId));
    }

    @Override
    public List<PackingUnitInfo> getAllPackingUnits() {
        return Collections.unmodifiableList(pallets);
    }
}

// integration/PackingInfoService.java
package integration;

import integration.IDeliveryPackingGateway.PackedJobInfo;
import integration.IDeliveryPackingGateway.PackingUnitInfo;

import java.util.List;
import java.util.Optional;

/**
 * Service providing delivery-side access to packing metadata: barcode
 * labels, packed job details, and pallet/unit information.
 *
 * <p>This extends the delivery subsystem's capabilities beyond the simple
 * {@code isPacked()} check, allowing it to:</p>
 * <ul>
 *   <li>Print barcode labels on delivery receipts</li>
 *   <li>Inspect packed job contents for dispatch planning</li>
 *   <li>Read pallet data for truck loading optimization</li>
 * </ul>
 *
 * <p><b>Design Pattern — Façade:</b> Provides a simplified, delivery-focused
 * view of the packing gateway's full API.</p>
 */
public class PackingInfoService {

    private final IDeliveryPackingGateway gateway;

    public PackingInfoService(IDeliveryPackingGateway gateway) {
        this.gateway = gateway;
    }

    // -----------------------------------------------------------------
    // Barcode operations
    // -----------------------------------------------------------------

    /**
     * Retrieves the barcode label string for a given packing job.
     *
     * @param jobId the packing job ID (e.g. "WMS-PKJ-0001")
     * @return the barcode string, or empty if not generated yet
     */
    public Optional<String> getBarcodeForJob(String jobId) {
        return gateway.getBarcodeForJob(jobId);
    }

    /**
     * Prints barcode info to the console for the given job.
     * Returns {@code true} if a barcode was found.
     */
    public boolean printBarcodeInfo(String jobId) {
        Optional<String> barcode = getBarcodeForJob(jobId);
        if (barcode.isPresent()) {
            System.out.println("🏷  Barcode for " + jobId + ": " + barcode.get());
            return true;
        } else {
            System.out.println("⚠  No barcode available for " + jobId);
            return false;
        }
    }

    // -----------------------------------------------------------------
    // Packed job queries
    // -----------------------------------------------------------------

    /**
     * Returns full details of a packed job.
     */
    public Optional<PackedJobInfo> getPackedJobDetails(String jobId) {
        return gateway.getPackedJob(jobId);
    }

    /**
     * Returns all jobs that are packed and ready for delivery.
     */
    public List<PackedJobInfo> getAllPackedJobs() {
        return gateway.getPackedJobs();
    }

    /**
     * Prints a summary of all packed jobs ready for delivery pickup.
     */
    public void printPackedJobsSummary() {
        List<PackedJobInfo> jobs = getAllPackedJobs();
        System.out.println("\n📋 Packed Jobs Ready for Delivery: " + jobs.size());
        System.out.println("─".repeat(55));
        System.out.printf("  %-16s %-12s %-10s %-8s%n",
                "Job ID", "Order ID", "Status", "Items");
        System.out.println("─".repeat(55));
        for (PackedJobInfo job : jobs) {
            System.out.printf("  %-16s %-12s %-10s %-8d%n",
                    job.getJobId(), job.getOrderId(),
                    job.getStatus(), job.getItemCount());
        }
        System.out.println("─".repeat(55));
    }

    // -----------------------------------------------------------------
    // Pallet / unit queries
    // -----------------------------------------------------------------

    /**
     * Returns all packing units (pallets) from the packing subsystem.
     */
    public List<PackingUnitInfo> getAllPallets() {
        return gateway.getAllPackingUnits();
    }

    /**
     * Prints a summary of all pallets for dispatch planning.
     */
    public void printPalletSummary() {
        List<PackingUnitInfo> pallets = getAllPallets();
        System.out.println("\n📦 Packing Units (Pallets): " + pallets.size());
        System.out.println("─".repeat(45));
        System.out.printf("  %-12s %-10s %-14s%n",
                "Pallet ID", "Jobs", "Weight (kg)");
        System.out.println("─".repeat(45));
        for (PackingUnitInfo pallet : pallets) {
            System.out.printf("  %-12s %-10d %-14.2f%n",
                    pallet.getUnitId(), pallet.getJobCount(),
                    pallet.getTotalWeightKg());
        }
        System.out.println("─".repeat(45));
    }
}

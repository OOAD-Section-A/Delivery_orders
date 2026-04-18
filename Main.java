// Main.java

import service.DeliveryOrderService;
import repository.DeliveryRepository;
import repository.AgentRepository;
import validator.DeliveryValidator;

import integration.*;

import exception.*;

public class Main {

    public static void main(String[] args) {

        // 🔧 Setup core components
        DeliveryRepository repo = new DeliveryRepository();
        AgentRepository agentRepo = new AgentRepository();
        DeliveryValidator validator = new DeliveryValidator();

        // ─────────────────────────────────────────────────────
        // 🔌 PACKING SUBSYSTEM INTEGRATION
        // ─────────────────────────────────────────────────────
        // The StubDeliveryPackingGateway simulates the packing
        // subsystem's DeliveryPackingGateway with sample data.
        //
        // To switch to the real packing JAR:
        //   1. Add packing-subsystem-1.0-SNAPSHOT-all.jar to classpath
        //   2. Replace the line below with:
        //      IDeliveryPackingGateway packingGateway =
        //          new DeliveryPackingGateway(packingModel);
        // ─────────────────────────────────────────────────────
        IDeliveryPackingGateway packingGateway = new StubDeliveryPackingGateway();

        // Real adapter — queries the packing gateway instead of returning hardcoded true
        WarehouseService warehouseService = new PackingWarehouseAdapter(packingGateway);

        // Barcode & pallet info service — new capability from packing integration
        PackingInfoService packingInfo = new PackingInfoService(packingGateway);

        // Other external systems (still mocked — replace with real adapters later)
        OrderFulfillmentService orderService = new MockOrderService();
        TrackingService trackingService = new MockTrackingService();

        // ⚙️ Initialize main service
        DeliveryOrderService service = new DeliveryOrderService(
                repo,
                validator,
                orderService,
                warehouseService,
                trackingService,
                agentRepo
        );

        // ═══════════════════════════════════════════════════════
        // DEMO 1: Packing Integration — View Packed Jobs & Barcodes
        // ═══════════════════════════════════════════════════════

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  PACKING SUBSYSTEM INTEGRATION DEMO");
        System.out.println("══════════════════════════════════════════════");

        // Show all packed jobs ready for delivery
        packingInfo.printPackedJobsSummary();

        // Show pallet data for dispatch planning
        packingInfo.printPalletSummary();

        // Look up barcode for a specific job
        System.out.println();
        packingInfo.printBarcodeInfo("WMS-PKJ-0001");
        packingInfo.printBarcodeInfo("WMS-PKJ-0002");
        packingInfo.printBarcodeInfo("WMS-PKJ-0003"); // in-progress — no barcode

        // ═══════════════════════════════════════════════════════
        // DEMO 2: Delivery Flow — Order 101 (PACKED ✅)
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 101 (Packed)");
            System.out.println("══════════════════════════════════════════════\n");

            // 📦 Step 1: Create Delivery — packing check goes through the real adapter
            service.createDelivery(1, 101, 5001, "Bangalore");

            // 🚚 Step 2: Assign Agent
            service.assignAgent(1);

            // 🔄 Step 3: Status Transitions
            service.updateStatus(1, "PACKED");
            service.updateStatus(1, "DISPATCHED");
            service.updateStatus(1, "OUT_FOR_DELIVERY");
            service.updateStatus(1, "DELIVERED");

        } catch (InvalidOrderException e) {
            System.out.println("❌ Invalid Order: " + e.getMessage());
        } catch (PackingNotConfirmedException e) {
            System.out.println("❌ Packing Issue: " + e.getMessage());
        } catch (AgentAssignmentFailedException e) {
            System.out.println("❌ Agent Issue: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected Error: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════
        // DEMO 3: Delivery Flow — Order 999 (NOT PACKED ❌)
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 999 (Not Packed)");
            System.out.println("══════════════════════════════════════════════\n");

            // This should FAIL — order 999 has no packed job in the packing system
            service.createDelivery(2, 999, 5002, "Mumbai");

        } catch (PackingNotConfirmedException e) {
            System.out.println("❌ Correctly rejected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════
        // DEMO 4: Invalid Status Transition
        // ═══════════════════════════════════════════════════════

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  INVALID STATUS TRANSITION TEST");
        System.out.println("══════════════════════════════════════════════\n");

        service.updateStatus(1, "CREATED"); // should fail — already DELIVERED

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  ALL DEMOS COMPLETE");
        System.out.println("══════════════════════════════════════════════\n");
    }
}
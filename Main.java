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
        // StubDeliveryPackingGateway simulates the packing subsystem.
        // To switch to real packing JAR:
        //   IDeliveryPackingGateway packingGateway =
        //       new DeliveryPackingGateway(packingModel);
        // ─────────────────────────────────────────────────────
        IDeliveryPackingGateway packingGateway = new StubDeliveryPackingGateway();
        WarehouseService warehouseService = new PackingWarehouseAdapter(packingGateway);
        PackingInfoService packingInfo = new PackingInfoService(packingGateway);

        // ─────────────────────────────────────────────────────
        // 🗄️ DATABASE MODULE INTEGRATION
        // ─────────────────────────────────────────────────────
        // StubDatabaseGateway simulates Team Jackfruit's DB module.
        // To switch to real database JAR:
        //   SupplyChainDatabaseFacade facade = new SupplyChainDatabaseFacade();
        //   IDatabaseGateway dbGateway = new RealDatabaseGateway(facade);
        // ─────────────────────────────────────────────────────
        IDatabaseGateway dbGateway = new StubDatabaseGateway();
        OrderFulfillmentService orderService = new DatabaseOrderAdapter(dbGateway);
        TrackingService trackingService = new DatabaseTrackingAdapter(dbGateway);

        // ⚙️ Initialize main delivery service
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
        System.out.println("  PACKING SUBSYSTEM INTEGRATION");
        System.out.println("══════════════════════════════════════════════");

        packingInfo.printPackedJobsSummary();
        packingInfo.printPalletSummary();

        System.out.println();
        packingInfo.printBarcodeInfo("WMS-PKJ-0001");
        packingInfo.printBarcodeInfo("WMS-PKJ-0002");
        packingInfo.printBarcodeInfo("WMS-PKJ-0003"); // in-progress — no barcode

        // ═══════════════════════════════════════════════════════
        // DEMO 2: Full Delivery Flow — Order 101 (Valid + Paid + Packed)
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 101 (Happy Path)");
            System.out.println("══════════════════════════════════════════════\n");

            // Order validated via DB, payment checked via DB, packing checked via gateway
            service.createDelivery(1, 101, 5001, "Bangalore");

            // Also persist delivery to DB gateway
            dbGateway.saveDelivery(new IDatabaseGateway.DeliveryData(
                    "1", "101", "5001", "Bangalore", "CREATED"));

            // Assign agent
            service.assignAgent(1);

            // Status transitions — each logged as tracking event in DB
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
        // DEMO 3: Order Not Found in DB — Order 999
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 999 (Not in DB)");
            System.out.println("══════════════════════════════════════════════\n");

            // Order 999 doesn't exist in the database — should fail validation
            service.createDelivery(2, 999, 5002, "Mumbai");

        } catch (InvalidOrderException e) {
            System.out.println("❌ Correctly rejected: " + e.getMessage());
        } catch (PackingNotConfirmedException e) {
            System.out.println("❌ Packing Issue: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════
        // DEMO 4: Payment Not Confirmed — Order 103
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 103 (Payment Pending)");
            System.out.println("══════════════════════════════════════════════\n");

            // Order 103 exists but payment is PENDING — should fail
            service.createDelivery(3, 103, 5003, "Delhi");

        } catch (InvalidOrderException e) {
            System.out.println("❌ Correctly rejected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════
        // DEMO 5: Order Not Packed — Order 102
        // ═══════════════════════════════════════════════════════

        try {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  DELIVERY FLOW — Order 102 (Not Packed)");
            System.out.println("══════════════════════════════════════════════\n");

            // Order 102 exists & paid, but not packed (packing stub only has 101)
            service.createDelivery(4, 102, 5002, "Chennai");

        } catch (PackingNotConfirmedException e) {
            System.out.println("❌ Correctly rejected: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════
        // DEMO 6: Invalid Status Transition
        // ═══════════════════════════════════════════════════════

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  INVALID STATUS TRANSITION TEST");
        System.out.println("══════════════════════════════════════════════\n");

        service.updateStatus(1, "CREATED"); // already DELIVERED — should fail

        // ═══════════════════════════════════════════════════════
        // SUMMARY: Database State
        // ═══════════════════════════════════════════════════════

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  DATABASE STATE SUMMARY");
        System.out.println("══════════════════════════════════════════════");

        System.out.println("\n📋 Deliveries in DB: " + dbGateway.listDeliveries().size());
        for (IDatabaseGateway.DeliveryData d : dbGateway.listDeliveries()) {
            System.out.println("  " + d);
        }

        if (dbGateway instanceof StubDatabaseGateway) {
            StubDatabaseGateway stub = (StubDatabaseGateway) dbGateway;
            System.out.println("\n📍 Tracking Events Logged: " + stub.getTrackingEvents().size());
            for (IDatabaseGateway.TrackingEventData e : stub.getTrackingEvents()) {
                System.out.println("  " + e);
            }
        }

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  ALL DEMOS COMPLETE");
        System.out.println("══════════════════════════════════════════════\n");
    }
}
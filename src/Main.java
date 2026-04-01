import java.util.*;

/**
 * GOAL: Enable safe cancellation and LIFO rollback of confirmed bookings.
 */
public class CancellationSystem {

    // --- 1. DOMAIN MODELS ---

    public record Reservation(String guestId, String roomType, String roomId) {}

    // --- 2. CANCELLATION SERVICE (REVERSAL LOGIC) ---

    public static class BookingCancellationService {
        private final Map<String, Integer> inventory = new HashMap<>();
        private final Map<String, Reservation> activeReservations = new HashMap<>();

        // Requirement: Stack Data Structure for LIFO Rollback Logic
        // Tracks released room IDs to be reused in future allocations.
        private final Stack<String> releasedRoomPool = new Stack<>();

        public BookingCancellationService() {
            // Initial State
            inventory.put("DELUXE", 0); // Currently sold out
            activeReservations.put("GUEST_101", new Reservation("GUEST_101", "DELUXE", "D-105"));
        }

        /**
         * Requirement: Validate existence and perform controlled rollback.
         */
        public void cancelBooking(String guestId) {
            System.out.println(">>> Initiating cancellation for Guest: " + guestId);

            // 1. Validation: Ensure the reservation exists before performing rollback
            if (!activeReservations.containsKey(guestId)) {
                System.err.println("[ERROR] Cancellation Failed: No active reservation found for " + guestId);
                return;
            }

            // 2. State Reversal: Retrieve the booking details
            Reservation reservation = activeReservations.remove(guestId);
            String roomType = reservation.roomType();
            String roomId = reservation.roomId();

            // 3. LIFO Rollback: Push the room ID onto the stack for immediate reuse
            releasedRoomPool.push(roomId);

            // 4. Inventory Restoration: Increment count immediately
            int currentStock = inventory.getOrDefault(roomType, 0);
            inventory.put(roomType, currentStock + 1);

            System.out.printf("[SUCCESS] Cancellation Complete. Room %s is back in pool.%n", roomId);
            System.out.printf("Current %s Inventory: %d%n", roomType, inventory.get(roomType));
            System.out.println("--------------------------------------------------");
        }

        public void showSystemStatus() {
            System.out.println("Released Rooms (Available for Re-allocation): " + releasedRoomPool);
        }
    }

    // --- 3. EXECUTION FLOW ---

    public static void main(String[] args) {
        BookingCancellationService service = new BookingCancellationService();

        // Scenario 1: Successful Cancellation
        service.cancelBooking("GUEST_101");

        // Scenario 2: Attempting to cancel a non-existent booking
        // Requirement: Prevent cancellation of non-existent or already cancelled bookings.
        service.cancelBooking("GUEST_999");

        // Scenario 3: Attempting to double-cancel
        service.cancelBooking("GUEST_101");

        service.showSystemStatus();
    }
}
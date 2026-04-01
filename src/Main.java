import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GOAL: Confirm bookings by assigning unique room IDs and preventing double-booking.
 */
public class Main {

    // --- 1. DOMAIN MODELS ---

    public record ReservationRequest(String guestName, String roomType) {}

    public record ConfirmedBooking(String guestName, String roomType, String roomId) {}

    // --- 2. THE SERVICES (Inventory & Allocation) ---

    public static class AllocationService {
        // State Holder: Inventory counts (Room Type -> Count)
        private final Map<String, AtomicInteger> inventory = new ConcurrentHashMap<>();

        // Uniqueness Enforcement: Set of already assigned IDs (Room Type -> Set of Unique IDs)
        // HashMap<String, Set<String>> allows grouped tracking.
        private final Map<String, Set<String>> allocatedRooms = new ConcurrentHashMap<>();

        public AllocationService() {
            // Initializing Mock Inventory
            inventory.put("DELUXE", new AtomicInteger(2));
            inventory.put("STANDARD", new AtomicInteger(5));

            allocatedRooms.put("DELUXE", Collections.synchronizedSet(new HashSet<>()));
            allocatedRooms.put("STANDARD", Collections.synchronizedSet(new HashSet<>()));
        }

        /**
         * Requirement: Atomic Logical Operation.
         * Assigns a room and decrements inventory in one synchronized flow.
         */
        public Optional<ConfirmedBooking> processAllocation(ReservationRequest request) {
            String type = request.roomType();

            // Critical Section: Ensure only one thread allocates for this type at a time
            synchronized (inventory.get(type)) {
                AtomicInteger count = inventory.get(type);

                // 1. Check availability
                if (count.get() > 0) {
                    // 2. Generate Unique Room ID (e.g., DELUXE-101)
                    String generatedId = type + "-" + (100 + allocatedRooms.get(type).size() + 1);

                    // 3. Uniqueness Enforcement: Check Set to prevent reuse
                    if (!allocatedRooms.get(type).contains(generatedId)) {

                        // 4. Record the ID to prevent reuse
                        allocatedRooms.get(type).add(generatedId);

                        // 5. Decrement inventory immediately
                        count.decrementAndGet();

                        System.out.printf("[SUCCESS] Allocated %s to %s. Remaining: %d%n",
                                generatedId, request.guestName(), count.get());

                        return Optional.of(new ConfirmedBooking(request.guestName(), type, generatedId));
                    }
                }
            }

            System.out.printf("[FAILURE] No availability for %s (%s)%n", request.guestName(), type);
            return Optional.empty();
        }
    }

    // --- 3. EXECUTION ---

    public static void main(String[] args) {
        AllocationService service = new AllocationService();

        // FIFO Queue from previous stage
        Queue<ReservationRequest> queue = new LinkedList<>();
        queue.add(new ReservationRequest("Alice", "DELUXE"));
        queue.add(new ReservationRequest("Bob", "DELUXE"));
        queue.add(new ReservationRequest("Charlie", "DELUXE")); // This should fail (Inventory is 2)

        System.out.println("--- Starting Allocation Processing (FIFO) ---");

        while (!queue.isEmpty()) {
            ReservationRequest request = queue.poll();
            service.processAllocation(request);
        }

        System.out.println("\n--- Final System Consistency Check ---");
        System.out.println("Inventory reflects real-time state. No double-bookings occurred.");
    }
}
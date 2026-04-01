import java.util.*;

/**
 * GOAL: Confirm booking requests safely using Unique Sets and Atomic operations.
 */
public class Main {

    // --- 1. DOMAIN MODELS ---

    public record BookingRequest(String guestName, String roomType) {}

    public record Confirmation(String guestName, String roomType, String roomId) {}

    // --- 2. CORE SERVICES ---

    public static class BookingService {
        // Inventory Service (State Holder)
        private final Map<String, Integer> inventory = new HashMap<>();

        // Uniqueness Enforcement: Mapping Room Types to Sets of Assigned IDs
        private final Map<String, Set<String>> allocatedRoomIds = new HashMap<>();

        public BookingService() {
            // Initializing system state
            inventory.put("DELUXE", 2);
            inventory.put("STANDARD", 5);

            allocatedRoomIds.put("DELUXE", new HashSet<>());
            allocatedRoomIds.put("STANDARD", new HashSet<>());
        }

        /**
         * Requirement: Process allocation and update inventory immediately.
         * This method is synchronized to prevent the "Double Booking" problem.
         */
        public synchronized Optional<Confirmation> processAllocation(BookingRequest request) {
            String type = request.roomType();
            int currentCount = inventory.getOrDefault(type, 0);

            // 1. Check availability
            if (currentCount > 0) {
                // 2. Generate a unique room ID
                // In a real system, this might be a physical room number (e.g., 101, 102)
                String generatedId = type + "-" + (allocatedRoomIds.get(type).size() + 1);

                // 3. Uniqueness Enforcement (Check Set to prevent reuse)
                if (!allocatedRoomIds.get(type).contains(generatedId)) {

                    // 4. Record the ID and decrement inventory immediately (Atomic operation)
                    allocatedRoomIds.get(type).add(generatedId);
                    inventory.put(type, currentCount - 1);

                    System.out.printf("[SUCCESS] Confirmed: %s assigned Room %s. Remaining %s: %d%n",
                            request.guestName(), generatedId, type, inventory.get(type));

                    return Optional.of(new Confirmation(request.guestName(), type, generatedId));
                }
            }

            System.out.printf("[DENIED] Could not book %s for %s. Out of stock.%n", type, request.guestName());
            return Optional.empty();
        }
    }

    // --- 3. EXECUTION FLOW ---

    public static void main(String[] args) {
        BookingService bookingService = new BookingService();

        // FIFO Queue: Stores requests in arrival order
        Queue<BookingRequest> requestQueue = new LinkedList<>();
        requestQueue.add(new BookingRequest("Alice", "DELUXE"));
        requestQueue.add(new BookingRequest("Bob", "DELUXE"));
        requestQueue.add(new BookingRequest("Charlie", "DELUXE")); // Should fail (only 2 rooms)
        requestQueue.add(new BookingRequest("Dan", "STANDARD"));

        System.out.println("--- Processing Dequeued Requests ---");

        // Requirement: Retrieve booking requests from the queue in FIFO order
        while (!requestQueue.isEmpty()) {
            BookingRequest currentRequest = requestQueue.poll();
            bookingService.processAllocation(currentRequest);
        }

        System.out.println("\n--- Final Allocation Report ---");
        System.out.println("No room IDs were reused, and inventory is synchronized.");
    }
}
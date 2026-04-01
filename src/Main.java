import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GOAL: Ensure system correctness under concurrent multi-user booking requests.
 */
public class ConcurrentBookingSystem {

    // --- 1. DOMAIN MODELS ---
    public record BookingRequest(String guestName, String roomType) {}

    // --- 2. THE CONCURRENT PROCESSOR ---
    public static class ConcurrentBookingProcessor {
        // Shared Mutable State: Inventory counts
        private final Map<String, Integer> inventory = new HashMap<>();

        // Shared Mutable State: The Request Queue (Thread-safe implementation)
        private final BlockingQueue<BookingRequest> requestQueue = new LinkedBlockingQueue<>();

        public ConcurrentBookingProcessor() {
            inventory.put("DELUXE", 2); // Only 2 rooms for many guests!
        }

        public void submitRequest(BookingRequest request) {
            requestQueue.add(request);
        }

        /**
         * Requirement: Critical Sections & Synchronized Access.
         * Processes a single request from the queue safely.
         */
        public void processNextRequest() {
            BookingRequest request = requestQueue.poll();
            if (request == null) return;

            // Synchronization ensures only one thread enters this block per room type
            synchronized (inventory) {
                int count = inventory.getOrDefault(request.roomType(), 0);

                System.out.printf("[Thread %s] Checking for %s... (Current Stock: %d)%n",
                        Thread.currentThread().getName(), request.guestName(), count);

                if (count > 0) {
                    // Simulate processing time to increase chance of race conditions if unsynced
                    try { Thread.sleep(50); } catch (InterruptedException e) { }

                    inventory.put(request.roomType(), count - 1);
                    System.out.printf(">> [SUCCESS] %s booked a %s. Remaining: %d%n",
                            request.guestName(), request.roomType(), inventory.get(request.roomType()));
                } else {
                    System.out.printf(">> [FAILED] No rooms left for %s.%n", request.guestName());
                }
            }
        }
    }

    // --- 3. THE SIMULATION (ACTORS: MULTIPLE GUESTS) ---
    public static void main(String[] args) throws InterruptedException {
        ConcurrentBookingProcessor processor = new ConcurrentBookingProcessor();

        // 5 Guests all trying to book the 2 available Deluxe rooms
        String[] guests = {"Alice", "Bob", "Charlie", "Dan", "Eve"};
        for (String name : guests) {
            processor.submitRequest(new BookingRequest(name, "DELUXE"));
        }

        // Creating an ExecutorService to simulate parallel processing
        // Requirement: Simulate multiple requests occurring at the same time.
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("--- CONCURRENCY TEST START ---");

        for (int i = 0; i < guests.length; i++) {
            executor.submit(processor::processNextRequest);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("--- CONCURRENCY TEST END ---");
        System.out.println("Final System State (Inventory): " + processor.inventory);
    }
}
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GOAL: Handle multiple booking requests fairly using a FIFO queue mechanism.
 */
public class Main {

    // --- 1. DOMAIN MODEL: RESERVATION INTENT ---

    // Represents the guest's intent. Note: No allocation data is present yet.
    public record Reservation(String guestName, String roomType, int nights) {
        @Override
        public String toString() {
            return String.format("Request[Guest: %s, Room: %s]", guestName, roomType);
        }
    }

    // --- 2. FAIRNESS MECHANISM: BOOKING QUEUE ---

    public static class BookingRequestQueue {
        // LinkedBlockingQueue is thread-safe and preserves arrival order (FIFO)
        private final Queue<Reservation> queue = new LinkedBlockingQueue<>();

        /**
         * Requirement: Accept booking requests and store in arrival order.
         * Concept: FIFO Principle.
         */
        public void enqueueRequest(Reservation request) {
            queue.add(request);
            System.out.println(">>> System: Received " + request + ". Added to queue.");
        }

        /**
         * Returns the next request to be processed without removing it,
         * or null if the queue is empty.
         */
        public Reservation peekNext() {
            return queue.peek();
        }

        /**
         * Requirement: Prepare requests for subsequent processing.
         * This would be called by the Allocation Service later.
         */
        public Reservation dequeueNext() {
            return queue.poll();
        }

        public int getQueueSize() {
            return queue.size();
        }
    }

    // --- 3. EXECUTION (ACTORS: MULTIPLE GUESTS) ---

    public static void main(String[] args) {
        BookingRequestQueue intakeQueue = new BookingRequestQueue();

        System.out.println("--- PEAK DEMAND SIMULATION ---");
        System.out.println("Decoupling intake from allocation. No inventory mutated yet.\n");

        // Step 1: Multiple guests submit requests nearly simultaneously
        intakeQueue.enqueueRequest(new Reservation("Alice", "DELUXE", 3));
        intakeQueue.enqueueRequest(new Reservation("Bob", "STANDARD", 1));
        intakeQueue.enqueueRequest(new Reservation("Charlie", "DELUXE", 2));

        System.out.println("\n--- QUEUE STATUS ---");
        System.out.println("Total requests waiting: " + intakeQueue.getQueueSize());

        // Step 2: Verify Fairness (First-Come-First-Served)
        System.out.println("First in line: " + intakeQueue.peekNext().guestName());

        // Step 3: Demonstrate processing order (Decoupled Allocation)
        System.out.println("\n--- PROCESSING LOG (FIFO) ---");
        while (intakeQueue.getQueueSize() > 0) {
            Reservation next = intakeQueue.dequeueNext();
            System.out.println("Processing " + next.guestName() + "'s request...");
            // Allocation logic would happen here in the next stage
        }

        System.out.println("\nAll queued requests handled. Fairness preserved.");
    }
}
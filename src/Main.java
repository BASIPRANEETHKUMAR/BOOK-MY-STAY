import java.util.*;

/**
 * GOAL: Strengthen reliability via structured validation and custom error handling.
 */
public class Main {

    // --- 1. CUSTOM EXCEPTIONS (Domain-Specific) ---

    /**
     * Requirement: Throw and handle custom exceptions for invalid scenarios.
     * Custom exceptions make the cause of failure explicit.
     */
    public static class BookingValidationException extends Exception {
        public BookingValidationException(String message) {
            super(message);
        }
    }

    // --- 2. THE VALIDATOR (Guarding System State) ---

    public static class BookingValidator {
        private final Set<String> validRoomTypes = Set.of("DELUXE", "STANDARD", "SUITE");

        /**
         * Requirement: Validate input values and system constraints.
         * Concept: Fail-Fast Design - check everything before touching the data.
         */
        public void validate(String roomType, int currentInventory) throws BookingValidationException {
            // 1. Validate Input: Room Type existence
            if (!validRoomTypes.contains(roomType.toUpperCase())) {
                throw new BookingValidationException("Invalid Room Type: " + roomType);
            }

            // 2. Guarding System State: Prevent negative inventory
            if (currentInventory <= 0) {
                throw new BookingValidationException("Insufficient Inventory: " + roomType + " is sold out.");
            }
        }
    }

    // --- 3. THE BOOKING SERVICE (Correctness over Happy Path) ---

    public static class BookingService {
        private final Map<String, Integer> inventory = new HashMap<>();
        private final BookingValidator validator = new BookingValidator();

        public BookingService() {
            inventory.put("DELUXE", 1); // Only one left!
            inventory.put("STANDARD", 10);
        }

        public void processBooking(String guestName, String roomType) {
            System.out.println(">>> Processing request for " + guestName + " (" + roomType + ")...");

            try {
                int currentStock = inventory.getOrDefault(roomType.toUpperCase(), 0);

                // Step 1: Validate (This will throw an exception if rules are broken)
                validator.validate(roomType, currentStock);

                // Step 2: If we reach here, validation passed. Update State safely.
                inventory.put(roomType.toUpperCase(), currentStock - 1);
                System.out.println("[SUCCESS] Booking confirmed for " + guestName);

            } catch (BookingValidationException e) {
                // Requirement: Graceful Failure Handling.
                // Display clear message without crashing the app.
                System.err.println("[VALIDATION ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.err.println("[SYSTEM ERROR] An unexpected error occurred.");
            } finally {
                // Requirement: Ensure the system remains stable.
                System.out.println("System Status: Operational. Ready for next request.\n");
            }
        }
    }

    // --- 4. EXECUTION (ACTOR: GUEST) ---

    public static void main(String[] args) {
        BookingService service = new BookingService();

        // Scenario A: Valid Booking
        service.processBooking("Alice", "DELUXE");

        // Scenario B: Invalid Input (Non-existent room type)
        service.processBooking("Bob", "PENTHOUSE");

        // Scenario C: State Constraint (Booking a room that just ran out)
        service.processBooking("Charlie", "DELUXE");

        // Scenario D: Valid Booking after failures
        service.processBooking("Dan", "STANDARD");
    }
}
import java.io.*;
import java.util.*;

/**
 * GOAL: Ensure system state survives restarts using File-based Persistence.
 */
public class Main {

    // --- 1. PERSISTENT MODELS (Must implement Serializable) ---

    // Requirement: Serialize complex data structures for storage.
    public record ConfirmedBooking(String guest, String roomType, String roomId) implements Serializable {}

    public static class SystemState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L; // Ensures version compatibility

        public Map<String, Integer> inventory = new HashMap<>();
        public List<ConfirmedBooking> history = new ArrayList<>();

        public void display() {
            System.out.println("Current Inventory: " + inventory);
            System.out.println("Booking History Count: " + history.size());
        }
    }

    // --- 2. PERSISTENCE SERVICE (File I/O) ---

    public static class PersistenceService {
        private static final String FILE_NAME = "system_state.dat";

        /**
         * Requirement: Persist state to a file (Serialization).
         */
        public void saveState(SystemState state) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
                oos.writeObject(state);
                System.out.println("[SAVE] System state successfully persisted to " + FILE_NAME);
            } catch (IOException e) {
                System.err.println("[SAVE ERROR] Could not persist state: " + e.getMessage());
            }
        }

        /**
         * Requirement: Restore data during startup (Deserialization).
         * Concept: Failure Tolerance - Handle missing files gracefully.
         */
        public SystemState loadState() {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                System.out.println("[LOAD] No persistence file found. Starting with fresh state.");
                return createInitialState();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                System.out.println("[LOAD] Restoring system state from " + FILE_NAME + "...");
                return (SystemState) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[LOAD ERROR] Persistence file corrupted. Starting fresh.");
                return createInitialState();
            }
        }

        private SystemState createInitialState() {
            SystemState state = new SystemState();
            state.inventory.put("DELUXE", 5);
            state.inventory.put("STANDARD", 10);
            return state;
        }
    }

    // --- 3. EXECUTION FLOW (RESTART SIMULATION) ---

    public static void main(String[] args) {
        PersistenceService persistence = new PersistenceService();

        // --- SESSION 1: Application Runs and Mutates State ---
        System.out.println("=== SESSION 1: INITIAL RUN ===");
        SystemState session1State = persistence.loadState();

        // Simulate a booking
        session1State.history.add(new ConfirmedBooking("Alice", "DELUXE", "D-101"));
        session1State.inventory.put("DELUXE", session1State.inventory.get("DELUXE") - 1);

        session1State.display();
        persistence.saveState(session1State);
        System.out.println("=== SESSION 1 END (Application Shutdown) ===\n");

        // --- SESSION 2: Application Restarts ---
        System.out.println("=== SESSION 2: RESTART & RECOVERY ===");
        SystemState session2State = persistence.loadState();

        // Requirement: Ensure restored state accurately reflects the last saved state.
        session2State.display();

        if (session2State.history.stream().anyMatch(b -> b.guest().equals("Alice"))) {
            System.out.println("[VERIFIED] Alice's booking survived the restart!");
        }

        System.out.println("=== SESSION 2 END (Resuming Operations) ===");
    }
}
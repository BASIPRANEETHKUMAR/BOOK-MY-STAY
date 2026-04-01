import java.util.*;

/**
 * GOAL: Sort passenger bogies based on seating capacity using a custom Comparator.
 */
public class Main {

    // --- 1. CUSTOM OBJECT: THE BOGIE CLASS ---

    public static class Bogie {
        private final String name;
        private final int capacity;

        public Bogie(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() { return name; }
        public int getCapacity() { return capacity; }

        @Override
        public String toString() {
            return String.format("Bogie: %-12s | Capacity: %d", name, capacity);
        }
    }

    // --- 2. EXECUTION FLOW ---

    public static void main(String[] args) {
        // Step 1: Create a List<Bogie> to store passenger bogies
        List<Bogie> trainBogies = new ArrayList<>();

        // Step 2: Add bogies with varying capacities
        trainBogies.add(new Bogie("Sleeper", 72));
        trainBogies.add(new Bogie("AC Chair", 56));
        trainBogies.add(new Bogie("First Class", 24));
        trainBogies.add(new Bogie("General", 90));

        System.out.println("--- Original Train Order ---");
        trainBogies.forEach(System.out::println);

        // --- 3. KEY CONCEPT: COMPARATOR & SORTING ---

        /**
         * Requirement: Use Comparator.comparingInt() to define sorting.
         * Benefit: Separation of Data and Logic. The Bogie class doesn't need
         * to know how to sort itself; the System defines the rule here.
         */
        trainBogies.sort(Comparator.comparingInt(Bogie::getCapacity));

        System.out.println("\n--- Sorted by Capacity (Ascending) ---");
        // Step 4: Display sorted bogies
        trainBogies.forEach(System.out::println);

        // Optional: Sorting in Descending order (Highest capacity first)
        trainBogies.sort(Comparator.comparingInt(Bogie::getCapacity).reversed());

        System.out.println("\n--- Sorted by Capacity (Descending) ---");
        trainBogies.forEach(System.out::println);

        System.out.println("\nProgram continues... Train planning complete.");
    }
}
import java.util.*;
import java.util.stream.Collectors;

/**
 * GOAL: Enable guests to view available rooms without modifying system state.
 */
public class Main {

    // --- 1. KEY CONCEPTS: IMMUTABLE DOMAIN MODELS ---

    // Room provides descriptive information (Identity/Details)
    public record Room(String type, String description, double price, List<String> amenities) {}

    // Inventory acts as the State Holder (Availability counts)
    public record InventoryItem(String roomType, int availableCount) {}

    // Data Transfer Object (DTO) for the Guest's view
    public record AvailableRoomView(String type, String description, double price, List<String> amenities) {}

    // --- 2. SEPARATION OF CONCERNS: SERVICE LAYER ---

    public static class RoomSearchService {
        private final RoomRepository roomRepo;
        private final InventoryRepository inventoryRepo;

        public RoomSearchService(RoomRepository roomRepo, InventoryRepository inventoryRepo) {
            this.roomRepo = roomRepo;
            this.inventoryRepo = inventoryRepo;
        }

        /**
         * Requirement: Retrieve availability and filter only valid options.
         * Implementation of Read-Only Access and Defensive Programming.
         */
        public List<AvailableRoomView> searchForGuest() {
            return inventoryRepo.getLiveInventory().stream()
                    // Validation Logic: Exclude rooms with zero availability
                    .filter(item -> item.availableCount() > 0)
                    // Domain Model Usage: Map state to descriptive details
                    .map(item -> {
                        Optional<Room> details = roomRepo.findByType(item.roomType());
                        return details.map(d -> new AvailableRoomView(
                                d.type(), d.description(), d.price(), d.amenities()
                        ));
                    })
                    .flatMap(Optional::stream)
                    // Ensure results are immutable (System state remains unchanged)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    // --- 3. REPOSITORIES (MOCK DATA ACCESS) ---

    interface RoomRepository {
        Optional<Room> findByType(String type);
    }

    interface InventoryRepository {
        List<InventoryItem> getLiveInventory();
    }

    // --- 4. EXECUTION (ACTOR: GUEST) ---

    public static void main(String[] args) {
        // Setup mock data for Room Details
        RoomRepository roomRepo = type -> switch (type) {
            case "STANDARD" -> Optional.of(new Room("STANDARD", "Cozy room", 100.0, List.of("WiFi")));
            case "DELUXE"   -> Optional.of(new Room("DELUXE", "Ocean view", 250.0, List.of("WiFi", "Mini-bar")));
            case "SUITE"    -> Optional.of(new Room("SUITE", "Luxury penthouse", 500.0, List.of("Jacuzzi", "Butler")));
            default -> Optional.empty();
        };

        // Setup mock data for Inventory State
        InventoryRepository inventoryRepo = () -> List.of(
                new InventoryItem("STANDARD", 10), // Available
                new InventoryItem("DELUXE", 2),    // Available
                new InventoryItem("SUITE", 0)      // UNAVAILABLE - Should be filtered
        );

        // Initialize Service
        RoomSearchService searchService = new RoomSearchService(roomRepo, inventoryRepo);

        // Guest initiates search
        System.out.println("Guest is searching for available rooms...");
        System.out.println("--------------------------------------------------");

        List<AvailableRoomView> results = searchService.searchForGuest();

        // Display results
        if (results.isEmpty()) {
            System.out.println("No rooms currently available.");
        } else {
            results.forEach(room -> {
                System.out.printf("Room: [%s]%n", room.type());
                System.out.printf("   Description: %s%n", room.description());
                System.out.printf("   Price:       $%.2f%n", room.price());
                System.out.printf("   Amenities:   %s%n%n", String.join(", ", room.amenities()));
            });
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Search Complete. System state remains unchanged.");
    }
}
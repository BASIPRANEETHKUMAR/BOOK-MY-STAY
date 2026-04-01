import java.util.*;
import java.util.stream.Collectors;

/**
 * GOAL: Maintain a chronological audit trail and support administrative reporting.
 */
public class Main
{

    // --- 1. DOMAIN MODEL: THE HISTORICAL RECORD ---

    // Represents a completed transaction stored for historical tracking.
    public record ConfirmedReservation(String guestName, String roomType, String roomId, double price) {
        @Override
        public String toString() {
            return String.format("Audit Entry: [Guest: %-8s | Room: %-10s | Revenue: $%.2f]",
                    guestName, roomId, price);
        }
    }

    // --- 2. BOOKING HISTORY (STORAGE LAYER) ---

    public static class BookingHistory {
        // Requirement: Use a List to maintain records in insertion order (Chronological).
        private final List<ConfirmedReservation> recordStore = new ArrayList<>();

        /**
         * Requirement: Store confirmed reservations.
         * Concept: Historical Tracking / Audit Trail.
         */
        public void archive(ConfirmedReservation reservation) {
            recordStore.add(reservation);
        }

        /**
         * Requirement: Allow retrieval for review.
         * Returns an unmodifiable view to ensure reporting does not modify data.
         */
        public List<ConfirmedReservation> getHistoryView() {
            return Collections.unmodifiableList(recordStore);
        }
    }

    // --- 3. BOOKING REPORT SERVICE (ANALYSIS LAYER) ---

    public static class BookingReportService {
        private final BookingHistory history;

        public BookingReportService(BookingHistory history) {
            this.history = history;
        }

        /**
         * Requirement: Generate summary reports from booking history.
         * Concept: Reporting Readiness.
         */
        public void runManagerialReport() {
            List<ConfirmedReservation> data = history.getHistoryView();

            double totalRevenue = data.stream().mapToDouble(ConfirmedReservation::price).sum();
            long totalCount = data.size();

            System.out.println("\n--- ADMINISTRATIVE OPERATIONAL REPORT ---");
            System.out.println("Total Bookings Processed: " + totalCount);
            System.out.printf("Total Gross Revenue:      $%.2f%n", totalRevenue);
            System.out.println("-----------------------------------------");

            // Categorical Breakdown
            Map<String, Long> summary = data.stream()
                    .collect(Collectors.groupingBy(ConfirmedReservation::roomType, Collectors.counting()));

            summary.forEach((type, count) ->
                    System.out.printf("Category: %-10s | Volume: %d%n", type, count));
            System.out.println("-----------------------------------------\n");
        }
    }

    // --- 4. EXECUTION FLOW (ACTOR: ADMIN) ---

    public static void main(String[] args) {
        // Initialize the Persistence-oriented infrastructure
        BookingHistory history = new BookingHistory();
        BookingReportService reportService = new BookingReportService(history);

        // Simulation: Bookings are confirmed and archived
        System.out.println("System: Archiving confirmed reservations...");
        history.archive(new ConfirmedReservation("Alice", "DELUXE", "D-101", 300.0));
        history.archive(new ConfirmedReservation("Bob", "STANDARD", "S-205", 150.0));
        history.archive(new ConfirmedReservation("Charlie", "DELUXE", "D-102", 300.0));

        // Requirement: Admin reviews booking history
        System.out.println("\n--- Admin: Accessing Historical Audit Trail ---");
        history.getHistoryView().forEach(System.out::println);

        // Requirement: Generate summaries
        reportService.runManagerialReport();

        System.out.println("Persistence Mindset Check: Audit trail is secured and ordered.");
    }
}
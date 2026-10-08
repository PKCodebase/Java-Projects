package com.mcd.plantation.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Comprehensive real-time snapshot of the MCD plantation drive.
 * Returned by GET /mcd/dashboard — admin-only.
 */
public record DashboardOverview(

    // ── All-time metrics ────────────────────────────────────────
    long totalTreesPlanted,
    long totalBookings,
    long totalCertificatesIssued,

    // ── Booking status breakdown ────────────────────────────────
    long bookingsPendingPayment,
    long bookingsPaid,
    long bookingsScheduled,
    long bookingsWaiting,
    long bookingsPlanted,
    long bookingsCompleted,
    long bookingsCancelled,

    // ── Today ───────────────────────────────────────────────────
    long treesPlantedToday,
    long bookingsMadeToday,

    // ── This week (Mon–today) ────────────────────────────────────
    long treesPlantedThisWeek,
    long bookingsMadeThisWeek,

    // ── This month (1st–today) ───────────────────────────────────
    long treesPlantedThisMonth,
    long bookingsMadeThisMonth,

    // ── Infrastructure ──────────────────────────────────────────
    int activeParkCount,
    int upcomingSlotCount,      // slots in the next 7 days that are not CLOSED
    int lowStockAlertCount,     // slot-inventory rows with ≤5 trees available

    // ── Lists ───────────────────────────────────────────────────
    List<RecentActivity>    recentActivity,     // last 10 plantation events
    List<ZoneStats>         zoneBreakdown,      // one row per zone
    List<SpeciesStats>      speciesBreakdown,   // top 8 species by planted count
    List<DailyTrend>        dailyTrend,         // last 7 calendar days
    List<ParkPendingStats>  topPendingParks,    // top 5 parks by pending count

    // ── Snapshot timestamp ──────────────────────────────────────
    OffsetDateTime generatedAt

) {

	/*
	 * public record RecentActivity( String bookingRef, String citizenName, String
	 * parkName, String treeName, String treeEmoji, String officialName, // may be
	 * null if not yet assigned LocalDate plantedDate, OffsetDateTime recordedAt )
	 * {}
	 */
    
    public record RecentActivity(

    	    String bookingRef,
    	    String citizenName,
    	    String parkName,

    	    List<TreeSummary> trees,

    	    String officialName,
    	    LocalDate plantedDate,
    	    OffsetDateTime recordedAt

    	) {}
    
    public record TreeSummary(

    	    String treeName,
    	    String treeEmoji,
    	    Integer quantity

    	) {}

    public record ZoneStats(
        String zoneName, // ????
        long   totalPlanted,
        long   scheduledBookings,
        long   totalBookings
    ) {}

    public record SpeciesStats(
        String commonName,
        String scientificName,
        String emojiCode,
        long   totalPlanted,
        long   scheduled          // SCHEDULED + WAITING bookings for this species
    ) {}

    public record DailyTrend(
        LocalDate date,
        long      planted,        // PlantationRecord.plantedDate == date
        long      booked          // new bookings whose slot is on this date
    ) {}

    public record ParkPendingStats(
        String parkName,
        String zoneName, //????
        long   pendingCount       // SCHEDULED + WAITING
    ) {}
}

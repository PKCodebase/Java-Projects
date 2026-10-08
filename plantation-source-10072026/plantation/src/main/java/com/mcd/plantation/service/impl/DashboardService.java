package com.mcd.plantation.service.impl;

import com.mcd.plantation.config.MasterDataCache;
import com.mcd.plantation.dto.response.DashboardOverview;
import com.mcd.plantation.dto.response.DashboardOverview.TreeSummary;
import com.mcd.plantation.entity.Booking;
import com.mcd.plantation.entity.ParkSlot;
import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.SlotStatus;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BookingRepository           bookingRepo;
    private final PlantationRecordRepository  recordRepo;
    private final CertificateRepository       certRepo;
    private final ParkRepository              parkRepo;
    private final ParkSlotRepository          slotRepo;
    private final SlotTreeInventoryRepository slotInvRepo;
    private final SwagamService               swagamService;
    private final MasterDataCache             masterDataCache;  

    private static final List<BookingStatus> PENDING_STATUSES =
        List.of(BookingStatus.SCHEDULED, BookingStatus.WAITING);

    public DashboardOverview getOverview() {

        // ── Time anchors ─────────────────────────────────────────
        LocalDate today      = LocalDate.now();
        LocalDate weekStart  = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        ZoneOffset utc           = ZoneOffset.UTC;
        OffsetDateTime todayDT   = today.atStartOfDay().atOffset(utc);
        OffsetDateTime tomorrowDT= today.plusDays(1).atStartOfDay().atOffset(utc);
        OffsetDateTime weekDT    = weekStart.atStartOfDay().atOffset(utc);
        OffsetDateTime monthDT   = monthStart.atStartOfDay().atOffset(utc);

        // ── All-time counts ──────────────────────────────────────
        long totalPlanted  = recordRepo.count();
        long totalBookings = bookingRepo.count();
        long totalCerts    = certRepo.count();

        // ── Status breakdown ─────────────────────────────────────
        long pendingPay = bookingRepo.countByBookingStatus(BookingStatus.PENDING_PAYMENT);
        long paid       = bookingRepo.countByBookingStatus(BookingStatus.PAID);
        long scheduled  = bookingRepo.countByBookingStatus(BookingStatus.SCHEDULED);
        long waiting    = bookingRepo.countByBookingStatus(BookingStatus.WAITING);
        long statPlanted= bookingRepo.countByBookingStatus(BookingStatus.PLANTED);
        long completed  = bookingRepo.countByBookingStatus(BookingStatus.COMPLETED);
        long cancelled  = bookingRepo.countByBookingStatus(BookingStatus.CANCELLED);

        // ── Time-scoped plantation counts ────────────────────────
        long plantedToday    = recordRepo.countByPlantedDate(today);
        long bookedToday     = bookingRepo.countByBookedAtRange(todayDT, tomorrowDT);
        long plantedWeek     = recordRepo.countByPlantedDateRange(weekStart, today);
        long bookedWeek      = bookingRepo.countByBookedAtRange(weekDT, tomorrowDT);
        long plantedMonth    = recordRepo.countByPlantedDateRange(monthStart, today);
        long bookedMonth     = bookingRepo.countByBookedAtRange(monthDT, tomorrowDT);

        // ── Infrastructure ───────────────────────────────────────
        int activeParkCount  = (int) parkRepo.countByIsActiveTrue();
        int upcomingSlotCount= (int) slotRepo.countUpcomingActiveSlots(today, today.plusDays(7), SlotStatus.CLOSED);
        int lowStockCount    = slotInvRepo.findLowStock(5, today).size();

        // ── Recent activity (last 10) ────────────────────────────
		/*
		 * List<DashboardOverview.RecentActivity> recentActivity = recordRepo
		 * .findRecentWithDetails(PageRequest.of(0, 10)) .stream() .map(r -> { var b =
		 * r.getBooking(); var inv = b.getInventory(); return new
		 * DashboardOverview.RecentActivity( b.getBookingRef(),
		 * //??b.getCitizen().getFullName(), "Pushpendra Singh",
		 * inv.getSlot().getPark().getName(), inv.getSpecies().getCommonName(),
		 * inv.getSpecies().getEmojiCode(), r.getOfficial() != null ? "Officer" : null,
		 * r.getPlantedDate(), r.getRecordedAt() ); }) .toList();
		 */
        
        
        List<DashboardOverview.RecentActivity> recentActivity =
                recordRepo.findRecentWithDetails(PageRequest.of(0, 10))
                        .stream()
                        .map(r -> {

                            Booking booking = r.getBooking();

                            ParkSlot slot = booking.getSlot();

                            List<TreeSummary> trees =
                                    booking.getItems()
                                            .stream()
                                            .map(item -> new TreeSummary(
                                                    item.getInventory().getSpecies().getCommonName(),
                                                    item.getInventory().getSpecies().getEmojiCode(),
                                                    item.getQuantity()))
                                            .toList();

                            return new DashboardOverview.RecentActivity(

                                    booking.getBookingRef(),

                                    swagamService.
                    				fetchUserSummary(booking.getCitizen(), UserTypeCode.UT_CTZ.name()).fullName(),

                                    slot.getPark().getName(),

                                    trees,

                                    r.getOfficial() != null
                                            ? swagamService.fetchUserSummary(r.getOfficial(), UserTypeCode.UT_EMP.name()).fullName()
                                            : null,

                                    r.getPlantedDate(),

                                    r.getRecordedAt());

                        })
                        .toList();

        // ── Zone breakdown ───────────────────────────────────────
        Map<String, Long> plantedByZone    = toStringLongMap(recordRepo.countPlantedByZone());
        Map<String, Long> scheduledByZone  = toStringLongMap(bookingRepo.countByZoneAndStatus(PENDING_STATUSES));
        Map<String, Long> totalByZone      = toStringLongMap(bookingRepo.countByZone());

        Set<String> allZones = new LinkedHashSet<>();
        allZones.addAll(plantedByZone.keySet());
        allZones.addAll(scheduledByZone.keySet());
        allZones.addAll(totalByZone.keySet());

        List<DashboardOverview.ZoneStats> zoneBreakdown = allZones.stream()
            .sorted()
            .map(z -> new DashboardOverview.ZoneStats(
            	masterDataCache.getZoneName(z),
                plantedByZone.getOrDefault(z, 0L),
                scheduledByZone.getOrDefault(z, 0L),
                totalByZone.getOrDefault(z, 0L)
            ))
            .toList();

        // ── Species breakdown (top 8) ─────────────────────────────
        Map<String, Long> speciesScheduled = new HashMap<>();
        for (Object[] row : bookingRepo.countBySpeciesAndStatus(PENDING_STATUSES, PageRequest.of(0, 8))) {
            speciesScheduled.put((String) row[0], (Long) row[3]);
        }

        List<DashboardOverview.SpeciesStats> speciesBreakdown = new ArrayList<>();
        for (Object[] row : recordRepo.countPlantedBySpecies(PageRequest.of(0, 8))) {
            String name = (String) row[0];
            speciesBreakdown.add(new DashboardOverview.SpeciesStats(
                name,
                (String) row[1],
                (String) row[2],
                (Long)   row[3],
                speciesScheduled.getOrDefault(name, 0L)
            ));
        }

        // ── Daily trend (last 7 days) ─────────────────────────────
        LocalDate trendStart = today.minusDays(6);
        Map<LocalDate, Long> plantedByDate = toDateLongMap(recordRepo.countPlantedByDate(trendStart));
        Map<LocalDate, Long> bookedByDate  = toDateLongMap(bookingRepo.countBySlotDate(trendStart));

        List<DashboardOverview.DailyTrend> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dailyTrend.add(new DashboardOverview.DailyTrend(
                d,
                plantedByDate.getOrDefault(d, 0L),
                bookedByDate.getOrDefault(d, 0L)
            ));
        }

        // ── Top 5 parks with most pending bookings ────────────────
        
		/*
		 * List<DashboardOverview.ParkPendingStats> topPending = new ArrayList<>(); for
		 * (Object[] row : bookingRepo.countPendingByPark(PENDING_STATUSES,
		 * PageRequest.of(0, 5))) { topPending.add(new
		 * DashboardOverview.ParkPendingStats( (String) row[0], (String) row[1], (Long)
		 * row[2] )); }
		 */
        
        List<DashboardOverview.ParkPendingStats> topPending = new ArrayList<>();

        for (Object[] row : bookingRepo.countPendingByPark(PENDING_STATUSES, PageRequest.of(0, 5))) {
            String parkName = (String) row[0];
            String zoneCode = (String) row[1];
            Long pendingCount = (Long) row[2];

            String zoneName = "Unzoned"; 
            if (zoneCode != null) {
                String cachedName = masterDataCache.getZoneName(zoneCode); 
                if (cachedName != null) {
                    zoneName = cachedName;
                }
            }
            topPending.add(new DashboardOverview.ParkPendingStats(
                parkName,
                zoneName,
                pendingCount != null ? pendingCount : 0L
            ));
        }

        return new DashboardOverview(
            totalPlanted, totalBookings, totalCerts,
            pendingPay, paid, scheduled, waiting, statPlanted, completed, cancelled,
            plantedToday, bookedToday,
            plantedWeek,  bookedWeek,
            plantedMonth, bookedMonth,
            activeParkCount, upcomingSlotCount, lowStockCount,
            recentActivity, zoneBreakdown, speciesBreakdown, dailyTrend, topPending,
            OffsetDateTime.now()
        );
    }

    // ── Helpers ─────────────────────────────────────────────────

    private static Map<String, Long> toStringLongMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    private static Map<LocalDate, Long> toDateLongMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((LocalDate) row[0], (Long) row[1]);
        }
        return map;
    }
}

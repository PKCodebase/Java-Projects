package com.mcd.plantation.util;

import com.mcd.plantation.repository.BookingRepository;
import java.time.LocalDate;

public final class BookingRefGenerator {
    private BookingRefGenerator() {}

    private static final Object LOCK = new Object();

    public static String next(BookingRepository repo) {
        synchronized (LOCK) {
            int year = LocalDate.now().getYear();
            long count = repo.count() + 1;
            return "MCD-" + year + "-" + String.format("%06d", count);
        }
    }
}

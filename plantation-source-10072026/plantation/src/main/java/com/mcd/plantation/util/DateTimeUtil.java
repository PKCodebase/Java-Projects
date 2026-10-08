package com.mcd.plantation.util;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public final class DateTimeUtil {

	public static final DateTimeFormatter dF_yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
	public static final DateTimeFormatter dF_db_yyyyMMdd = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	public static final DateTimeFormatter dF_dh_yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter dF_yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	public static final DateTimeFormatter dF_db_yyyyMMdd_dc_HHmmss = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	public static final DateTimeFormatter dF_dh_yyyyMMdd_dc_HHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter dF_ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy");
	public static final DateTimeFormatter dF_db_ddMMyyyy = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public static final DateTimeFormatter dF_dh_ddMMyyyy = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	public static final DateTimeFormatter dF_ddMMyyyyHHmmss = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
	public static final DateTimeFormatter dF_db_ddMMyyyy_dc_HHmmss = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	public static final DateTimeFormatter dF_dh_ddMMyyyy_dc_HHmmss = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	public static final DateTimeFormatter dFyyyy = DateTimeFormatter.ofPattern("yyyy");
	public static final DateTimeFormatter tFhhmmss = DateTimeFormatter.ofPattern("HHmmss");
	
	public static String formatDate(Date date, DateTimeFormatter dateTimeFormatter) {
		if(date != null && dateTimeFormatter != null) {
			Instant instant = date.toInstant();
			LocalDateTime localDateTime = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();
			String formatted = localDateTime.format(dateTimeFormatter);
			return formatted;
		}
		return null;
	}
	
	public static Date parseDate(String dateStr, DateTimeFormatter dateTimeFormatter) {
		if(dateStr != null && dateTimeFormatter != null) {
			LocalDate dateTime = LocalDate.parse(dateStr, dateTimeFormatter);
			return java.util.Date.from(dateTime.atStartOfDay()
				      .atZone(ZoneId.systemDefault())
				      .toInstant());
		}
		return null;
	}
	
	public static Long getCurrentTimeStamp() {	
		return System.currentTimeMillis();
	}
	
	public static Date addDays(Date date, int noOfDays) {
		Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, noOfDays);
        return cal.getTime();
	}
	
	public static Date getZeroTimeDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		date = calendar.getTime();
		return date;
	}
	
	public static boolean checkDateTimeExpire(Date createdDateTime, Date currentDateTime, 
			int daysExpireIn, int hourExpireIn, int minuteExpireIn, int secondExpireIn) {
		if(createdDateTime != null && currentDateTime != null) {
			LocalDateTime createdLocalDateTime = createdDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			if(daysExpireIn != 0) {
				createdLocalDateTime.plusDays(daysExpireIn);
			}
			if(hourExpireIn != 0) {
				createdLocalDateTime.plusHours(hourExpireIn);
			}
			if(minuteExpireIn != 0) {
				createdLocalDateTime.plusMinutes(minuteExpireIn);
			}
			if(secondExpireIn != 0) {
				createdLocalDateTime.plusSeconds(secondExpireIn);
			}
			Date expireDateTime = Date.from(createdLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
			if(expireDateTime.before(currentDateTime)) {
				return false;
			}
		}
		return true;
	}
	
}


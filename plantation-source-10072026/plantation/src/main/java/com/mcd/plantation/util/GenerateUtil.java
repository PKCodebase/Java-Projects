package com.mcd.plantation.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public final class GenerateUtil {

	private GenerateUtil() {}
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO RANDOM NUMBER ##### */
	public static final long getRandomNum(long rangeStart, long rangeEnd){
	    if (rangeStart > rangeEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = rangeEnd - rangeStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    Random rnd = new Random(System.currentTimeMillis());
	    long fraction = (long)(range * rnd.nextDouble());
	    int randomNumber =  (int)(fraction + rangeStart);    
	    return randomNumber;
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE UNIQUE IDENTIFIER ##### */
	public static final String getAlpaNumId(){
		String id = UUID.randomUUID().toString();
	    id = id.replaceAll("-", "").toUpperCase();
	    return id;
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE UUID ##### */
	public static final String getUUID() {
		return UUID.randomUUID().toString();
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE DATE RANDOM ID  ##### */
	public static final String getDateTimeRandomNum(int randomNumDigit, String delimiter) {
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHMMSS");
		StringBuilder stringBuilder = new StringBuilder(dateFormat.format(currentDate));
		if(randomNumDigit > 0) {
			long rangeStart = 1;
			for(int i = 1; i < randomNumDigit; i++) {
				rangeStart = rangeStart * 10;
			}
			long rangeEnd = (rangeStart * 10) - 1;
			long randomNum = getRandomNum(rangeStart, rangeEnd);
			if(delimiter != null && !delimiter.isBlank()) {
				stringBuilder.append(delimiter);
			}
			stringBuilder.append(String.valueOf(randomNum));
		}
		return stringBuilder.toString();
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE EXCEPTION ID  ##### */
	public static String getExceptionID() {
		StringBuilder builder = new StringBuilder("EID");
		builder.append("-");
		builder.append(DateTimeUtil.formatDate(new Date(), DateTimeUtil.dF_yyyyMMdd));
		builder.append("-");
		builder.append(GenerateUtil.getRandomNum(100000, 999999));
		return builder.toString();
	}
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE UNIQUE IDENTIFIER ##### */
	public static final String getIdentifier(){
		String id = UUID.randomUUID().toString();
	    id = id.replaceAll("-", "").toUpperCase();
	    return id;
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE RANDOM NUMBER ##### */
	public static final int getRandomInteger(int rangeStart, int rangeEnd){
	    if (rangeStart > rangeEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = (long)rangeEnd - (long)rangeStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    Random rnd = new Random(System.currentTimeMillis());
	    long fraction = (long)(range * rnd.nextDouble());
	    int randomNumber =  (int)(fraction + rangeStart);    
	    return randomNumber;
	}
	/* ################################################################################ */
	
	/* ################################################################################ */
	/* ##### FUNCTIONS TO GENERATE RANDOM NUMBER ##### */
	public static final int getRandomFloat(long rangeStart, long rangeEnd){
	    if (rangeStart > rangeEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = rangeEnd - rangeStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    Random rnd = new Random(System.currentTimeMillis());
	    long fraction = (long)(range * rnd.nextDouble());
	    int randomNumber =  (int)(fraction + rangeStart);    
	    return randomNumber;
	}
	/* ################################################################################ */
}

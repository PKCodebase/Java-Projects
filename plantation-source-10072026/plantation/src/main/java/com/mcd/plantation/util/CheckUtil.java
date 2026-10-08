package com.mcd.plantation.util;


import java.math.BigDecimal;

public final class CheckUtil {

    private CheckUtil() {}

    /* ################################################################################ */
    /* ##### FUNCTIONS TO CHECK NULL OR ZERO ##### */
    public static boolean isNullOrZero(BigDecimal id) {
        BigDecimal zero = new BigDecimal(0);
        if(id==null || id.compareTo(zero)==0)
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrZero(Long id) {
        Long zero = 0L;
        if(id==null || zero.equals(id))
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrZero(Double id) {
        Double zero = 0D;
        if(id==null || zero.equals(id))
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrZero(Float id) {
        Float zero = 0F;
        if(id==null || zero.equals(id))
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrZeroOrNegative(Long id) {
        return isNullOrZero(id) || id.intValue() < 0;
    }

    public static boolean isNullOrZero(Integer id) {
        Integer zero = 0;
        if(id == null || zero.equals(id))
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(String string) {
        if(string == null || string.isBlank())
        {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Character character) {
        if(character == null || character.equals(' '))
        {
            return true;
        }
        return false;
    }

    public static boolean isNotNull(Object obj)    {
        return (obj != null);
    }
    /* ################################################################################ */

    /* ################################################################################ */
    /* ##### FUNCTIONS TO CHECK STRING IS NUMERIC ##### */

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /* ################################################################################ */
}
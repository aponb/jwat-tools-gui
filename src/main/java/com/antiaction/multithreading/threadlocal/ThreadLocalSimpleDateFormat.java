package com.antiaction.multithreading.threadlocal;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ThreadLocalSimpleDateFormat {

	private final String pattern;

	private final boolean lenient;

	private final TimeZone zone;

	private final ThreadLocal<DateFormat> DATEFORMAT_TL = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat initialValue() {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setLenient(lenient);
            dateFormat.setTimeZone(zone);
            return dateFormat;
        }
    };

    private ThreadLocalSimpleDateFormat(String pattern, boolean lenient, TimeZone zone) {
    	this.pattern = pattern;
    	this.lenient = lenient;
    	this.zone = zone;
    }

    public static ThreadLocalSimpleDateFormat getInstanceDefaultTZ(String pattern) {
		return new ThreadLocalSimpleDateFormat(pattern, false, TimeZone.getDefault());
	}

    public static ThreadLocalSimpleDateFormat getInstanceUTC(String pattern) {
		return new ThreadLocalSimpleDateFormat(pattern, false, TimeZone.getTimeZone("UTC"));
	}

    public static ThreadLocalSimpleDateFormat getInstance(String pattern, boolean lenient, TimeZone zone) {
		return new ThreadLocalSimpleDateFormat(pattern, lenient, zone);
	}

    public DateFormat getDateFormat() {
        return DATEFORMAT_TL.get();
    }

    public String format(Date date) {
    	return DATEFORMAT_TL.get().format(date);
    }

	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		return DATEFORMAT_TL.get().format(date, toAppendTo, pos);
	}

	public Date parse(String source) throws ParseException {
        return DATEFORMAT_TL.get().parse(source);
	}

	public Date parse(String text, ParsePosition pos) {
		return DATEFORMAT_TL.get().parse(text, pos);
	}

}

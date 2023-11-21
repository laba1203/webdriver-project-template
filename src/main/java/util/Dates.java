package util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.JavascriptExecutor;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static java.time.temporal.TemporalAdjusters.*;

public class Dates {
    
    private static final String DEFAULT_PATTERN = "MM/dd/yyyy";
    private static final String PATTERN_IN_DATE_RANGE_PICKER = "M/d/yyyy";

    private static final DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final DateTimeFormatter rangePickerFormatter = DateTimeFormatter.ofPattern(PATTERN_IN_DATE_RANGE_PICKER);

    private static final String SHORT_FORMAT_PATTERN = "M/d/yy";
    public static final String DEFAULT_TIMEZONE = "UTC"; // API value in InternalAPI


    @RequiredArgsConstructor
    @Getter
    public enum TimeZones {
        SF("America/Los_Angeles"),
        UTC("Greenwich");

        private final String value;
    }

    /**
     * LocalDateTime methods
     */
    public static String getCurrentDateTimeInFormat(String pattern) {
        return getLocalDateTime(LocalDateTime.now(), pattern, null);
    }

    public static String getCurrentDate() {
        return getLocalDateTime(LocalDateTime.now(), DEFAULT_PATTERN, null);
    }

    public static String getCurrentDate(String pattern, TimeZones timeZone) {
        return getLocalDateTime(LocalDateTime.now(), pattern, timeZone);
    }

    public static String getCurrentDateInShortFormat() {
        return getLocalDateTime(LocalDateTime.now(), SHORT_FORMAT_PATTERN, TimeZones.UTC);
    }

    public static String getCurrentDateUsStandardFormat(LocalDateTime localDateTime) {
        return getLocalDateTime(localDateTime, PATTERN_IN_DATE_RANGE_PICKER, TimeZones.UTC);
    }

    public static String getCurrentDateUsStandardFormat() {
        return getLocalDateTime(LocalDateTime.now(), PATTERN_IN_DATE_RANGE_PICKER, TimeZones.UTC);
    }

    public static String getCurrentDateInFormatWithTimezone(String pattern, TimeZones timeZone) {
        return getLocalDateTime(LocalDateTime.now(), pattern, timeZone);
    }

    private static String getLocalDateTime(LocalDateTime localDateTime, String pattern, TimeZones timeZone) {
        if (timeZone != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(timeZone.getValue()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return zonedDateTime.format(formatter);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return localDateTime.format(formatter);
        }
    }

    /**
     * @return date from browser in format "MM/dd/yyyy"
     */
    public static String getCurrentDateFromBrowser() {
        String script =
                "function getDate(){\n" +
                        "var today = new Date();\n" +
                        "var dd = today.getDate();\n" +
                        "var mm = today.getMonth()+1; //January is 0!\n" +
                        "var yyyy = today.getFullYear();\n" +
                        "\n" +
                        "if(dd<10) {\n" +
                        "    dd = '0'+dd\n" +
                        "} \n" +
                        "\n" +
                        "if(mm<10) {\n" +
                        "    mm = '0'+mm\n" +
                        "} \n" +
                        "\n" +
                        "return mm + '/' + dd + '/' + yyyy;" +
                        "};\n" +
                        "return getDate();";
        JavascriptExecutor js = (JavascriptExecutor) DriverConfig.getDriver();
        return js.executeScript(script).toString();
    }

    /**
     * The following methods get today date form Browser session
     */
    private static LocalDate getLocalDateFromBrowser() {
        return LocalDate.parse(getCurrentDateFromBrowser(), defaultFormatter);
    }

    public static String getCurrentDateFromBrowser(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return getLocalDateFromBrowser().format(formatter);
    }

    public static String getDateDifferentFromTodayFromBrowser(int fromToday) {
        LocalDate localDate = getLocalDateFromBrowser();
        return getDateDiffFromDate(fromToday, rangePickerFormatter, localDate);
    }

    public static String getDateDifferentFromTodayFromBrowser(int fromToday, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        LocalDate localDate = getLocalDateFromBrowser();
        return getDateDiffFromDate(fromToday, formatter, localDate);
    }

    /**
     * LocalDate different date from today methods
     */
    public static String getDateDiffFromToday(int fromToday, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate localDate = LocalDate.now();
        return getDateDiffFromDate(fromToday, formatter, localDate);
    }

    public static String getDateDiffFromToday(int fromToday, DateTimeFormatter formatter) {
        LocalDate localDate = LocalDate.now();
        return getDateDiffFromDate(fromToday, formatter, localDate);
    }

    public static String getDateDiffFromToday(int fromToday) {
        LocalDate localDate = LocalDate.now();
        return getDateDiffFromDate(fromToday, rangePickerFormatter, localDate);
    }

    private static String getDateDiffFromDate(int fromToday, DateTimeFormatter formatter, LocalDate localDate) {
        LocalDate differentDate = localDate.plusDays(fromToday);
        return differentDate.format(formatter);
    }

    /**
     * Current month dates
     */
    public static String getFirstDateOfCurrentMonth() {
        return LocalDate.now().with(firstDayOfMonth()).format(rangePickerFormatter);
    }

    public static String getSecondDateOfCurrentMonth() {
        return LocalDate.now().with(firstDayOfMonth()).plusDays(1).format(rangePickerFormatter);
    }

    /**
     * Previous month dates
     **/
    public static String getFirstDateOfPreviousMonth() {
        return LocalDate.now().minusMonths(1).with(firstDayOfMonth()).format(rangePickerFormatter);
    }

    public static String getLastDateOfPreviousMonth() {
        return LocalDate.now().minusMonths(1).with(lastDayOfMonth()).format(rangePickerFormatter);
    }

    /**
     * Current year dates
     **/
    public static String getFirstDateOfCurrentYear() {
        return LocalDate.now().with(firstDayOfYear()).format(rangePickerFormatter);
    }

    /**
     * Previous year dates
     **/
    public static String getFirstDateOfPreviousYear() {
        return LocalDate.now().minusYears(1).with(firstDayOfYear()).format(rangePickerFormatter);
    }

    public static String getLastDateOfPreviousYear() {
        return LocalDate.now().minusYears(1).with(lastDayOfYear()).format(rangePickerFormatter);
    }


    public static String getFirstAndLastDateOfTheYear(int minusYears) {
        return LocalDate.now().minusYears(minusYears).with(firstDayOfYear()).format(DateTimeFormatter.ofPattern("MM/dd/yy")) + " - " +
                LocalDate.now().minusYears(minusYears).with(lastDayOfYear()).format(DateTimeFormatter.ofPattern("MM/dd/yy"));
    }

    public static String getFirstDateOfTheYearAndTodaysDate() {
        return LocalDate.now().with(firstDayOfYear()).format(DateTimeFormatter.ofPattern("MM/dd/yy")) + " - " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yy"));
    }

    /**
     * Random dates
     **/
    public static LocalDate getRandomDate(boolean inThePast) {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.of(2050, Month.JANUARY, 1);
        if(inThePast) {
            start = LocalDate.of(1921, Month.JANUARY, 1);
            end = LocalDate.now();
        }
        return getRandomDateBetween(start, end);
    }

    public static LocalDate getRandomDateInThePast() {
        return getRandomDate(true);
    }

    private static LocalDate getRandomDateBetween(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = ThreadLocalRandom
                .current()
                .nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay);
    }

}
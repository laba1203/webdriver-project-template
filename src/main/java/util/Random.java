package util;

import lombok.SneakyThrows;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Random {

    private static final java.util.Random random = new java.util.Random();

    public static String id() {
        return String.valueOf(System.currentTimeMillis()).substring(4);
    }

    public static int number() {
        return number(100000);
    }

    @Deprecated
    public static long randLong() {
        return Long.parseLong(id().substring(6) + number());
    }

    public static int number(int range) {
        return (int) (Math.random() * range) + 1;
    }

    public static String doubleNumber(double range) {
        double number = (Math.random() * range) + 1;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(number);
    }

    public static String couponListName(String prefix) {
        return prefix + "-coupons-" + Random.number();
    }

    public static String tierName() {
        return tierName("random");
    }

    public static String tierName(String prefix) {
        return prefix + "-tier" + Random.number();
    }

    public static String couponListName() {
        return couponListName("auto");
    }


    public static String email() {
        return email("test.user", "gmail.com");
    }

    public static String email(String prefix) {
        return email(prefix, "gmail.com");
    }

    public static String adEmail(){
        return email("ad");
    }

    public static String frEmail(){
        return email("fr");
    }

    public static String email(String prefix, String domain) {
        return prefix + "." + number() + id() + "@" + domain;
    }

    public static String orderNumber() {
        return orderNumber("order-number");
    }

    public static String orderNumber(String prefix) {
        return prefix + "-" + number();
    }

    //Phone example: +11972325739
    public static String phoneNumberUs() {
        return phoneNumber("+" + 1202, 514, null);
    }

    //Phone example: +447911123456
    public static String phoneNumberUk() {
        return phoneNumber("+" + 447911);
    }

    //Phone example: +380684446666
    public static String phoneNumberUa() {
        return phoneNumber("+" + 38068);
    }

    /**
     * @param codeAndDestination: country code (+38) and National destination code(e.g .068).
     *                            Examples:
     *                            `+38068` - Ukraine
     *                            `+447911` - UK
     *                            `+1202` - US
     */
    private static String phoneNumber(String codeAndDestination) {
        return phoneNumber(codeAndDestination, null, null);
    }

    private static String phoneNumber(String codeAndDestination, Integer localeCode, Integer code) {
        if (localeCode == null) {
            localeCode = random.nextInt(643) + 100;
        }
        if (code == null) {
            code = random.nextInt(8999) + 1000;
        }
        return codeAndDestination + localeCode + code;
    }

    public static String ip() {
        StringBuilder ipAddressBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int octet = random.nextInt(256); // Generate a random number between 0 and 255
            ipAddressBuilder.append(octet);
            if (i < 3) {
                ipAddressBuilder.append(".");
            }
        }
        return ipAddressBuilder.toString();
    }

    public static String uuid() {
        return "b3567d87-3e7f-44bc-92b6-" +
                String.valueOf(System.currentTimeMillis()).substring(5) +
                "cd6a";
    }

    public static ArrayList<String> coupons(String prefix, int count) {
        ArrayList<String> coupons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            coupons.add(prefix + number() + "-" + i);
        }
        return coupons;
    }

    public static ArrayList<String> coupons(int count) {
        return coupons("COUPON-", count);
    }

    public static ArrayList<String> generateIpsList(int count) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String ip = ip();
            list.add(ip);
        }
        return list;
    }

    public static ArrayList<String> generateEmailsList(int count) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String email = email();
            list.add(email);
        }
        return list;
    }

    public static String userName() {
        StringBuilder username = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            username.append(getRandomLatinLetter());
        }
        return username.toString();
    }

    @SneakyThrows
    private static char getRandomLatinLetter() {
        java.util.Random rand = SecureRandom.getInstanceStrong();
        return (char) (rand.nextInt(26) + 'a');
    }

}
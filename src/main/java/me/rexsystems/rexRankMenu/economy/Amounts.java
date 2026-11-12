package me.rexsystems.rexRankMenu.economy;

import java.math.BigDecimal;

public final class Amounts {

    private Amounts() {
    }

    public static String format(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return "0";
        }
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }

    public static double parse(String raw) {
        if (raw == null) {
            return Double.NaN;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.indexOf('%') >= 0) {
            return Double.NaN;
        }
        StringBuilder number = new StringBuilder();
        boolean comma = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == ',') {
                comma = true;
                continue;
            }
            if ((c >= '0' && c <= '9') || c == '.' || (c == '-' && number.isEmpty())) {
                number.append(c);
            }
        }
        if (number.isEmpty() || (comma && number.indexOf(".") < 0 && number.length() == 0)) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(number.toString());
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    public static boolean covers(double balance, double price) {
        return !Double.isNaN(balance) && balance + 0.0000001d >= price;
    }

    public static String command(String template, String player, double amount) {
        String value = template == null ? "" : template.trim();
        if (value.startsWith("/")) {
            value = value.substring(1).trim();
        }
        String name = player == null ? "" : player;
        return value.replace("{player}", name).replace("{amount}", format(amount));
    }
}

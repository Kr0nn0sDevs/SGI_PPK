package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Fmt {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String moneda(double v) {
        return String.format(new Locale("es","MX"), "$%,.2f", v);
    }
    public static String dt(LocalDateTime t) { return t != null ? t.format(DT) : ""; }
    public static String d(LocalDateTime t)  { return t != null ? t.format(D)  : ""; }
    public static String folio(long n)       { return String.format("VTA-%06d", n); }
}

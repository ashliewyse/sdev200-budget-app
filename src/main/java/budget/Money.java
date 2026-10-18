package budget;
import java.math.BigDecimal;
import java.math.RoundingMode;
/** Validates dollar amounts without binary floating-point rounding. */
public final class Money {
    private static final BigDecimal MAX = new BigDecimal("999999999.99");
    private Money() { }
    public static BigDecimal parse(String raw, boolean positive) {
        if (raw == null || !raw.trim().matches("[0-9]{1,9}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Enter a dollar amount with at most two decimal places.");
        }
        return amount(new BigDecimal(raw.trim()), positive);
    }
    static BigDecimal amount(BigDecimal value, boolean positive) {
        if (value == null || value.signum() < 0 || (positive && value.signum() == 0) || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException(positive ? "Amount must be greater than zero and at most 999999999.99." : "Income must be between zero and 999999999.99.");
        }
        try { return value.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("Enter whole cents only."); }
    }
}

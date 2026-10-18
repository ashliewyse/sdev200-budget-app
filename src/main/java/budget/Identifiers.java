package budget;
import java.util.UUID;
/** Canonical UUIDs keep display text out of file paths. */
final class Identifiers {
    private Identifiers() { }
    static String require(String id) {
        if (id == null) throw new IllegalArgumentException("Select a valid profile or entry.");
        try {
            if (!UUID.fromString(id).toString().equals(id)) throw new IllegalArgumentException();
            return id;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Select a valid profile or entry.");
        }
    }
}

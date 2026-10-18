package budget;
/** A local profile; selection is not password authentication. */
public final class UserProfile {
    private final String id;
    private final String displayName;
    public UserProfile(String id, String displayName) {
        this.id = Identifiers.require(id);
        if (displayName == null || displayName.trim().isEmpty() || displayName.trim().length() > 40) {
            throw new IllegalArgumentException("Profile name must contain 1 to 40 characters.");
        }
        this.displayName = displayName.trim();
    }
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
}

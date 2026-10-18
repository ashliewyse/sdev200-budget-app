package budget;
/** File problems are reported without pretending that saved data is empty. */
public final class BudgetStorageException extends RuntimeException {
    public BudgetStorageException(String message, Throwable cause) { super(message, cause); }
}

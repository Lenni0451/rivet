package net.lenni0451.rivet.property;

public enum SyncMode {

    /**
     * Changes from external stores will not be synced automatically.<br>
     * The implementation must call {@code refresh()} manually to call change listeners.
     */
    MANUAL,
    /**
     * Changes are automatically checked on every {@code get()} call.<br>
     * This could cause a performance impact if the get method is called frequently.
     */
    POLL_ON_GET

}

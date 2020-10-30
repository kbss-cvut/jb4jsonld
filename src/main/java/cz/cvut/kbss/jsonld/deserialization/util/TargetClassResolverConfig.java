package cz.cvut.kbss.jsonld.deserialization.util;

/**
 * Configuration object for the {@link TargetClassResolver}.
 */
public final class TargetClassResolverConfig {

    private final boolean allowAssumingTargetType;
    private final boolean optimisticTypeResolutionEnabled;
    private final boolean preferSuperclass;

    public TargetClassResolverConfig() {
        this.allowAssumingTargetType = false;
        this.optimisticTypeResolutionEnabled = false;
        this.preferSuperclass = false;
    }

    public TargetClassResolverConfig(boolean allowAssumingTargetType, boolean optimisticTypeResolutionEnabled,
                                     boolean preferSuperclass) {
        this.allowAssumingTargetType = allowAssumingTargetType;
        this.optimisticTypeResolutionEnabled = optimisticTypeResolutionEnabled;
        this.preferSuperclass = preferSuperclass;
    }

    public boolean shouldAllowAssumingTargetType() {
        return allowAssumingTargetType;
    }

    public boolean isOptimisticTypeResolutionEnabled() {
        return optimisticTypeResolutionEnabled;
    }

    public boolean shouldPreferSuperclass() {
        return preferSuperclass;
    }
}

package cz.cvut.kbss.jsonld.deserialization.util;

public final class TargetClassResolverConfig {

    private final boolean allowAssumingTargetType;
    private final boolean optimisticTypeResolution;
    private final boolean preferSuperclass;

    public TargetClassResolverConfig() {
        this.allowAssumingTargetType = false;
        this.optimisticTypeResolution = false;
        this.preferSuperclass = false;
    }

    public TargetClassResolverConfig(boolean allowAssumingTargetType, boolean optimisticTypeResolution,
                                     boolean preferSuperclass) {
        this.allowAssumingTargetType = allowAssumingTargetType;
        this.optimisticTypeResolution = optimisticTypeResolution;
        this.preferSuperclass = preferSuperclass;
    }

    public boolean shouldAllowAssumingTargetType() {
        return allowAssumingTargetType;
    }

    public boolean isOptimisticTypeResolution() {
        return optimisticTypeResolution;
    }

    public boolean shouldPreferSuperclass() {
        return preferSuperclass;
    }
}

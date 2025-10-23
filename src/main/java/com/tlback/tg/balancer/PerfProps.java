package com.tlback.tg.balancer;

public record PerfProps(String groupId, int prioriy) {
    private static final String UNGROUPED_STRING = "ungrouped";

    public enum PrioriySelector {
        LOW(-1), NORAMAL(0), HIGH(5), MAX(10);
        private final int value;

        PrioriySelector(int value) {
            this.value = value;
        }

    }

    public PerfProps {
        if (groupId == null)
            groupId = UNGROUPED_STRING;
    }

    public static PerfProps of(String tgId) {
        return new PerfProps(tgId, 0);
    }

    public static PerfProps of(PrioriySelector priority) {
        return new PerfProps(UNGROUPED_STRING, priority.value);
    }

    public static PerfProps of() {
        return new PerfProps(UNGROUPED_STRING, 0);
    }
}

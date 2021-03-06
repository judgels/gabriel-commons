package org.iatoki.judgels.gabriel.sandboxes;

public final class SandboxExecutionResult {
    private final SandboxExecutionStatus status;
    private final int time;
    private final int memory;
    private final String message;

    public SandboxExecutionResult(SandboxExecutionStatus status, int time, int memory, String message) {
        this.status = status;
        this.time = time;
        this.memory = memory;
        this.message = message;
    }

    public static SandboxExecutionResult plainMessage(String message) {
        return new SandboxExecutionResult(SandboxExecutionStatus.ZERO_EXIT_CODE, 0, 0, message);
    }

    public static SandboxExecutionResult internalError(String message) {
        return new SandboxExecutionResult(SandboxExecutionStatus.INTERNAL_ERROR, -1, -1, message);
    }

    public SandboxExecutionStatus getStatus() {
        return status;
    }

    public int getTimeInMilliseconds() {
        return time;
    }

    public int getMemoryInKilobytes() {
        return memory;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}

package com.example.banking.interbank.application.out;

/** A downstream step failed; adapters translate their technical errors into this one. */
public class TransferStepFailedException extends RuntimeException {

    private final String step;

    public TransferStepFailedException(String step, Throwable cause) {
        super("Step '" + step + "' failed", cause);
        this.step = step;
    }

    public String step() {
        return step;
    }
}

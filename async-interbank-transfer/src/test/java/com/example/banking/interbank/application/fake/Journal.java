package com.example.banking.interbank.application.fake;

import java.util.ArrayList;
import java.util.List;

/** Ordered log of the side effects the fakes receive, shared by all fakes of one scenario. */
public class Journal {

    private final List<String> entries = new ArrayList<>();

    void add(String entry) {
        entries.add(entry);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}

package com.example.banking.interbank.application.service;

/** Process codes sent downstream per kind of work, and the status an exhausted order is left in. */
public record ProcessSettings(String executeProcessCode, String modifyProcessCode, String errorStatus) {
}

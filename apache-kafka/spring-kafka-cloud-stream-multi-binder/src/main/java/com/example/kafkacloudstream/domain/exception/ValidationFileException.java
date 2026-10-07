package com.example.kafkacloudstream.domain.exception;


public class ValidationFileException extends CodeException {

  public ValidationFileException() {
    super(
        CodeFile.ERROR_CODE_VALIDATION_FILE_EXCEPTION,
        "Error in SP - CreditOrdersApi - POST sp-list-beneficiary-payment."
    );
  }
}

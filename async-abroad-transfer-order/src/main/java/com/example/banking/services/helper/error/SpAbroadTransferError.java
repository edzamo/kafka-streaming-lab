package com.example.banking.services.helper.error;

import com.example.banking.services.server.models.Error;
import org.springframework.http.HttpStatus;


public class SpAbroadTransferError {

    private SpAbroadTransferError(){}

    static final String USER_MESSAGE = "Error obtaining the information";

    static final String ERROR_CODE_01 = "SP01";
    static final String MESSAGE_01 = "Unexpected error";
    static final Integer STATUS_01 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE01_UNEXPECTED_ERROR = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_01)
                    .developerMessage(MESSAGE_01)
                    .userMessage(MESSAGE_01), STATUS_01);


    static final String ERROR_CODE_02 = "SP02";
    static final String DEVELOPER_MESSAGE_02 = "Error API SP ABROAD TRANSFER ORDER";
    static final Integer STATUS_02 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE02_SP_ABROAD_TRANSFER_ORDER = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_02)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_02), STATUS_02);


    static final String ERROR_CODE_03 = "SP03";
    static final String DEVELOPER_MESSAGE_03 = "Order not found";
    static final Integer STATUS_03 = HttpStatus.NOT_FOUND.value();

    public  static final FailureException FE03_ORDER_NOT_FOUND = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_03)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_03), STATUS_03);


    static final String ERROR_CODE_04 = "SP02";
    static final String DEVELOPER_MESSAGE_04 = "Error API BS ABROAD TRANSFER ORDER";
    static final Integer STATUS_04 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE04_BS_ABROAD_TRANSFER_ORDER = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_04)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_04), STATUS_04);

    static final String ERROR_CODE_05 = "SP02";
    static final String DEVELOPER_MESSAGE_05 = "Error API BS ACCOUNT";
    static final Integer STATUS_05 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE05_SP_ACCOUNT = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_05)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_05), STATUS_05);

    static final String ERROR_CODE_06 = "SP02";
    static final String DEVELOPER_MESSAGE_06 = "Error API SP APPROVAL ENGINE SHEDULED TASK";
    static final Integer STATUS_06 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE05_SP_TASK = new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_06)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_06), STATUS_06);

    static final String ERROR_CODE_07 = "SP02";
    static final String DEVELOPER_MESSAGE_07 = "Error API BS ORDER";
    static final Integer STATUS_07 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE05_BS_ORDER= new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_07)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_07), STATUS_07);

    static final String ERROR_CODE_08 = "SP02";
    static final String DEVELOPER_MESSAGE_08 = "Error API BS Order History";
    static final Integer STATUS_08 = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public  static final FailureException FE08_BS_ORDER_HISTORY= new FailureException(
            new Error()
                    .errorCode(ERROR_CODE_08)
                    .userMessage(USER_MESSAGE)
                    .developerMessage(DEVELOPER_MESSAGE_08), STATUS_08);

}

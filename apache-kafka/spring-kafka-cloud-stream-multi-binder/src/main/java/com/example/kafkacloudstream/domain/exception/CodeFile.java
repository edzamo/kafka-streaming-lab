package com.example.kafkacloudstream.domain.exception;


import lombok.experimental.UtilityClass;

@UtilityClass
public final class CodeFile {


  public static final String CODE_VALIDATION_TABS_OK = "ok.tsv.tab.001";
  public static final String CODE_VALIDATION_TABS_ERROR = "error.tsv.tab.001";
  public static final String VALIDATION_TABS_ERROR_DETAIL = "Error: Delimitador de columnas incorrecto: El delimitador de columnas deberá ser 'tabulador'";
  public static final String CODE_VALIDATION_HEADER_OK = "ok.tsv.header.001";
  public static final String CODE_VALIDATION_HEADER_ERROR = "error.tsv.header.001";
  public static final String VALIDATION_HEADER_ERROR_DETAIL = "Error: El archivo no contiene las columnas requeridas";
  public static final String CODE_VALIDATION_ROWS_EXIT_OK = "ok.tsv.rowsExist.001";
  public static final String CODE_VALIDATION_ROWS_EXIT_ERROR = "error.tsv.RowsExist.001";
  public static final String VALIDATION_ROWS_EXIT_ERROR_DETAIL = "Error: El archivo no contiene las columnas requeridas";
  public static final String CODE_DATA_VALIDATION_CODE_BANK_OK = "ok.tsv.codebank.001";
  public static final String DATA_VALIDATION_CODE_BANK_BLANK_EMPTY_DETAIL = "Error: Valor COD_BCO en Blanco ";
  public static final String DATA_VALIDATION_CODE_BANK_NOT_EXIST_DETAIL = "Error: Código de Banco en Columna COD_BCO ";
  public static final String DATA_VALIDATION_CODE_BANK_CRD_DETAIL = "Error: Código de Banco en Columna COD_BCO incorrecto. Para el Servicio CRD sólo se permite '0010' ";
  public static final String DATA_VALIDATION_CURRENCY_BLANK_EMPTY_DETAIL = "Error: Valor MONEDA en Blanco ";
  public static final String DATA_VALIDATION_CURRENCY_NOT_EXIST_DETAIL = "Error: Código de Divisa en Columna MONEDA incorrecto ";
  public static final String DATA_VALIDATION_NAME_BLANK_EMPTY_DETAIL = "Error: Valor NOMBRE en Blanco ";
  public static final String DATA_VALIDATION_NAME_SIZE_DETAIL = "Error: Valor en Columna NOMBRE. El valor no puede exceder 100 caracteres ";
  public static final int ERROR_CODE_VALIDATION_FILE_EXCEPTION = 9001;
}

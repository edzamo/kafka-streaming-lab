package com.example.kafkacloudstream.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusTypeEnum {
  STATUS("INFORMATION"), ERROR("ERROR"), OUTPUT("DATA VALIDATION"), FULLOUTPUT("WARNING"), FILE(
      "WARNING"), STOP(
      "STOP PROCESS");
  private String name;

}

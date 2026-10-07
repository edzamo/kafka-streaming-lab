package com.example.kafkacloudstream.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Generated
public enum MethodEnum {

  UPLOAD("upload"),
  DOWNLOAD("download");
  private final String value;


}
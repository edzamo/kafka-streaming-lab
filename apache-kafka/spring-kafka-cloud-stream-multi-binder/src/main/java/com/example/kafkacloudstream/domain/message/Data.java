package com.example.kafkacloudstream.domain.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Data {

  String api;
  String clientRequestId;
  String requestId;
  String eTag;
  String contentType;
  int contentLength;
  String blobType;
  String url;
  String originUrl;
  String sequencer;
  StorageDiagnostics storageDiagnostics;
  String referenceId;
  String referenceName;
  String id;
  String originFileName;
  String hash;
  String channel;
  String product;
  Status status;
}

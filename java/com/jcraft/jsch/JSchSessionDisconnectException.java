package com.jcraft.jsch;

public class JSchSessionDisconnectException extends JSchException {
  private static final long serialVersionUID = -1L;

  // RFC 4253 11.1.
  private final int reasonCode; // RFC 4250 4.2.2.
  private final String description;
  private final String languageTag;

  JSchSessionDisconnectException(String s, int reasonCode, String description, String languageTag) {
    super(s);
    this.reasonCode = reasonCode;
    this.description = description;
    this.languageTag = languageTag;
  }

  public int getReasonCode() {
    return reasonCode;
  }

  public String getDescription() {
    return description;
  }

  public String getLanguageTag() {
    return languageTag;
  }
}

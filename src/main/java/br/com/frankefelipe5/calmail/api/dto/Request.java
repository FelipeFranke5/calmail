package br.com.frankefelipe5.calmail.api.dto;

import br.com.frankefelipe5.calmail.api.exception.RequestException;

public record Request(
    String name,
    Boolean recurrent,
    Boolean hasProtocol,
    Integer protocolDeadlineInDays,
    String protocolStatus,
    String message) {
  public Request {
    validateRequiredArgs(recurrent, hasProtocol);
    validateHasProtocolWithRequiredArgs(hasProtocol, protocolDeadlineInDays, protocolStatus);
    validateDeadline(protocolDeadlineInDays);
    validateProtocolStatus(protocolStatus);
    validateMessage(message);
  }

  public Request() {
    this(null, null, null, null, null, null);
  }

  public Request(String name) {
    this(name, null, null, null, null, null);
  }

  public Request(Boolean recurrent, Boolean hasProtocol, String message) {
    this(null, recurrent, hasProtocol, null, null, message);
  }

  public Request(
      Boolean hasProtocol, Integer protocolDeadlineInDays, String protocolStatus, String message) {
    this(null, null, hasProtocol, protocolDeadlineInDays, protocolStatus, message);
  }

  public Request(Boolean recurrent, String message) {
    this(null, recurrent, null, null, null, message);
  }

  public Request(Boolean recurrent, Boolean hasProtocol, String protocolStatus, String message) {
    this(null, recurrent, hasProtocol, null, protocolStatus, message);
  }

  public Request(
      Boolean recurrent, Boolean hasProtocol, Integer protocolDeadlineInDays, String message) {
    this(null, recurrent, hasProtocol, protocolDeadlineInDays, null, message);
  }

  public Request(
      Boolean recurrent,
      Boolean hasProtocol,
      Integer protocolDeadlineInDays,
      String protocolStatus,
      String message) {
    this(null, recurrent, hasProtocol, protocolDeadlineInDays, protocolStatus, message);
  }

  public Request(Boolean recurrent, Boolean hasProtocol) {
    this(null, recurrent, hasProtocol, null, null, null);
  }

  private void validateRequiredArgs(Boolean recurrent, Boolean hasProtocol) {
    if (recurrent == null && hasProtocol == null) {
      throw new RequestException("the following fields are missing: recurrent, hasProtocol");
    } else if (recurrent == null) {
      throw new RequestException("the field recurrent is missing");
    } else if (hasProtocol == null) {
      throw new RequestException("the field hasProtocol is missing");
    }
  }

  private void validateHasProtocolWithRequiredArgs(
      Boolean hasProtocol, Integer protocolDeadlineInDays, String protocolStatus) {
    if ((hasProtocol == true) && (protocolDeadlineInDays == null || protocolStatus == null)) {
      throw new RequestException(
          "the fields protocolDeadlineInDays and protocolStatus are mandatory");
    }
    if ((hasProtocol == false) && (protocolDeadlineInDays != null || protocolStatus != null)) {
      throw new RequestException(
          "hasProtocol is set to false, therefore protocolDeadlineInDays or protocolStatus should not be filled");
    }
  }

  private void validateDeadline(Integer protocolDeadlineInDays) {
    if ((protocolDeadlineInDays != null)
        && (protocolDeadlineInDays < 3 || protocolDeadlineInDays > 7)) {
      throw new RequestException("protocolDeadlineInDays should be between 3 and 7");
    }
  }

  private void validateProtocolStatus(String protocolStatus) {
    if (protocolStatus != null && protocolStatus.length() > 30) {
      throw new RequestException("protocolStatus should have less than 30 characters");
    }
    if ((protocolStatus != null) && (protocolStatus.isEmpty() || protocolStatus.isBlank())) {
      throw new RequestException("protocolStatus cannot be empty");
    }
  }

  private void validateMessage(String message) {
    if (message == null) {
      throw new RequestException("the message is required");
    }
    if ((message != null) && (message.isEmpty() || message.isBlank())) {
      throw new RequestException("the message cannot be empty");
    }
  }
}

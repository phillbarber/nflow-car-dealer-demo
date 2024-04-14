package com.github.phillbarber.nflow.remoteservices;

public record OrderValidationResponse (String rejectionMessage, boolean isValid) {

}

package com.pocket.spring.application.exception;

public class ApiException extends RuntimeException {


    public static class UserNameAlreadyExistsException extends RuntimeException {
        public UserNameAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class UserIdNotFoundException extends RuntimeException {
        public UserIdNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidRequestField extends RuntimeException {
        public InvalidRequestField(String message) {
            super(message);
        }
    }

    public static class InvalidEmailFormat extends RuntimeException {
        public InvalidEmailFormat(String message) {
            super(message);
        }
    }

    public static class UserIsInactive extends RuntimeException {
        public UserIsInactive(String message) {
            super(message);
        }
    }

}
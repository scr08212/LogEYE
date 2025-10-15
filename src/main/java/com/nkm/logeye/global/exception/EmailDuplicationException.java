package com.nkm.logeye.global.exception;

public class EmailDuplicationException extends BusinessException {
    public EmailDuplicationException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}

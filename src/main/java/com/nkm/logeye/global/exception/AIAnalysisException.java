package com.nkm.logeye.global.exception;

public class AIAnalysisException extends BusinessException {

    public AIAnalysisException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AIAnalysisException(ErrorCode errorCode, Throwable cause) {
        super(errorCode);
    }
}

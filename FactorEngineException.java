package com.quant.altdata.exception;

/**
 * 因子引擎异常类
 */
public class FactorEngineException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * 构造函数
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public FactorEngineException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public FactorEngineException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     * @return 错误代码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        DATA_FETCH_ERROR(1001),
        DATA_PROCESSING_ERROR(1002),
        MODEL_TRAINING_ERROR(2001),
        MODEL_PREDICTION_ERROR(2002),
        BACKTEST_ERROR(3001),
        VISUALIZATION_ERROR(4001),
        CONFIGURATION_ERROR(5001),
        UNKNOWN_ERROR(9999);

        private final int code;

        ErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
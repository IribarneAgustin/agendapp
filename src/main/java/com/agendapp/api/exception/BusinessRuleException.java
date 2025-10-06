package com.agendapp.api.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessRuleException extends RuntimeException {
    private final String code;
    private final Map<String, Object> details;

    public BusinessRuleException(String code) {
        this(code, Map.of());
    }

    public BusinessRuleException(String code, Map<String, Object> details) {
        super(code);
        this.code = code;
        this.details = details;
    }

}

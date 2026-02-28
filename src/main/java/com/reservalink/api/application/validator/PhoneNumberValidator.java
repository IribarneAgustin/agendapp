package com.reservalink.api.application.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberValidator {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public String formatAndValidate(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            throw new BusinessRuleException(BusinessErrorCodes.INVALID_PHONE_NUMBER.name());
        }

        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(rawNumber, "AR");

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw new BusinessRuleException(BusinessErrorCodes.INVALID_PHONE_NUMBER.name());
            }

            return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Unable to parse phone number: " + e.getMessage());
        }
    }
}
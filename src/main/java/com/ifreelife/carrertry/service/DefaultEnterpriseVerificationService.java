package com.ifreelife.carrertry.service;

import org.springframework.stereotype.Service;

@Service
public class DefaultEnterpriseVerificationService implements EnterpriseVerificationService {

    @Override
    public boolean verify(String enterpriseName, String unifiedSocialCreditCode, String legalRepresentative) {
        return isNotBlank(enterpriseName) && isNotBlank(unifiedSocialCreditCode) && isNotBlank(legalRepresentative);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}

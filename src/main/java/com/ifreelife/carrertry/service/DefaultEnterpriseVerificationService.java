package com.ifreelife.carrertry.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultEnterpriseVerificationService implements EnterpriseVerificationService {

    @Override
    public boolean verify(String enterpriseName, String unifiedSocialCreditCode, String legalRepresentative) {
        return StringUtils.hasText(enterpriseName)
            && StringUtils.hasText(unifiedSocialCreditCode)
            && StringUtils.hasText(legalRepresentative);
    }
}

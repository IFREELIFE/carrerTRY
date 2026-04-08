package com.ifreelife.carrertry.service;

public interface EnterpriseVerificationService {
    boolean verify(String enterpriseName, String unifiedSocialCreditCode, String legalRepresentative);
}

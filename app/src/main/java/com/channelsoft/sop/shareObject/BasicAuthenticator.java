package com.channelsoft.sop.shareObject;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public final class BasicAuthenticator extends Authenticator {
    private String consumer_key = "ck_67a13ccfc27b004012944f512a69884502b5f7be";
    private String consumer_secret = "cs_52989cfaefd7b3497a99c3116733c034321de742";
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(consumer_key, consumer_secret.toCharArray());
    }
}
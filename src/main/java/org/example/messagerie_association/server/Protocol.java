package org.example.messagerie_association.server;

public final class Protocol {

    public static final String SEP = "|";
    public static final String END = "END";

    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String INSCRIRE = "INSCRIRE";
    public static final String GET_MEMBERS_ONLINE = "GET_MEMBERS_ONLINE";
    public static final String GET_MESSAGES = "GET_MESSAGES";
    public static final String SEND_MESSAGE = "SEND_MESSAGE";
    public static final String GET_MEMBERS = "GET_MEMBERS";
    public static final String GET_ALL_MEMBERS = "GET_ALL_MEMBERS";

    public static final String OK = "OK";
    public static final String KO = "KO";
    public static final String PREFIX_MEMBER = "M";
    public static final String PREFIX_MSG = "MSG";

    private Protocol() {}
}

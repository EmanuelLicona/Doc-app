package com.application.pm1_proyecto_final.models;

import java.util.Date;

public class ChatMessage {

    public static final String STATUS_SENT = "1";
    public static final String STATUS_DELETE = "2";


    public String idFirebase, senderId, groupId, message, datatime, status, position;
    public Date dateObject;
}

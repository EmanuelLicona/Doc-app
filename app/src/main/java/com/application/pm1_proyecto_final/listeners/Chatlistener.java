package com.application.pm1_proyecto_final.listeners;

import com.application.pm1_proyecto_final.models.ChatMessage;
import com.application.pm1_proyecto_final.models.Group;

public interface Chatlistener {

    void onClickChat(ChatMessage chatMessage, int position);
}

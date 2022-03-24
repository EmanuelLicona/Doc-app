package com.application.pm1_proyecto_final.listeners;

import com.application.pm1_proyecto_final.models.Publication;

public interface Chatlistener {

    void onClickChat(Publication publication, int position);
    void onClickFile(Publication publication, int position);
}

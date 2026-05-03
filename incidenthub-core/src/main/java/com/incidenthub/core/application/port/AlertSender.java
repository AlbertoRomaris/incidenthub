package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.alert.Alert;

public interface AlertSender {

    void send(Alert alert);
}
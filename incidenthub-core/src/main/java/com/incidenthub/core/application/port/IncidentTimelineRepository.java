package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;

import java.util.List;

public interface IncidentTimelineRepository {

    IncidentTimelineEvent save(IncidentTimelineEvent event);

    List<IncidentTimelineEvent> findByIncidentId(IncidentId incidentId);
}
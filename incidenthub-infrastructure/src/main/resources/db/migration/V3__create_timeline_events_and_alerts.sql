CREATE TABLE incident_timeline_events (
                                          id UUID PRIMARY KEY,

                                          incident_id UUID NOT NULL,

                                          event_type VARCHAR(80) NOT NULL,

                                          occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                          summary TEXT NOT NULL,
                                          actor VARCHAR(120) NOT NULL DEFAULT 'system',

                                          attributes_json JSONB NOT NULL DEFAULT '{}'::jsonb,

                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                                          CONSTRAINT fk_incident_timeline_events_incident
                                              FOREIGN KEY (incident_id)
                                                  REFERENCES incidents(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT incident_timeline_events_type_check CHECK (
                                              event_type IN (
                                                             'INCIDENT_OPENED',
                                                             'INCIDENT_ACKNOWLEDGED',
                                                             'INCIDENT_RESOLVED',
                                                             'INCIDENT_REOPENED',
                                                             'EVIDENCE_ATTACHED',
                                                             'RUNBOOK_ATTACHED',
                                                             'ALERT_REQUESTED',
                                                             'ALERT_SENT',
                                                             'ALERT_FAILED'
                                                  )
                                              )
);

CREATE INDEX idx_incident_timeline_events_incident_id
    ON incident_timeline_events(incident_id);

CREATE INDEX idx_incident_timeline_events_event_type
    ON incident_timeline_events(event_type);

CREATE INDEX idx_incident_timeline_events_occurred_at
    ON incident_timeline_events(occurred_at);

CREATE TABLE alerts (
                        id UUID PRIMARY KEY,

                        incident_id UUID NOT NULL,

                        channel VARCHAR(40) NOT NULL,
                        status VARCHAR(40) NOT NULL,

                        title TEXT NOT NULL,
                        message TEXT NOT NULL,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        sent_at TIMESTAMP WITH TIME ZONE,
                        failed_at TIMESTAMP WITH TIME ZONE,
                        failure_reason TEXT,

                        attributes_json JSONB NOT NULL DEFAULT '{}'::jsonb,

                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                        CONSTRAINT fk_alerts_incident
                            FOREIGN KEY (incident_id)
                                REFERENCES incidents(id)
                                ON DELETE CASCADE,

                        CONSTRAINT alerts_channel_check CHECK (
                            channel IN (
                                        'LOG',
                                        'WEBHOOK',
                                        'EMAIL',
                                        'SLACK',
                                        'SNS'
                                )
                            ),

                        CONSTRAINT alerts_status_check CHECK (
                            status IN (
                                       'PENDING',
                                       'SENT',
                                       'FAILED',
                                       'SUPPRESSED'
                                )
                            ),

                        CONSTRAINT alerts_sent_state_check CHECK (
                            status <> 'SENT' OR sent_at IS NOT NULL
                            ),

                        CONSTRAINT alerts_failed_state_check CHECK (
                            status <> 'FAILED' OR failed_at IS NOT NULL
                            )
);

CREATE INDEX idx_alerts_incident_id
    ON alerts(incident_id);

CREATE INDEX idx_alerts_status
    ON alerts(status);

CREATE INDEX idx_alerts_channel
    ON alerts(channel);

CREATE INDEX idx_alerts_created_at
    ON alerts(created_at);
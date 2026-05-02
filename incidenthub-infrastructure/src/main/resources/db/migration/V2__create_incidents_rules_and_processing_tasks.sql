CREATE TABLE rules (
                       id UUID PRIMARY KEY,

                       name VARCHAR(160) NOT NULL,
                       description TEXT NOT NULL DEFAULT '',

                       service_pattern VARCHAR(120) NOT NULL,
                       signal_type VARCHAR(60) NOT NULL,
                       condition_type VARCHAR(80) NOT NULL,

                       threshold INTEGER NOT NULL,
                       time_window_seconds INTEGER NOT NULL,

                       incident_type VARCHAR(80) NOT NULL,
                       incident_severity VARCHAR(40) NOT NULL,

                       enabled BOOLEAN NOT NULL DEFAULT true,

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                       CONSTRAINT rules_signal_type_check CHECK (
                           signal_type IN (
                                           'ERROR',
                                           'LATENCY',
                                           'TIMEOUT',
                                           'HEARTBEAT',
                                           'DEPENDENCY_FAILURE',
                                           'QUEUE_BACKLOG',
                                           'DEPLOYMENT_EVENT',
                                           'CUSTOM'
                               )
                           ),

                       CONSTRAINT rules_condition_type_check CHECK (
                           condition_type IN (
                                              'COUNT_OVER_WINDOW',
                                              'CONSECUTIVE_FAILURES',
                                              'LATENCY_THRESHOLD',
                                              'MISSING_HEARTBEAT'
                               )
                           ),

                       CONSTRAINT rules_incident_type_check CHECK (
                           incident_type IN (
                                             'HIGH_ERROR_RATE',
                                             'HIGH_LATENCY',
                                             'TIMEOUT_SPIKE',
                                             'SERVICE_DOWN',
                                             'DEPENDENCY_FAILURE',
                                             'QUEUE_BACKLOG',
                                             'CUSTOM'
                               )
                           ),

                       CONSTRAINT rules_incident_severity_check CHECK (
                           incident_severity IN (
                                                 'LOW',
                                                 'MEDIUM',
                                                 'HIGH',
                                                 'CRITICAL'
                               )
                           ),

                       CONSTRAINT rules_threshold_check CHECK (threshold > 0),
                       CONSTRAINT rules_time_window_check CHECK (time_window_seconds > 0)
);

CREATE INDEX idx_rules_enabled
    ON rules(enabled);

CREATE INDEX idx_rules_service_pattern_signal_type
    ON rules(service_pattern, signal_type);

CREATE TABLE incidents (
                           id UUID PRIMARY KEY,

                           service_name VARCHAR(120) NOT NULL,
                           environment VARCHAR(60) NOT NULL,

                           incident_type VARCHAR(80) NOT NULL,
                           severity VARCHAR(40) NOT NULL,
                           status VARCHAR(40) NOT NULL,

                           summary TEXT NOT NULL,
                           description TEXT NOT NULL DEFAULT '',

                           deduplication_key VARCHAR(260) NOT NULL,

                           occurrence_count INTEGER NOT NULL,

                           first_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           acknowledged_at TIMESTAMP WITH TIME ZONE,
                           resolved_at TIMESTAMP WITH TIME ZONE,

                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                           CONSTRAINT incidents_type_check CHECK (
                               incident_type IN (
                                                 'HIGH_ERROR_RATE',
                                                 'HIGH_LATENCY',
                                                 'TIMEOUT_SPIKE',
                                                 'SERVICE_DOWN',
                                                 'DEPENDENCY_FAILURE',
                                                 'QUEUE_BACKLOG',
                                                 'CUSTOM'
                                   )
                               ),

                           CONSTRAINT incidents_severity_check CHECK (
                               severity IN (
                                            'LOW',
                                            'MEDIUM',
                                            'HIGH',
                                            'CRITICAL'
                                   )
                               ),

                           CONSTRAINT incidents_status_check CHECK (
                               status IN (
                                          'OPEN',
                                          'ACKNOWLEDGED',
                                          'RESOLVED'
                                   )
                               ),

                           CONSTRAINT incidents_occurrence_count_check CHECK (occurrence_count > 0),
                           CONSTRAINT incidents_last_seen_check CHECK (last_seen_at >= first_seen_at)
);

CREATE INDEX idx_incidents_status
    ON incidents(status);

CREATE INDEX idx_incidents_severity
    ON incidents(severity);

CREATE INDEX idx_incidents_service_environment
    ON incidents(service_name, environment);

CREATE INDEX idx_incidents_opened_at
    ON incidents(opened_at);

CREATE INDEX idx_incidents_deduplication_key
    ON incidents(deduplication_key);

CREATE UNIQUE INDEX ux_incidents_active_deduplication_key
    ON incidents(deduplication_key)
    WHERE status IN ('OPEN', 'ACKNOWLEDGED');

CREATE TABLE incident_evidence (
                                   id UUID PRIMARY KEY,

                                   incident_id UUID NOT NULL,
                                   signal_id UUID NOT NULL,
                                   rule_id UUID NOT NULL,

                                   captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                   summary TEXT NOT NULL,

                                   attributes_json JSONB NOT NULL DEFAULT '{}'::jsonb,

                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                                   CONSTRAINT fk_incident_evidence_incident
                                       FOREIGN KEY (incident_id)
                                           REFERENCES incidents(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_incident_evidence_signal
                                       FOREIGN KEY (signal_id)
                                           REFERENCES signals(id),

                                   CONSTRAINT fk_incident_evidence_rule
                                       FOREIGN KEY (rule_id)
                                           REFERENCES rules(id),

                                   CONSTRAINT ux_incident_evidence_incident_signal_rule
                                       UNIQUE (incident_id, signal_id, rule_id)
);

CREATE INDEX idx_incident_evidence_incident_id
    ON incident_evidence(incident_id);

CREATE INDEX idx_incident_evidence_signal_id
    ON incident_evidence(signal_id);

CREATE INDEX idx_incident_evidence_rule_id
    ON incident_evidence(rule_id);

CREATE TABLE signal_processing_tasks (
                                         signal_id UUID PRIMARY KEY,

                                         status VARCHAR(40) NOT NULL DEFAULT 'PENDING',

                                         attempts INTEGER NOT NULL DEFAULT 0,

                                         locked_by VARCHAR(120),
                                         locked_at TIMESTAMP WITH TIME ZONE,

                                         processed_at TIMESTAMP WITH TIME ZONE,
                                         last_error TEXT,

                                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                                         CONSTRAINT fk_signal_processing_tasks_signal
                                             FOREIGN KEY (signal_id)
                                                 REFERENCES signals(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT signal_processing_tasks_status_check CHECK (
                                             status IN (
                                                        'PENDING',
                                                        'PROCESSING',
                                                        'PROCESSED',
                                                        'FAILED'
                                                 )
                                             ),

                                         CONSTRAINT signal_processing_tasks_attempts_check CHECK (attempts >= 0)
);

CREATE INDEX idx_signal_processing_tasks_status
    ON signal_processing_tasks(status);

CREATE INDEX idx_signal_processing_tasks_locked_at
    ON signal_processing_tasks(locked_at);

CREATE INDEX idx_signal_processing_tasks_created_at
    ON signal_processing_tasks(created_at);

INSERT INTO rules (
    id,
    name,
    description,
    service_pattern,
    signal_type,
    condition_type,
    threshold,
    time_window_seconds,
    incident_type,
    incident_severity,
    enabled
) VALUES
      (
          '11111111-1111-1111-1111-111111111111',
          'High error rate on payment-service',
          'Opens an incident when payment-service emits too many ERROR signals within a short time window.',
          'payment-service',
          'ERROR',
          'COUNT_OVER_WINDOW',
          5,
          60,
          'HIGH_ERROR_RATE',
          'HIGH',
          true
      ),
      (
          '22222222-2222-2222-2222-222222222222',
          'High latency on payment-service',
          'Opens an incident when payment-service emits repeated LATENCY signals within a short time window.',
          'payment-service',
          'LATENCY',
          'COUNT_OVER_WINDOW',
          3,
          60,
          'HIGH_LATENCY',
          'MEDIUM',
          true
      ),
      (
          '33333333-3333-3333-3333-333333333333',
          'Timeout spike on payment-service',
          'Opens an incident when payment-service emits repeated TIMEOUT signals within a short time window.',
          'payment-service',
          'TIMEOUT',
          'COUNT_OVER_WINDOW',
          3,
          60,
          'TIMEOUT_SPIKE',
          'HIGH',
          true
      );

INSERT INTO signal_processing_tasks (
    signal_id,
    status
)
SELECT
    id,
    'PENDING'
FROM signals
    ON CONFLICT (signal_id) DO NOTHING;
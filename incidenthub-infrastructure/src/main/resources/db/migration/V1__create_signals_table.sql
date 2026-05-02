CREATE TABLE signals (
                         id UUID PRIMARY KEY,

                         service_name VARCHAR(120) NOT NULL,
                         environment VARCHAR(60) NOT NULL,

                         signal_type VARCHAR(60) NOT NULL,
                         severity VARCHAR(40) NOT NULL,

                         message TEXT NOT NULL,

                         correlation_id VARCHAR(120),
                         trace_id VARCHAR(120),
                         span_id VARCHAR(120),

                         latency_ms INTEGER,
                         status_code INTEGER,
                         error_code VARCHAR(120),

                         observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         received_at TIMESTAMP WITH TIME ZONE NOT NULL,

                         attributes_json JSONB NOT NULL DEFAULT '{}'::jsonb,

                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                         CONSTRAINT signals_signal_type_check CHECK (
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

                         CONSTRAINT signals_severity_check CHECK (
                             severity IN (
                                          'LOW',
                                          'MEDIUM',
                                          'HIGH',
                                          'CRITICAL'
                                 )
                             )
);

CREATE INDEX idx_signals_service_environment
    ON signals(service_name, environment);

CREATE INDEX idx_signals_type
    ON signals(signal_type);

CREATE INDEX idx_signals_severity
    ON signals(severity);

CREATE INDEX idx_signals_observed_at
    ON signals(observed_at);

CREATE INDEX idx_signals_correlation_id
    ON signals(correlation_id);
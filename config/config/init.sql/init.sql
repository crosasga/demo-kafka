CREATE TABLE IF NOT EXISTS kafka_message (
    id VARCHAR(255) NOT NULL,
    uuid_kafka UUID NOT NULL,
    message TEXT ,
    timestamp TIMESTAMPTZ DEFAULT NOW(),-- Fecha y hora del mensaje
    metadata JSONB,
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    error TEXT
);
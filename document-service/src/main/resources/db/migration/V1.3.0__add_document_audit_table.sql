CREATE TABLE document_audits (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    action_type VARCHAR(32) NOT NULL CHECK (action_type IN ('UPLOAD', 'DOWNLOAD')),
    auth_type VARCHAR(32) NOT NULL CHECK (auth_type IN ('SECURE', 'LINK_OTP')),
    occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_audits_document
        FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_document_audits_document_id ON document_audits(document_id);
CREATE INDEX idx_document_audits_user_name ON document_audits(user_name);
CREATE INDEX idx_document_audits_occurred_at ON document_audits(occurred_at);

CREATE TABLE applications (
                              id SERIAL PRIMARY KEY,
                              package_name VARCHAR(255) UNIQUE NOT NULL,
                              app_name VARCHAR(255) NOT NULL,
                              version_code INT,
                              risk_level VARCHAR(50),
                              last_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
                             id SERIAL PRIMARY KEY,
                             application_id INT NOT NULL,
                             permission_name VARCHAR(255) NOT NULL,
                             is_suspicious BOOLEAN DEFAULT FALSE,
                             FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE TABLE risk_predictions (
                                  id SERIAL PRIMARY KEY,
                                  application_id INT NOT NULL,
                                  risk_score FLOAT,
                                  risk_label VARCHAR(50),
                                  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

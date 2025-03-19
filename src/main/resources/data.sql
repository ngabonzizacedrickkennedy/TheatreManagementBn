-- Insert Admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (username, email, password, first_name, last_name, phone_number, role)
VALUES ('admin', 'admin@theatresystem.com', '$2a$10$X2gBqJai2hXeEFwI877nIO1Ivkugef2QK8JvWAA3fCrDJan7WZuvC', 'System', 'Administrator', '123-456-7890', 'ROLE_ADMIN') ON CONFLICT (username) DO NOTHING;
-- Insert Manager user
-- Password: manager123 (BCrypt encoded)
-- INSERT INTO users (username, email, password, first_name, last_name, phone_number, role)
-- VALUES ('manager', 'manager@theatresystem.com', '$2a$10$dR0qs1.LqQs7mNdDuVMx3.fz2nGGEVA2zLnsHlTcItQqnJz/u.YNG', 'Theatre', 'Manager', '123-456-7891', 'ROLE_MANAGER');
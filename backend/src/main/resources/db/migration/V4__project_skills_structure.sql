CREATE TABLE IF NOT EXISTS project_skills (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    skill_name VARCHAR(120) NOT NULL,
    position_index INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_project_skills_project_id
    ON project_skills (project_id);

CREATE INDEX IF NOT EXISTS idx_project_skills_skill_name_lower
    ON project_skills (LOWER(skill_name));

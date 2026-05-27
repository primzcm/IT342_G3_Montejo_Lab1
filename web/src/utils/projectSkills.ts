import type { ProjectSummary } from "../types";

export function parseSkills(rawValue: string): string[] {
  return rawValue
    .split(",")
    .map((skill) => skill.trim())
    .filter(Boolean);
}

export function matchesProjectFilters(
  project: ProjectSummary,
  selectedCategory: string,
  skillQuery: string
): boolean {
  const categoryMatches =
    selectedCategory === "All" || project.category === selectedCategory;
  const normalizedSkillQuery = skillQuery.trim().toLowerCase();
  const searchableContent = [
    project.title,
    project.category,
    project.rolesNeeded,
    project.description,
    project.ownerName,
    ...project.requiredSkills
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
  const skillMatches =
    !normalizedSkillQuery || searchableContent.includes(normalizedSkillQuery);

  return categoryMatches && skillMatches;
}

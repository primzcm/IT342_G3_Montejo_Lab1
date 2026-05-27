import { describe, expect, it } from "vitest";
import { matchesProjectFilters, parseSkills } from "./projectSkills";

describe("projectSkills", () => {
  it("parses comma-separated skills into a trimmed unique-ready list", () => {
    expect(parseSkills(" React, Kotlin ,  Supabase ,, ")).toEqual([
      "React",
      "Kotlin",
      "Supabase"
    ]);
  });

  it("matches against structured required skills as part of dashboard filtering", () => {
    const project = {
      id: 1,
      title: "Campus Event App",
      description: "Volunteer scheduling and announcements",
      category: "Community",
      rolesNeeded: "Frontend, Backend",
      requiredSkills: ["Figma", "Kotlin", "Firebase"],
      status: "OPEN",
      createdAt: "2026-05-26T00:00:00Z",
      ownerId: 7,
      ownerName: "Alex Rivera",
      owner: false,
      joined: false,
      joinRequested: false
    };

    expect(matchesProjectFilters(project, "All", "firebase")).toBe(true);
    expect(matchesProjectFilters(project, "Community", "figma")).toBe(true);
    expect(matchesProjectFilters(project, "FinTech", "figma")).toBe(false);
  });
});

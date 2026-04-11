package edu.cit.montejo.collabmatch.controller;

import edu.cit.montejo.collabmatch.dto.ApiResponse;
import edu.cit.montejo.collabmatch.dto.CreateJoinRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectRequest;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.dto.ProjectResponse;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listProjects(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.listProjects(currentUser)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            HttpServletRequest request,
            @Valid @RequestBody CreateProjectRequest createProjectRequest
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.createProject(currentUser, createProjectRequest)));
    }

    @PostMapping("/{projectId}/requests")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> requestToJoin(
            HttpServletRequest request,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateJoinRequest createJoinRequest
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.requestToJoin(currentUser, projectId, createJoinRequest)));
    }
}

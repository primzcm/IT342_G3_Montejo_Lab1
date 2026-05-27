package edu.cit.montejo.collabmatch.controller;

import edu.cit.montejo.collabmatch.dto.ApiResponse;
import edu.cit.montejo.collabmatch.dto.CreateJoinRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectMessageRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectRequest;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.dto.ProjectDetailResponse;
import edu.cit.montejo.collabmatch.dto.ProjectMessageResponse;
import edu.cit.montejo.collabmatch.dto.ProjectResponse;
import edu.cit.montejo.collabmatch.dto.UpdateProjectRequest;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProject(
            HttpServletRequest request,
            @PathVariable Long projectId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(currentUser, projectId)));
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

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            HttpServletRequest request,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest updateProjectRequest
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.updateProject(currentUser, projectId, updateProjectRequest)));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            HttpServletRequest request,
            @PathVariable Long projectId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        projectService.deleteProject(currentUser, projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
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

    @GetMapping("/{projectId}/requests")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> listProjectRequests(
            HttpServletRequest request,
            @PathVariable Long projectId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.listProjectRequests(currentUser, projectId)));
    }

    @GetMapping("/{projectId}/messages")
    public ResponseEntity<ApiResponse<List<ProjectMessageResponse>>> listProjectMessages(
            HttpServletRequest request,
            @PathVariable Long projectId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.listProjectMessages(currentUser, projectId)));
    }

    @PostMapping("/{projectId}/messages")
    public ResponseEntity<ApiResponse<ProjectMessageResponse>> createProjectMessage(
            HttpServletRequest request,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectMessageRequest createProjectMessageRequest
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.createProjectMessage(currentUser, projectId, createProjectMessageRequest)));
    }
}

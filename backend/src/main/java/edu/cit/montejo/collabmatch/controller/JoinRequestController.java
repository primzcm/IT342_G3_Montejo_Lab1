package edu.cit.montejo.collabmatch.controller;

import edu.cit.montejo.collabmatch.dto.ApiResponse;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/requests")
public class JoinRequestController {
    private final ProjectService projectService;

    public JoinRequestController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> approve(
            HttpServletRequest request,
            @PathVariable Long requestId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.approveJoinRequest(currentUser, requestId)));
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> reject(
            HttpServletRequest request,
            @PathVariable Long requestId
    ) {
        User currentUser = (User) request.getAttribute("currentUser");
        return ResponseEntity.ok(ApiResponse.success(projectService.rejectJoinRequest(currentUser, requestId)));
    }
}

package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.CreateJoinRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectRequest;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.dto.ProjectResponse;
import edu.cit.montejo.collabmatch.exception.ConflictException;
import edu.cit.montejo.collabmatch.model.JoinRequest;
import edu.cit.montejo.collabmatch.model.Project;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.repository.JoinRequestRepository;
import edu.cit.montejo.collabmatch.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_PENDING = "PENDING";

    private final ProjectRepository projectRepository;
    private final JoinRequestRepository joinRequestRepository;

    public ProjectService(ProjectRepository projectRepository, JoinRequestRepository joinRequestRepository) {
        this.projectRepository = projectRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(User currentUser) {
        return projectRepository.findAllForExplorer().stream()
                .map(project -> toProjectResponse(project, currentUser))
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(User currentUser, CreateProjectRequest request) {
        Project project = new Project(
                currentUser,
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                request.getRolesNeeded().trim(),
                STATUS_OPEN
        );
        projectRepository.save(project);
        return toProjectResponse(project, currentUser);
    }

    @Transactional
    public JoinRequestResponse requestToJoin(User currentUser, Long projectId, CreateJoinRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (project.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot join your own project");
        }

        if (!STATUS_OPEN.equals(project.getStatus())) {
            throw new IllegalArgumentException("Project is closed");
        }

        if (joinRequestRepository.existsByProjectIdAndRequesterId(project.getId(), currentUser.getId())) {
            throw new ConflictException("You already applied to this project");
        }

        JoinRequest joinRequest = new JoinRequest(
                project,
                currentUser,
                STATUS_PENDING,
                request.getMessage() == null ? null : request.getMessage().trim()
        );
        joinRequestRepository.save(joinRequest);
        return new JoinRequestResponse(
                joinRequest.getId(),
                project.getId(),
                currentUser.getId(),
                joinRequest.getStatus(),
                joinRequest.getMessage(),
                joinRequest.getCreatedAt()
        );
    }

    private ProjectResponse toProjectResponse(Project project, User currentUser) {
        boolean isOwner = currentUser != null && project.getOwner().getId().equals(currentUser.getId());
        boolean joinRequested = currentUser != null && !isOwner
                && joinRequestRepository.existsByProjectIdAndRequesterId(project.getId(), currentUser.getId());

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getRolesNeeded(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getOwner().getId(),
                project.getOwner().getFirstname() + " " + project.getOwner().getLastname(),
                isOwner,
                joinRequested
        );
    }
}

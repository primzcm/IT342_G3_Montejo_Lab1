package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.CreateJoinRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectMessageRequest;
import edu.cit.montejo.collabmatch.dto.CreateProjectRequest;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.dto.ProjectDetailResponse;
import edu.cit.montejo.collabmatch.dto.ProjectMemberResponse;
import edu.cit.montejo.collabmatch.dto.ProjectMessageResponse;
import edu.cit.montejo.collabmatch.dto.ProjectResponse;
import edu.cit.montejo.collabmatch.dto.UpdateProjectRequest;
import edu.cit.montejo.collabmatch.exception.ConflictException;
import edu.cit.montejo.collabmatch.exception.ForbiddenException;
import edu.cit.montejo.collabmatch.exception.NotFoundException;
import edu.cit.montejo.collabmatch.model.JoinRequest;
import edu.cit.montejo.collabmatch.model.Project;
import edu.cit.montejo.collabmatch.model.ProjectMember;
import edu.cit.montejo.collabmatch.model.ProjectMessage;
import edu.cit.montejo.collabmatch.model.ProjectSkill;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.repository.JoinRequestRepository;
import edu.cit.montejo.collabmatch.repository.ProjectMemberRepository;
import edu.cit.montejo.collabmatch.repository.ProjectMessageRepository;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMessageRepository projectMessageRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            JoinRequestRepository joinRequestRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMessageRepository projectMessageRepository
    ) {
        this.projectRepository = projectRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMessageRepository = projectMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(User currentUser) {
        return projectRepository.findAllForExplorer().stream()
                .map(project -> toProjectResponse(project, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProject(User currentUser, Long projectId) {
        Project project = getProjectOrThrow(projectId);
        List<ProjectMemberResponse> members = projectMemberRepository.findAllByProjectIdWithUserOrderByJoinedAtAsc(projectId).stream()
                .map(member -> new ProjectMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getFirstname() + " " + member.getUser().getLastname(),
                        member.getJoinedAt()
                ))
                .toList();

        return toProjectDetailResponse(project, currentUser, members);
    }

    @Transactional
    public ProjectResponse createProject(User currentUser, CreateProjectRequest request) {
        List<String> requiredSkills = normalizeSkills(request.getRequiredSkills());
        Project project = new Project(
                currentUser,
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                String.join(", ", requiredSkills),
                STATUS_OPEN
        );
        project.replaceRequiredSkills(requiredSkills);
        projectRepository.save(project);
        return toProjectResponse(project, currentUser);
    }

    @Transactional
    public ProjectResponse updateProject(User currentUser, Long projectId, UpdateProjectRequest request) {
        Project project = getOwnedProjectOrThrow(currentUser, projectId);
        List<String> requiredSkills = normalizeSkills(request.getRequiredSkills());
        project.updateDetails(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                String.join(", ", requiredSkills),
                request.getStatus().trim()
        );
        project.replaceRequiredSkills(requiredSkills);
        return toProjectResponse(project, currentUser);
    }

    @Transactional
    public void deleteProject(User currentUser, Long projectId) {
        Project project = getOwnedProjectOrThrow(currentUser, projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public JoinRequestResponse requestToJoin(User currentUser, Long projectId, CreateJoinRequest request) {
        Project project = getProjectOrThrow(projectId);

        if (project.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot join your own project");
        }

        if (!STATUS_OPEN.equals(project.getStatus())) {
            throw new IllegalArgumentException("Project is closed");
        }

        if (joinRequestRepository.existsByProjectIdAndRequesterId(project.getId(), currentUser.getId())) {
            throw new ConflictException("You already applied to this project");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUser.getId())) {
            throw new ConflictException("You are already a member of this project");
        }

        JoinRequest joinRequest = new JoinRequest(
                project,
                currentUser,
                STATUS_PENDING,
                request.getMessage() == null ? null : request.getMessage().trim()
        );
        joinRequestRepository.save(joinRequest);
        return toJoinRequestResponse(joinRequest);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> listProjectRequests(User currentUser, Long projectId) {
        Project project = getOwnedProjectOrThrow(currentUser, projectId);
        return joinRequestRepository.findAllByProjectIdWithRequesterOrderByCreatedAtDesc(project.getId()).stream()
                .map(this::toJoinRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectMessageResponse> listProjectMessages(User currentUser, Long projectId) {
        Project project = getProjectOrThrow(projectId);
        ensureProjectBoardAccess(currentUser, project);
        return projectMessageRepository.findAllByProjectIdWithAuthorOrderByCreatedAtAsc(projectId).stream()
                .map(this::toProjectMessageResponse)
                .toList();
    }

    @Transactional
    public ProjectMessageResponse createProjectMessage(User currentUser, Long projectId, CreateProjectMessageRequest request) {
        Project project = getProjectOrThrow(projectId);
        ensureProjectBoardAccess(currentUser, project);
        ProjectMessage message = new ProjectMessage(project, currentUser, request.getContent().trim());
        projectMessageRepository.save(message);
        return toProjectMessageResponse(message);
    }

    @Transactional
    public JoinRequestResponse approveJoinRequest(User currentUser, Long requestId) {
        JoinRequest joinRequest = getOwnedJoinRequestOrThrow(currentUser, requestId);
        ensurePending(joinRequest);

        if (!projectMemberRepository.existsByProjectIdAndUserId(joinRequest.getProject().getId(), joinRequest.getRequester().getId())) {
            projectMemberRepository.save(new ProjectMember(joinRequest.getProject(), joinRequest.getRequester()));
        }

        joinRequest.approve();
        return toJoinRequestResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse rejectJoinRequest(User currentUser, Long requestId) {
        JoinRequest joinRequest = getOwnedJoinRequestOrThrow(currentUser, requestId);
        ensurePending(joinRequest);
        joinRequest.reject();
        return toJoinRequestResponse(joinRequest);
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findByIdWithOwner(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private Project getOwnedProjectOrThrow(User currentUser, Long projectId) {
        Project project = getProjectOrThrow(projectId);
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
        return project;
    }

    private JoinRequest getOwnedJoinRequestOrThrow(User currentUser, Long requestId) {
        JoinRequest joinRequest = joinRequestRepository.findByIdWithProjectAndRequester(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));
        if (!joinRequest.getProject().getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
        return joinRequest;
    }

    private void ensurePending(JoinRequest joinRequest) {
        if (!STATUS_PENDING.equals(joinRequest.getStatus())) {
            throw new ConflictException("Join request has already been processed");
        }
    }

    private void ensureProjectBoardAccess(User currentUser, Project project) {
        if (currentUser == null) {
            throw new ForbiddenException("Only project members can access the project board");
        }

        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUser.getId());
        if (!isOwner && !isMember) {
            throw new ForbiddenException("Only the project owner and approved members can access the project board");
        }
    }

    private ProjectDetailResponse toProjectDetailResponse(Project project, User currentUser, List<ProjectMemberResponse> members) {
        boolean isOwner = currentUser != null && project.getOwner().getId().equals(currentUser.getId());
        boolean joined = currentUser != null && !isOwner
                && projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUser.getId());
        boolean joinRequested = currentUser != null && !isOwner && !joined
                && joinRequestRepository.existsByProjectIdAndRequesterIdAndStatus(project.getId(), currentUser.getId(), STATUS_PENDING);
        List<String> requiredSkills = extractRequiredSkills(project);

        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getRolesNeeded(),
                requiredSkills,
                project.getStatus(),
                project.getCreatedAt(),
                project.getOwner().getId(),
                project.getOwner().getFirstname() + " " + project.getOwner().getLastname(),
                isOwner,
                joined,
                joinRequested,
                members
        );
    }

    private JoinRequestResponse toJoinRequestResponse(JoinRequest joinRequest) {
        return new JoinRequestResponse(
                joinRequest.getId(),
                joinRequest.getProject().getId(),
                joinRequest.getRequester().getId(),
                joinRequest.getRequester().getFirstname() + " " + joinRequest.getRequester().getLastname(),
                joinRequest.getStatus(),
                joinRequest.getMessage(),
                joinRequest.getCreatedAt(),
                joinRequest.getReviewedAt()
        );
    }

    private ProjectMessageResponse toProjectMessageResponse(ProjectMessage projectMessage) {
        return new ProjectMessageResponse(
                projectMessage.getId(),
                projectMessage.getProject().getId(),
                projectMessage.getAuthor().getId(),
                projectMessage.getAuthor().getFirstname() + " " + projectMessage.getAuthor().getLastname(),
                projectMessage.getContent(),
                projectMessage.getCreatedAt()
        );
    }

    private ProjectResponse toProjectResponse(Project project, User currentUser) {
        boolean isOwner = currentUser != null && project.getOwner().getId().equals(currentUser.getId());
        boolean joined = currentUser != null && !isOwner
                && projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUser.getId());
        boolean joinRequested = currentUser != null && !isOwner && !joined
                && joinRequestRepository.existsByProjectIdAndRequesterIdAndStatus(project.getId(), currentUser.getId(), STATUS_PENDING);
        List<String> requiredSkills = extractRequiredSkills(project);

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getRolesNeeded(),
                requiredSkills,
                project.getStatus(),
                project.getCreatedAt(),
                project.getOwner().getId(),
                project.getOwner().getFirstname() + " " + project.getOwner().getLastname(),
                isOwner,
                joined,
                joinRequested
        );
    }

    private List<String> extractRequiredSkills(Project project) {
        return project.getRequiredSkills().stream()
                .map(ProjectSkill::getSkillName)
                .toList();
    }

    private List<String> normalizeSkills(List<String> skills) {
        return skills.stream()
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .toList();
    }
}

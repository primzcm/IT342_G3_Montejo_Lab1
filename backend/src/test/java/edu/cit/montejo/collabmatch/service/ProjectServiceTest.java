package edu.cit.montejo.collabmatch.service;

import edu.cit.montejo.collabmatch.dto.CreateJoinRequest;
import edu.cit.montejo.collabmatch.dto.JoinRequestResponse;
import edu.cit.montejo.collabmatch.dto.ProjectDetailResponse;
import edu.cit.montejo.collabmatch.dto.UpdateProjectRequest;
import edu.cit.montejo.collabmatch.exception.ConflictException;
import edu.cit.montejo.collabmatch.exception.ForbiddenException;
import edu.cit.montejo.collabmatch.model.JoinRequest;
import edu.cit.montejo.collabmatch.model.Project;
import edu.cit.montejo.collabmatch.model.ProjectMember;
import edu.cit.montejo.collabmatch.model.User;
import edu.cit.montejo.collabmatch.repository.JoinRequestRepository;
import edu.cit.montejo.collabmatch.repository.ProjectMemberRepository;
import edu.cit.montejo.collabmatch.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private User requester;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@example.com", "Owner", "User", "USER", "hash", Instant.now());
        requester = new User("requester", "requester@example.com", "Requester", "User", "USER", "hash", Instant.now());
        ReflectionTestUtils.setField(owner, "id", 1L);
        ReflectionTestUtils.setField(requester, "id", 2L);

        project = new Project(owner, "Build App", "Collaborative platform", "Web", "Backend, UI", "OPEN");
        ReflectionTestUtils.setField(project, "id", 10L);
        ReflectionTestUtils.setField(project, "createdAt", Instant.now());
    }

    @Test
    void getProjectIncludesMembers() {
        ProjectMember member = new ProjectMember(project, requester);
        ReflectionTestUtils.setField(member, "id", 100L);
        ReflectionTestUtils.setField(member, "joinedAt", Instant.now());

        when(projectRepository.findByIdWithOwner(10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findAllByProjectIdWithUserOrderByJoinedAtAsc(10L)).thenReturn(List.of(member));
        when(joinRequestRepository.existsByProjectIdAndRequesterId(10L, 2L)).thenReturn(false);

        ProjectDetailResponse response = projectService.getProject(requester, 10L);

        assertEquals(10L, response.getId());
        assertEquals(1, response.getMembers().size());
        assertEquals("Requester User", response.getMembers().get(0).getName());
        assertFalse(response.isOwner());
    }

    @Test
    void updateProjectRejectsNonOwner() {
        when(projectRepository.findByIdWithOwner(10L)).thenReturn(Optional.of(project));

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("Updated");
        request.setDescription("Updated description");
        request.setCategory("AI");
        request.setRolesNeeded("Mobile");
        request.setStatus("CLOSED");

        assertThrows(ForbiddenException.class, () -> projectService.updateProject(requester, 10L, request));
    }

    @Test
    void approveJoinRequestCreatesMemberAndMarksApproved() {
        JoinRequest joinRequest = new JoinRequest(project, requester, "PENDING", "Please let me join");
        ReflectionTestUtils.setField(joinRequest, "id", 50L);
        ReflectionTestUtils.setField(joinRequest, "createdAt", Instant.now());

        when(joinRequestRepository.findByIdWithProjectAndRequester(50L)).thenReturn(Optional.of(joinRequest));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);

        JoinRequestResponse response = projectService.approveJoinRequest(owner, 50L);

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());
        assertEquals(project, memberCaptor.getValue().getProject());
        assertEquals(requester, memberCaptor.getValue().getUser());
        assertEquals("APPROVED", response.getStatus());
        assertNotNull(response.getReviewedAt());
    }

    @Test
    void rejectJoinRequestRejectsProcessedRequest() {
        JoinRequest joinRequest = new JoinRequest(project, requester, "APPROVED", "Already done");
        ReflectionTestUtils.setField(joinRequest, "id", 51L);
        ReflectionTestUtils.setField(joinRequest, "createdAt", Instant.now());

        when(joinRequestRepository.findByIdWithProjectAndRequester(51L)).thenReturn(Optional.of(joinRequest));

        assertThrows(ConflictException.class, () -> projectService.rejectJoinRequest(owner, 51L));
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void requestToJoinRejectsExistingMembers() {
        when(projectRepository.findByIdWithOwner(10L)).thenReturn(Optional.of(project));
        when(joinRequestRepository.existsByProjectIdAndRequesterId(10L, 2L)).thenReturn(false);
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

        CreateJoinRequest request = new CreateJoinRequest();
        request.setMessage("I can help");

        assertThrows(ConflictException.class, () -> projectService.requestToJoin(requester, 10L, request));
    }
}

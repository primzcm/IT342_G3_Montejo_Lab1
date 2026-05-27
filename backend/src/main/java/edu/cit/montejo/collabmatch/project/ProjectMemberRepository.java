package edu.cit.montejo.collabmatch.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Query("select pm from ProjectMember pm join fetch pm.user where pm.project.id = :projectId order by pm.joinedAt asc")
    List<ProjectMember> findAllByProjectIdWithUserOrderByJoinedAtAsc(@Param("projectId") Long projectId);
}

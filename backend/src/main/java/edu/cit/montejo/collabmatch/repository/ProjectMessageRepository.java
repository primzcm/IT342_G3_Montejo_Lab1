package edu.cit.montejo.collabmatch.repository;

import edu.cit.montejo.collabmatch.model.ProjectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {
    @Query("select pm from ProjectMessage pm join fetch pm.author where pm.project.id = :projectId order by pm.createdAt asc")
    List<ProjectMessage> findAllByProjectIdWithAuthorOrderByCreatedAtAsc(@Param("projectId") Long projectId);
}

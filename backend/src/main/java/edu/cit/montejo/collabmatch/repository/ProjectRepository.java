package edu.cit.montejo.collabmatch.repository;

import edu.cit.montejo.collabmatch.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("select p from Project p join fetch p.owner order by p.createdAt desc")
    List<Project> findAllForExplorer();

    @Query("select p from Project p join fetch p.owner where p.id = :projectId")
    Optional<Project> findByIdWithOwner(@Param("projectId") Long projectId);
}

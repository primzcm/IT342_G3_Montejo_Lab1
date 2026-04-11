package edu.cit.montejo.collabmatch.repository;

import edu.cit.montejo.collabmatch.model.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    boolean existsByProjectIdAndRequesterId(Long projectId, Long requesterId);
}

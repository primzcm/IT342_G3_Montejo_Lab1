package edu.cit.montejo.collabmatch.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    boolean existsByProjectIdAndRequesterId(Long projectId, Long requesterId);

    boolean existsByProjectIdAndRequesterIdAndStatus(Long projectId, Long requesterId, String status);

    @Query("select jr from JoinRequest jr join fetch jr.project p join fetch p.owner join fetch jr.requester where jr.id = :requestId")
    Optional<JoinRequest> findByIdWithProjectAndRequester(@Param("requestId") Long requestId);

    @Query("select jr from JoinRequest jr join fetch jr.requester where jr.project.id = :projectId order by jr.createdAt desc")
    List<JoinRequest> findAllByProjectIdWithRequesterOrderByCreatedAtDesc(@Param("projectId") Long projectId);
}

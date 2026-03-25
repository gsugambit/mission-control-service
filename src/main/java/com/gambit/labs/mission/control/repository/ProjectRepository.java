package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.ProjectDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectDao, UUID> {
    List<ProjectDao> findAllByAssignedUserId(UUID assignedUserId);
}

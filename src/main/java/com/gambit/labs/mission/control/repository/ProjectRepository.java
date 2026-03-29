package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.ProjectDao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectDao, UUID> {

  List<ProjectDao> findAllByAssignedUserId(UUID assignedUserId);

  boolean existsByName(String name);
}

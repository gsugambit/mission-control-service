package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.TaskDao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskDao, UUID> {

  boolean existsByProjectId(UUID id);

  List<TaskDao> findAllByAssignedUserId(UUID assignedUserId);
}

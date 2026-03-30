package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.TaskCommentDao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskCommentDao, UUID> {

  List<TaskCommentDao> findAllByTaskIdOrderByDateCreatedAsc(final UUID taskId);
}

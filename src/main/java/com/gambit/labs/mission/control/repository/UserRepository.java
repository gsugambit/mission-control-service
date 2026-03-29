package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.UserDao;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDao, UUID> {

  boolean existsByUserName(String userName);
}

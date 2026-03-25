package com.gambit.labs.mission.control.repository;

import com.gambit.labs.mission.control.dao.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserDao, UUID> {
}

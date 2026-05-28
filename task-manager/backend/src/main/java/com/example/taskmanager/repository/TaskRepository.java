package com.example.taskmanager.repository;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>
{
        List<Task> findByOwner(AppUser owner);

        Optional<Task> findByIdAndOwner(Long id, AppUser owner);
}

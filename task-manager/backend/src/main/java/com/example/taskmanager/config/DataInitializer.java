package com.example.taskmanager.config;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.domain.Role;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default user and a couple of sample tasks on startup so the
 * application is usable immediately after launch.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TaskRepository taskRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
            log.info("Seeded default user 'admin' (password: 'admin123')");
        }

        if (taskRepository.count() == 0) {
            Task first = new Task();
            first.setTitle("Read the README");
            first.setDescription("Get the project up and running");
            first.setStatus(TaskStatus.TODO);

            Task second = new Task();
            second.setTitle("Record the demo video");
            second.setDescription("2-3 minutes showing the app in action");
            second.setStatus(TaskStatus.IN_PROGRESS);

            taskRepository.save(first);
            taskRepository.save(second);
            log.info("Seeded {} sample tasks", taskRepository.count());
        }
    }
}

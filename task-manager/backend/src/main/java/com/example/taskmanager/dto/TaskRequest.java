package com.example.taskmanager.dto;

import com.example.taskmanager.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(

                @NotBlank(message = "Title must not be blank")
                @Size(max = 200, message = "Title must be at most 200 characters")
                String title,

                @Size(max = 1000, message = "Description must be at most 1000 characters")
                String description,

                TaskStatus status)
{
}

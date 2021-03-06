package pl.edu.pw.ee.pyskp.documentworkflow.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.task.NewTaskForm;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.ResourceNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.services.TaskService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

/**
 * Created by p.pysk on 02.01.2017.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TasksController {
    @NonNull
    private final TaskService taskService;

    @GetMapping("/exists")
    @PreAuthorize("@securityService.hasAccessToProject(#projectId)")
    public boolean existsByName(@PathVariable ObjectId projectId, @RequestParam String taskName) {
        return taskService.existsByName(projectId, taskName);
    }

    @PostMapping
    @PreAuthorize("@securityService.canAddTask(#projectId)")
    public Map<String, String> processNewTaskForm(@PathVariable ObjectId projectId,
                                                  @RequestBody @Valid NewTaskForm newTask)
            throws ResourceNotFoundException {
        ObjectId taskId = taskService.createTaskFromForm(newTask, projectId);
        return Collections.singletonMap("taskId", taskId.toString());
    }
}

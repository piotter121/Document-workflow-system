package pl.edu.pw.ee.pyskp.documentworkflow.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.User;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.NewProjectForm;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.ProjectInfoDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.ProjectSummaryDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.services.ProjectService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Created by piotr on 16.12.16.
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@RequestMapping("/api/projects")
public class ProjectsController {
    private static final Logger logger = Logger.getLogger(ProjectsController.class);

    @NonNull
    private final ProjectService projectService;

    @NonNull
    private final UserService userService;

    @GetMapping
    public List<ProjectSummaryDTO> getUserProjects() {
        User currentUser = userService.getCurrentUser();
        return projectService.getUserParticipatedProjects(currentUser.getEmail());
    }

    @PostMapping
    public String processCreationOfNewProject(@RequestBody @Valid NewProjectForm newProject) {
        UUID createdProjectId = projectService.createNewProjectFromForm(newProject);
        return createdProjectId.toString();
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("@securityService.hasAccessToProject(#projectId)")
    public ProjectInfoDTO getProjectInfo(@PathVariable UUID projectId) {
        return projectService.getProjectInfo(projectId);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityService.isCurrentUserProjectAdministrator(#projectId)")
    public void deleteProject(@PathVariable UUID projectId) {
        logger.debug("Received HTTP DELETE request for deletion project " + projectId);
        projectService.deleteProject(projectId);
    }
}
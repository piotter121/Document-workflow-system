package pl.edu.pw.ee.pyskp.documentworkflow.controllers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.NewFileForm;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.ResourceNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.UnknownContentType;
import pl.edu.pw.ee.pyskp.documentworkflow.services.FilesMetadataService;

import java.util.UUID;

/**
 * Created by piotr on 04.01.17.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/files")
public class FilesMetadataController {
    @NonNull
    private final FilesMetadataService filesMetadataService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("@securityService.isTaskParticipant(#projectId, #taskId)")
    public String processNewFileForm(@RequestPart(name = "name") String name,
                                     @RequestPart(name = "description", required = false) String description,
                                     @RequestPart(name = "file") MultipartFile file,
                                     @RequestPart(name = "versionString") String versionString,
                                     @PathVariable UUID taskId,
                                     @PathVariable UUID projectId)
            throws UnknownContentType, ResourceNotFoundException {
        NewFileForm newFileForm = new NewFileForm(name, description, file, versionString);
        UUID fileId = filesMetadataService.createNewFileFromForm(newFileForm, projectId, taskId);
        return fileId.toString();
    }
}
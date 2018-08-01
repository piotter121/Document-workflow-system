package pl.edu.pw.ee.pyskp.documentworkflow.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.*;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.FileMetadataRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.TaskRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.UserProjectRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.VersionRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.FileMetadataDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.NewFileForm;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.FileNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.ProjectNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.TaskNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.UnknownContentType;
import pl.edu.pw.ee.pyskp.documentworkflow.services.FilesMetadataService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.TaskService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.UserService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.VersionService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by piotr on 06.01.17.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class FilesMetadataServiceImpl implements FilesMetadataService {
    private final static Logger logger = Logger.getLogger(FilesMetadataServiceImpl.class);

    @NonNull
    private final FileMetadataRepository fileMetadataRepository;

    @NonNull
    private final VersionService versionService;

    @NonNull
    private final VersionRepository versionRepository;

    @NonNull
    private final TaskRepository taskRepository;

    @NonNull
    private final UserProjectRepository userProjectRepository;

    @NonNull
    private final UserService userService;

    @NonNull
    private final TaskService taskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UUID createNewFileFromForm(NewFileForm formData, final UUID projectId, final UUID taskID)
            throws UnknownContentType, IOException, TaskNotFoundException {
        Task task = taskRepository.findTaskByProjectIdAndTaskId(projectId, taskID)
                .orElseThrow(() -> new TaskNotFoundException(taskID));
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setName(formData.getName());
        fileMetadata.setDescription(formData.getDescription());
        fileMetadata.setConfirmed(false);
        fileMetadata.setMarkedToConfirm(false);
        fileMetadata.setTaskId(taskID);
        fileMetadata.setContentType(getContentType(formData.getFile()));
        fileMetadata.setTaskName(task.getName());
        Version initVersion = versionService.createUnmanagedInitVersionOfFile(formData);
        fileMetadata.setLatestVersion(new VersionSummary(initVersion));
        fileMetadata = fileMetadataRepository.save(fileMetadata);
        UUID fileId = fileMetadata.getFileId();

        initVersion.setFileId(fileId);
        versionRepository.save(initVersion);

        task.setLastModifiedFile(new FileSummary(fileMetadata));
        task.incrementNumberOfFiles();
        task = taskRepository.save(task);

        String currentUserEmail = userService.getCurrentUserEmail();
        UserProject userProject =
                userProjectRepository.findUserProjectByUserEmailAndProjectId(currentUserEmail, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        userProject.setLastModifiedFile(task.getLastModifiedFile());
        userProject.incrementNumberOfFiles();
        userProjectRepository.save(userProject);

        return fileId;
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataDTO getFileMetadataDTO(UUID taskId, UUID fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        List<Version> versions = versionRepository.findAllByFileId(fileId);
        return new FileMetadataDTO(fileMetadata, versions);
    }

    @Override
    @Transactional
    public void markFileToConfirm(UUID taskId, UUID fileId) {
        FileMetadata file = fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        file.setMarkedToConfirm(true);
        fileMetadataRepository.save(file);
    }

    @Override
    public boolean hasContentTypeAs(UUID taskId, UUID fileId, MultipartFile file) {
        FileMetadata fileMetadata = fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        ContentType contentType;
        try {
            contentType = getContentType(file);
        } catch (IOException | UnknownContentType e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return contentType.equals(fileMetadata.getContentType());
    }

    @Override
    public void confirmFile(UUID taskId, UUID fileId) {
        FileMetadata fileToConfirm = fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
        fileToConfirm.setConfirmed(true);
        fileMetadataRepository.save(fileToConfirm);
    }

    @Override
    public void deleteFile(UUID projectId, UUID taskId, UUID fileId) throws TaskNotFoundException {
        versionRepository.deleteAllByFileId(fileId);
        fileMetadataRepository.deleteFileMetadataByTaskIdAndFileId(taskId, fileId);
        taskService.updateTaskStatistic(projectId, taskId);
    }

    @Override
    public boolean isValidVersionStringForFile(String versionString, UUID fileId) {
        List<Version> versions = versionRepository.findAllByFileId(fileId);
        if (versions.isEmpty()) throw new FileNotFoundException(fileId);
        return versions.parallelStream()
                .map(Version::getVersionString)
                .noneMatch(string -> string.equalsIgnoreCase(versionString));
    }

    @Override
    public String getFileName(UUID taskId, UUID fileId) {
        return fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .map(metadata -> metadata.getName() + "." + metadata.getContentType().getExtension())
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    private static ContentType getContentType(MultipartFile multipartFile)
            throws UnknownContentType, IOException {
        Tika tika = new Tika();
        String contentType = tika.detect(multipartFile.getBytes());
        return ContentType.fromName(contentType)
                .orElseThrow(() -> new UnknownContentType(contentType));
    }
}

package pl.edu.pw.ee.pyskp.documentworkflow.services;

import org.springframework.validation.annotation.Validated;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.Project;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.Task;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.file.ContentTypeDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.file.FileMetadataDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.file.FileSummaryDTO;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.file.NewFileForm;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.FileNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.ResourceNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.UnsupportedContentType;

import javax.validation.Valid;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by p.pysk on 04.01.2017.
 */
@Validated
public interface FilesMetadataService {
    Long createNewFileFromForm(@Valid NewFileForm formData, Long taskId)
            throws UnsupportedContentType, ResourceNotFoundException;

    FileMetadataDTO getFileMetadataDTO(Long fileId) throws FileNotFoundException;

    void markFileToConfirm(Long fileId) throws FileNotFoundException;

    boolean hasContentTypeAs(Long fileId, InputStream inputStream) throws FileNotFoundException;

    void confirmFile(Long fileId) throws FileNotFoundException;

    void deleteFile(Long fileId);

    String getFileName(Long fileId) throws FileNotFoundException;

    int getNumberOfFiles(Task task);

    int getNumberOfFiles(Project project);

    Optional<FileSummaryDTO> getLastModifiedFileSummary(Task task);

    Optional<FileSummaryDTO> getLastModifiedFileSummary(Project project);

    ContentTypeDTO getContentType(Long fileId) throws FileNotFoundException;
}

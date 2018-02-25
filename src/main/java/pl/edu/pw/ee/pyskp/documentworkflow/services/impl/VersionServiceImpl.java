package pl.edu.pw.ee.pyskp.documentworkflow.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pw.ee.pyskp.documentworkflow.data.domain.*;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.FileMetadataRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.data.repository.VersionRepository;
import pl.edu.pw.ee.pyskp.documentworkflow.dtos.*;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.FileNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.exceptions.VersionNotFoundException;
import pl.edu.pw.ee.pyskp.documentworkflow.services.DifferenceService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.TaskService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.UserService;
import pl.edu.pw.ee.pyskp.documentworkflow.services.VersionService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by piotr on 06.01.17.
 */
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
    private static final Logger logger = Logger.getLogger(VersionServiceImpl.class);
    private static MessageDigest sha256 = initializeSHA256();

    private static MessageDigest initializeSHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private final UserService userService;

    @NonNull
    private final DifferenceService differenceService;

    @NonNull
    private final VersionRepository versionRepository;

    @NonNull
    private final FileMetadataRepository fileMetadataRepository;

    @NonNull
    private final TaskService taskService;

    @Override
    public Version createUnmanagedInitVersionOfFile(NewFileForm form) throws IOException {
        Version version = new Version();
        version.setVersionString(form.getVersionString());
        version.setMessage(form.getVersionMessage());
        version.setAuthor(new UserSummary(userService.getCurrentUser()));
        MultipartFile file = form.getFile();
        byte[] bytes = file.getBytes();
        version.setCheckSum(calculateCheckSum(bytes));
        version.setSaveDate(new Date());
        ByteBuffer content = ByteBuffer.wrap(bytes);
        version.setFileContent(content);
        Set<Difference> differences = differenceService.createDifferencesForNewFile(file.getInputStream());
        version.setDifferences(differences);
        return version;
    }

    @Override
    public long addNewVersionOfFile(NewVersionForm form) throws IOException {
        Version newVersion = new Version();
        MultipartFile file = form.getFile();
        newVersion.setSaveDate(new Date());
        newVersion.setAuthor(new UserSummary(userService.getCurrentUser()));
        newVersion.setVersionString(form.getVersionString());
        newVersion.setMessage(form.getMessage());
        newVersion.setCheckSum(calculateCheckSum(file.getBytes()));
        newVersion.setFileContent(ByteBuffer.wrap(file.getBytes()));
        FileMetadata fileMetadata = getFileMetadata(form.getTaskId(), form.getFileId());
        newVersion.setFileId(form.getFileId());
        byte[] oldContent = versionRepository.findTopByFileIdOrderBySaveDateDesc(form.getFileId())
                .map(version -> version.getFileContent().array())
                .orElseThrow(VersionNotFoundException::new);
        Set<Difference> differences = differenceService.getDifferencesBetweenTwoFiles(
                new ByteArrayInputStream(oldContent), file.getInputStream());
        newVersion.setDifferences(differences);
        long saveDateTime = versionRepository.save(newVersion).getSaveDate().getTime();
        fileMetadata.setLatestVersion(new VersionSummary(newVersion));
        fileMetadata.setNumberOfVersions(fileMetadata.getNumberOfVersions() + 1);
        fileMetadataRepository.save(fileMetadata);

        taskService.updateTaskStatistic(form.getProjectId(), form.getTaskId());

        return saveDateTime;
    }

    @Override
    public DiffData buildDiffData(UUID fileId, long versionSaveDateMillis) {
        List<Version> last2Versions = versionRepository
                .findTop2ByFileIdAndSaveDateLessThanEqualOrderBySaveDateDesc(fileId,
                        new Date(versionSaveDateMillis));
        if (last2Versions.isEmpty()) throw new VersionNotFoundException(versionSaveDateMillis);
        Version version = last2Versions.get(0);
        DiffData diffData = new DiffData();
        diffData.setDifferences(version.getDifferences());
        diffData.setNewContent(new FileContentDTO(version.getFileContent().array()));

        if (last2Versions.size() != 1) {
            diffData.setOldContent(new FileContentDTO(last2Versions.get(1).getFileContent().array()));
        }

        return diffData;
    }

    @Override
    public VersionInfoDTO getVersionInfo(UUID fileId, long versionSaveDateMillis) {
        List<Version> versions = versionRepository
                .findTop2ByFileIdAndSaveDateLessThanEqualOrderBySaveDateDesc(fileId,
                new Date(versionSaveDateMillis));
        if (versions.isEmpty()) throw new VersionNotFoundException(versionSaveDateMillis);
        VersionInfoDTO versionInfoDTO = new VersionInfoDTO(versions.get(0));
        if (versions.size() == 2)
            versionInfoDTO.setPreviousVersionString(versions.get(1).getVersionString());
        return versionInfoDTO;
    }

    private FileMetadata getFileMetadata(UUID taskId, UUID fileId) {
        return fileMetadataRepository.findOneByTaskIdAndFileId(taskId, fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    private static String calculateCheckSum(byte[] bytes) {
        if (sha256 == null)
            sha256 = initializeSHA256();
        return String.format("%064x", new BigInteger(sha256.digest(bytes)));
    }
}

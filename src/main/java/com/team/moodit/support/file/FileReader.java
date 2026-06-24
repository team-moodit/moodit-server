package com.team.moodit.support.file;

import com.team.moodit.storage.db.core.FileEntity;
import com.team.moodit.storage.db.core.FileRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileReader {
    private static final String FILE_HOST = "http://14.6.152.212:9000/moodit/";

    private final FileRepository fileRepository;

    public File getFile(Long fileId) {
        FileEntity entity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new File(
                entity.getId(),
                FILE_HOST + entity.getObjectKey(),
                entity.getResourceType()
        );
    }
}

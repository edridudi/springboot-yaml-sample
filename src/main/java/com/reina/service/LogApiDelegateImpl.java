package com.reina.service;

import com.reina.openapi.api.LogApiDelegate;
import com.reina.openapi.model.Log;
import com.reina.openapi.model.LogError;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class LogApiDelegateImpl implements LogApiDelegate {

    private static final String FILES_DIR = "./logs/";

    @Override
    public ResponseEntity<LogError> logPost(Log log) {
        checkDir();
        var file = new File(FILES_DIR+log.getLogFile());
        try {
            createIfNotExists(file);
            int logEntries = countLogEntries(file);
            if (logEntries<log.getLogLimit()) {
                FileUtils.writeStringToFile(file,"log\n",StandardCharsets.UTF_8,true);
            } else {
                var logError = new LogError();
                logError.setCode(HttpStatus.NO_CONTENT.value());
                logError.setMessage("Log file limit exceeded");
                return ResponseEntity.status(HttpStatus.OK).body(logError);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        var logError = new LogError();
        logError.setCode(HttpStatus.OK.value());
        logError.setMessage("Written successfully");
        return ResponseEntity.status(HttpStatus.OK).body(logError);
    }

    private int countLogEntries(File file) throws IOException {
        return FileUtils.readLines(file,StandardCharsets.UTF_8).size();
    }

    private void createIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    private void checkDir() {
        var dir = new File(FILES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}

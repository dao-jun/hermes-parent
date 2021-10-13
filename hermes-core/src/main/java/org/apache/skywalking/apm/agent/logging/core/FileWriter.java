/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.apache.skywalking.apm.agent.logging.core;

import org.apache.skywalking.apm.agent.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.config.Config;
import org.aries.middleware.hermes.common.consts.Constants;
import org.aries.middleware.hermes.common.exceptions.RunnableWithExceptionProtection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * The <code>FileWriter</code> support async file output, by using a queue as buffer.
 *
 * @author wusheng
 */
public class FileWriter implements IWriter {
    private static final Map<Object, FileWriter> map = new ConcurrentHashMap<>(2, 1.0F);
    private FileOutputStream fileOutputStream;
    private final ArrayBlockingQueue<String> logBuffer;
    private volatile int fileSize;
    private static final Object KEY = new Object();
    private final Pattern filenamePattern = Pattern.compile(Config.Logging.FILE_NAME + "\\.\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}");

    public static FileWriter get() {
        return map.computeIfAbsent(KEY, k -> new FileWriter());
    }

    private FileWriter() {
        logBuffer = new ArrayBlockingQueue<>(1024);
        final ArrayList<String> outputLogs = new ArrayList<>(200);
        Executors
                .newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory("LogFileWriter"))
                .scheduleAtFixedRate(new RunnableWithExceptionProtection(() -> {
                    try {
                        logBuffer.drainTo(outputLogs);
                        for (String log : outputLogs) {
                            writeToFile(log + Constants.LINE_SEPARATOR);
                        }
                        try {
                            fileOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        outputLogs.clear();
                    }
                }, t -> {
                }), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * @param message to be written into the file.
     */
    private void writeToFile(String message) {
        if (prepareWriteStream()) {
            try {
                fileOutputStream.write(message.getBytes());
                fileSize += message.length();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                switchFile();
            }
        }
    }

    private void switchFile() {
        if (fileSize > Config.Logging.MAX_FILE_SIZE) {
            forceExecute(() -> {
                fileOutputStream.flush();
                return null;
            });
            forceExecute(() -> {
                fileOutputStream.close();
                return null;
            });
            forceExecute(() -> {
                new File(Config.Logging.DIR, Config.Logging.FILE_NAME)
                        .renameTo(new File(Config.Logging.DIR,
                                Config.Logging.FILE_NAME + new SimpleDateFormat(".yyyy_MM_dd_HH_mm_ss").format(new Date())));
                return null;
            });
            forceExecute(() -> {
                fileOutputStream = null;
                return null;
            });

            if (Config.Logging.MAX_HISTORY_FILES > 0) {
                deleteExpiredFiles();
            }
        }
    }

    /**
     * load history log file name array
     *
     * @return history log file name array
     */
    private String[] getHistoryFilePath() {
        File path = new File(Config.Logging.DIR);
        String[] pathArr = path.list((dir, name) -> filenamePattern.matcher(name).matches());

        return pathArr;
    }

    /**
     * delete expired log files
     */
    private void deleteExpiredFiles() {
        String[] historyFileArr = getHistoryFilePath();
        if (historyFileArr != null && historyFileArr.length > Config.Logging.MAX_HISTORY_FILES) {

            Arrays.sort(historyFileArr, Comparator.reverseOrder());

            for (int i = Config.Logging.MAX_HISTORY_FILES; i < historyFileArr.length; i++) {
                File expiredFile = new File(Config.Logging.DIR, historyFileArr[i]);
                expiredFile.delete();
            }
        }
    }

    private void forceExecute(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if stream is prepared ready.
     */
    private boolean prepareWriteStream() {
        if (fileOutputStream != null) {
            return true;
        }
        File logFilePath = new File(Config.Logging.DIR);
        if (!logFilePath.exists()) {
            logFilePath.mkdirs();
        } else if (!logFilePath.isDirectory()) {
            System.err.println("Log dir(" + Config.Logging.DIR + ") is not a directory.");
        }
        try {
            fileOutputStream = new FileOutputStream(new File(logFilePath, Config.Logging.FILE_NAME), true);
            fileSize = Long.valueOf(new File(logFilePath, Config.Logging.FILE_NAME).length()).intValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileOutputStream != null;
    }

    /**
     * Write log to the queue. W/ performance trade off, set 2ms timeout for the log OP.
     *
     * @param message to log
     */
    @Override
    public void write(String message) {
        try {
            logBuffer.offer(message, 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            System.out.println("InterruptedException happened, thread name = " + threadName);
            e.printStackTrace();
        }
    }
}

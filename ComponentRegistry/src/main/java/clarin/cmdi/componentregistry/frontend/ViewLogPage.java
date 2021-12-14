package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author paucas
 */
public class ViewLogPage extends SecureAdminWebPage {
    
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ViewLogPage.class);
    
    public ViewLogPage(final PageParameters pageParameters) {
        super(pageParameters);
        addLinks();
        addLogFileContent();
    }
    
    private void addLogFileContent() {
        final int tailSize = 1000000; // Megabyte

        final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        numberFormat.setGroupingUsed(true);
        
        try {
            final File logFile = getLogFile();
            if (logFile == null) {
                addErrorValues("Log file not found");
            } else {
                try {
                    RandomAccessFile raLogFile = null;
                    try {
                        raLogFile = geFileTail(logFile, tailSize);
                        final String content = getLogFileContent(raLogFile);
                        
                        add(new Label("logLabel", String.format("Showing final %s bytes (or less) of total %s in %s:", numberFormat.format(tailSize), numberFormat.format(raLogFile.length()), logFile)));
                        add(new TextArea("logText", new Model(content)));
                        
                        add(new DownloadLink("logDownloadLink", logFile));
                    } catch (IOException ioEx) {
                        addErrorValues(ioEx.getMessage());
                        throw (ioEx);
                    } finally {
                        if (raLogFile != null) {
                            raLogFile.close();
                        }
                    }
                } catch (IOException ioEx) {
                    logger.error("Error in reading log file", ioEx);
                    throw ioEx;
                }
            }
        } catch (Exception ex) {
            addErrorValues(ex.getMessage());
        }
    }
    
    private void addErrorValues(String message) {
        add(new Label("logLabel", "Could not read from log file. See error message below."));
        add(new TextArea("logText", new Model(message)));
        add(new DownloadLink("logDownloadLink", new File("")).setEnabled(false));
    }
    
    private String getLogFileContent(final RandomAccessFile randomAccessFile) throws IOException {
        String currentLine;
        StringBuilder contentBuilder = new StringBuilder();
        while ((currentLine = randomAccessFile.readLine()) != null) {
            contentBuilder.append(currentLine).append("\n");
        }
        String content = contentBuilder.toString();
        return content;
    }
    
    private RandomAccessFile geFileTail(File logFile, int tailLength) throws IOException, FileNotFoundException {
        // Skip to tail of file
        final RandomAccessFile raLogFile = new RandomAccessFile(logFile, "r");
        final long startPosition = raLogFile.length() - tailLength;
        if (startPosition > 0) {
            raLogFile.seek(startPosition);
            // Read until end of line so we don't end up halfway some random line
            raLogFile.readLine();
        }
        return raLogFile;
    }
    
    private File getLogFile() throws Exception {
        // Get file from appender
        final LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext();
        Appender appender = context.getConfiguration().getAppender("FILE");
        if (appender instanceof RollingFileAppender) {
            final File file = new File(((RollingFileAppender) appender).getFileName());
            if (file.exists()) {
                return file;
            } else {
                throw new Exception("File configured in appender does not exist: " + file.getAbsolutePath());
            }
        } else {
            logger.warn("Log view: unexpected appender {}, instance of RollingFileAppender expected", Objects.toString(appender));
            throw new Exception("Cannot find log appender; found " + Objects.toString(appender) + ", expected RollingFileAppender");
        }
    }
}

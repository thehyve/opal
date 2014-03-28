/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.batch;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.shiro.SecurityUtils;
import org.obiba.core.util.StringUtil;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;


public class DataExportParameters {

  private static final Logger log = LoggerFactory.getLogger(DataExportParameters.class);

  private final OpalRuntime opalRuntime;

  private final Map<String,JobParameter> parameters;

  public DataExportParameters(OpalRuntime opalRuntime, Commands.ExportCommandOptionsDto options) {
    this.opalRuntime = opalRuntime;
    parameters = new LinkedHashMap<String, JobParameter>();
    parameters.put("submission-date", new JobParameter(new Date()));
    parameters.put("owner", new JobParameter(SecurityUtils.getSubject().getPrincipal().toString()));
    if (options.hasIdConfig()) {
      parameters.put("id-config", new JobParameter(options.getIdConfig().getName()));
    }
    if (options.hasSource()) {
      parameters.put("source", new JobParameter(options.getSource()));
    }
    if (options.hasDestination()) {
      parameters.put("destination", new JobParameter(options.getDestination()));
    }
    if (options.hasOut()) {
      String out = options.getOut();
      if (options.hasFormat()) {
        out = addFileExtensionIfMissing(out, options.getFormat());
      }
      parameters.put("out", new JobParameter(out));
    }
    if (options.hasDestinationTableName()) {
      parameters.put("destination-table-name", new JobParameter(options.getDestinationTableName()));
    }
    parameters.put("non-incremental", new JobParameter(Boolean.toString(options.getNonIncremental())));
    parameters.put("no-values", new JobParameter(Boolean.toString(options.getNoValues())));
    parameters.put("no-variables", new JobParameter(Boolean.toString(options.getNoVariables())));
    parameters.put("copy-null-values", new JobParameter(Boolean.toString(options.getCopyNullValues())));
    parameters.put("tables", new JobParameter(StringUtil.collectionToString(options.getTablesList())));
  }

  public JobParameters asJobParameters() {
    return new JobParameters(parameters);
  }

  //
  // Methods
  //

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  private String addFileExtensionIfMissing(String outputFilePath, String outputFileFormat) {
    String modifiedPath = outputFilePath;

    FileObject file = null;
    try {
      file = resolveFileInFileSystem(outputFilePath);

      // Add the extension if the file object is an existing file (FileType.FILE)
      // or a new file (FileType.IMAGINARY). We assume here that any "imaginary" file object
      // is a non-existent folder.
      if(file.getType() == FileType.FILE) {
        modifiedPath = addExtension(outputFileFormat, outputFilePath);

      } else if(file.getType() == FileType.IMAGINARY) {
        if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
          modifiedPath = addExtension(outputFileFormat, outputFilePath);
        } else if("csv".equals(outputFileFormat)) {
          // Create the directory
          file.createFolder();
        }
      }

    } catch(FileSystemException ex) {
      log.error("Unexpected file system exception in addFileExtensionIfMissing", ex);
    }

    return modifiedPath;
  }

  private String addExtension(String outputFileFormat, String outputFilePath) {
    if("csv".equals(outputFileFormat) && !outputFilePath.endsWith(".csv")) {
      return outputFilePath + ".csv";
    }
    if("excel".equals(outputFileFormat) && !outputFilePath.endsWith(".xls") &&
        !outputFilePath.endsWith(".xlsx")) {
      return outputFilePath + ".xlsx"; // prefer .xlsx over .xls
    }
    if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
      return outputFilePath + ".zip";
    }
    return outputFilePath;
  }
}

package de.digitalcollections.cudami.server.backend.impl.file.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceNotFoundException;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Repository
public class FileResourceBinaryRepositoryImpl implements FileResourceBinaryRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceBinaryRepositoryImpl.class);
  private final String repositoryFolderPath;
  private final ResourceLoader resourceLoader;

  @Autowired
  public FileResourceBinaryRepositoryImpl(
      @Value("${cudami.repositoryFolderPath}") String folderPath, ResourceLoader resourceLoader) {
    this.repositoryFolderPath = folderPath.replace("~", System.getProperty("user.home"));
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void assertReadability(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException {
    try (InputStream is = getInputStream(resource)) {
      if (is.available() <= 0) {
        throw new ResourceIOException("Cannot read " + resource.getFilename() + ": Empty file");
      }
    } catch (ResourceIOException e) {
      throw new ResourceIOException("Cannot read " + resource.getFilename() + ": Empty file");
    } catch (ResourceNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new ResourceIOException(
          "Cannot read " + resource.getFilename() + ": " + e.getMessage());
    }
  }

  protected URI createUri(@NonNull UUID uuid, MimeType mimeType) {
    Objects.requireNonNull(uuid, "uuid must not be null");

    final String uuidStr = uuid.toString();
    String uuidPath = getSplittedUuidPath(uuidStr);
    Path path = Paths.get(repositoryFolderPath, uuidPath, uuidStr);
    String location = "file://" + path.toString();

    if (mimeType != null && !mimeType.getExtensions().isEmpty()) {
      location = location + "." + mimeType.getExtensions().get(0);
      // example location =
      // file:///local/cudami/resourceRepository/a30c/f362/5992/4f5a/8de0/6193/8134/e721/a30cf362-5992-4f5a-8de0-61938134e721.xml
    }
    return URI.create(location);
  }

  @Override
  public FileResource find(String uuidStr) throws ResourceIOException, ResourceNotFoundException {
    FileResource resource = new FileResourceImpl();

    final UUID uuid = UUID.fromString(uuidStr);
    resource.setUuid(uuid);

    URI uri = getUri(uuid);
    if (!resourceLoader.getResource(uri.toString()).isReadable()) {
      throw new ResourceIOException("File resource at uri " + uri + " is not readable");
    }
    resource.setUri(uri);

    String filename = uri.toString().substring(uri.toString().lastIndexOf("/"));
    resource.setFilename(filename);

    resource.setMimeType(MimeType.fromFilename(filename));

    Resource springResource = resourceLoader.getResource(uri.toString());

    long lastModified = getLastModified(springResource);
    if (lastModified != 0) {
      // lastmodified by code in java.io.File#lastModified (is also used in Spring's
      // core.io.Resource) is in milliseconds!
      resource.setLastModified(
          Instant.ofEpochMilli(lastModified).atOffset(ZoneOffset.UTC).toLocalDateTime());
    } else {
      resource.setLastModified(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    }

    long length = getSize(springResource);
    if (length > -1) {
      resource.setSizeInBytes(length);
    }
    return resource;
  }

  @Override
  public byte[] getAsBytes(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException {
    try {
      assertReadability(resource);
      return IOUtils.toByteArray(this.getInputStream(resource));
    } catch (IOException ex) {
      String msg = "Could not read bytes from resource: " + resource;
      LOGGER.error(msg, ex);
      throw new ResourceIOException(msg, ex);
    }
  }

  @Override
  public Document getAsDocument(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException {
    Document doc = null;
    try {
      // get InputStream on resource
      try (InputStream is = getInputStream(resource)) {
        // create Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(is);
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Got document: " + doc);
      }
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      throw new ResourceIOException(
          "Cannot read document from resolved resource '" + resource.getUri().toString() + "'", ex);
    }
    return doc;
  }

  private void setImageProperties(ImageFileResource fileResource) throws IOException {
    try (ImageInputStream in = ImageIO.createImageInputStream(new File(fileResource.getUri()))) {
      final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        try {
          reader.setInput(in);
          fileResource.setWidth(reader.getWidth(0));
          fileResource.setHeight(reader.getHeight(0));
        } finally {
          reader.dispose();
        }
      }
    }
  }

  public InputStream getInputStream(URI resourceUri)
      throws ResourceIOException, ResourceNotFoundException {
    try {
      String location = resourceUri.toString();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Getting inputstream for location '{}'.", location);
      }
      final Resource resource = resourceLoader.getResource(location);
      if (!resourceUri.getScheme().startsWith("http") && !resource.exists()) {
        throw new ResourceNotFoundException("Resource not found at location '" + location + "'");
      }
      return resource.getInputStream();
    } catch (IOException e) {
      throw new ResourceIOException(e);
    }
  }

  @Override
  public InputStream getInputStream(FileResource resource)
      throws ResourceIOException, ResourceNotFoundException {
    return getInputStream(resource.getUri());
  }

  protected long getLastModified(Resource springResource) {
    try {
      return springResource.lastModified();
    } catch (FileNotFoundException e) {
      LOGGER.warn("Resource " + springResource.toString() + " does not exist.");
    } catch (IOException ex) {
      LOGGER.warn("Can not get lastModified for resource " + springResource.toString(), ex);
    }
    return -1;
  }

  public Reader getReader(FileResource resource, Charset charset)
      throws ResourceIOException, ResourceNotFoundException {
    return new InputStreamReader(this.getInputStream(resource), charset);
  }

  protected long getSize(Resource springResource) {
    try {
      long length = springResource.contentLength();
      return length;
    } catch (IOException ex) {
      LOGGER.warn("Can not get size for resource " + springResource.toString(), ex);
    }
    return -1;
  }

  protected String getSplittedUuidPath(String uuid) {
    // regex
    // '^([0-9a-f]{4})([0-9a-f]{4})-([0-9a-f]{4})-([1-5][0-9a-f]{3})-([89ab][0-9a-f]{3})-([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})$' could be used, too...
    String uuidWithoutDashes = uuid.replaceAll("-", "");
    String[] pathParts = splitEqually(uuidWithoutDashes, 4);
    String splittedUuidPath = String.join(File.separator, pathParts);
    return splittedUuidPath;
  }

  protected URI getUri(@NonNull UUID uuid) throws ResourceNotFoundException {
    Objects.requireNonNull(uuid, "uuid must not be null");

    final String uuidStr = uuid.toString();
    String uuidPath = getSplittedUuidPath(uuidStr);
    Path path = Paths.get(repositoryFolderPath, uuidPath);
    String location = "file://" + path.toString();

    File directory = path.toFile();
    if (!directory.isDirectory()) {
      throw new ResourceNotFoundException(path.toString() + " does not exist");
    }
    // create new filename filter
    FilenameFilter fileNameFilter =
        (File dir, String name) -> {
          return name.startsWith(uuidStr);
        };

    File[] matchingFiles = directory.listFiles(fileNameFilter);
    if (matchingFiles != null && matchingFiles.length > 0) {
      File file = matchingFiles[0];
      String filename = file.getName();
      location = location + "/" + filename;
      return URI.create(location);
    }
    throw new ResourceNotFoundException("No matching file found in " + path.toString());
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData)
      throws ResourceIOException {
    Assert.notNull(fileResource, "fileResource must not be null");
    Assert.notNull(binaryData, "binaryData must not be null");

    try {
      if (fileResource.isReadonly()) {
        throw new ResourceIOException(
            "fileResource is read only, does not support write-operations.");
      }

      URI uri = createUri(fileResource.getUuid(), fileResource.getMimeType());
      fileResource.setUri(uri);

      //      final String scheme = uri.getScheme();
      //      if ("http".equals(scheme) || "https".equals(scheme)) {
      //        throw new ResourceIOException("Scheme not supported for write-operations: " + scheme
      // + " (" + uri + ")");
      //      }
      final Path parentDirectory = Paths.get(uri).getParent();
      if (parentDirectory == null) {
        throw new ResourceIOException("No parent directory defined for uri: " + uri);
      }
      Files.createDirectories(parentDirectory);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Writing: " + uri);
      }
      long size = IOUtils.copyLarge(binaryData, new FileOutputStream(Paths.get(uri).toFile()));
      fileResource.setSizeInBytes(size);

      if (fileResource instanceof ImageFileResource) {
        setImageProperties((ImageFileResource) fileResource);
      }

    } catch (IOException ex) {
      String msg = "Error writing binary data of fileresource " + fileResource.getUuid().toString();
      throw new ResourceIOException(msg, ex);
    }
    return fileResource;
  }

  @Override
  public FileResource save(FileResource resource, String input, Charset charset)
      throws ResourceIOException {
    try (InputStream in = new ReaderInputStream(new StringReader(input), charset)) {
      return save(resource, in);
    } catch (IOException ex) {
      String msg = "Could not write data to uri " + String.valueOf(resource.getUri());
      LOGGER.error(msg, ex);
      throw new ResourceIOException(msg, ex);
    }
  }

  /**
   * Convert "Thequickbrownfoxjumps" to String[] {"Theq","uick","brow","nfox","jump","s"}
   *
   * @param text text to split
   * @param partLength length of parts
   * @return array of text parts
   */
  private String[] splitEqually(String text, int partLength) {
    if (StringUtils.isEmpty(text) || partLength == 0) {
      return new String[] {text};
    }

    int textLength = text.length();

    // Number of parts
    int numberOfParts = (textLength + partLength - 1) / partLength;
    String[] parts = new String[numberOfParts];

    // Break into parts
    int offset = 0;
    int i = 0;
    while (i < numberOfParts) {
      parts[i] = text.substring(offset, Math.min(offset + partLength, textLength));
      offset += partLength;
      i++;
    }

    return parts;
  }
}

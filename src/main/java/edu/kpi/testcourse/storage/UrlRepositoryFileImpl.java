package edu.kpi.testcourse.storage;

import com.google.gson.reflect.TypeToken;
import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.UrlShortenerConfig;
import edu.kpi.testcourse.serialization.JsonTool;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A file-backed implementation of {@link UrlRepository} suitable for use in production.
 */
public class UrlRepositoryFileImpl implements UrlRepository {

  // Urls, keyed by alias.
  private final Map<String, UrlAlias> urlMapByAlias;

  private final JsonTool jsonTool;
  private final Path jsonFilePath;

  /**
   * Creates an instance.
   */
  @Inject
  public UrlRepositoryFileImpl(JsonTool jsonTool, UrlShortenerConfig appConfig) {
    this.jsonTool = jsonTool;
    this.jsonFilePath = makeJsonFilePath(appConfig.storageRoot());
    this.urlMapByAlias = readUrlsFromJsonDatabaseFile(jsonTool, this.jsonFilePath);
  }

  @Override
  public synchronized void createUrlAlias(UrlAlias urlAlias) throws AliasAlreadyExist {
    if (urlMapByAlias.containsKey(urlAlias.alias())) {
      throw new AliasAlreadyExist();
    }

    urlMapByAlias.put(urlAlias.alias(), urlAlias);
    writeUrlsToJsonDatabaseFile(jsonTool, urlMapByAlias, jsonFilePath);
  }

  @Nullable
  @Override
  public UrlAlias findUrlAlias(String alias) {
    return urlMapByAlias.get(alias);
  }

  @Override
  public void deleteUrlAlias(String email, String alias) throws PermissionDenied {
    throw new PermissionDenied();
  }

  @Override
  public List<UrlAlias> getAllAliasesForUser(String userEmail) {
    return null;
  }

  private static Path makeJsonFilePath(Path storageRoot) {
    return storageRoot.resolve("url-repository.json");
  }

  private static Map<String, UrlAlias> readUrlsFromJsonDatabaseFile(
      JsonTool jsonTool, Path sourceFilePath
  ) {
    String json;
    try {
      json = Files.readString(sourceFilePath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Type type = new TypeToken<HashMap<String, UrlAlias>>(){}.getType();
    Map<String, UrlAlias> result = jsonTool.fromJson(json, type);
    if (result == null) {
      throw new RuntimeException("Could not deserialize the aliases repository");
    }
    return result;
  }

  private static void writeUrlsToJsonDatabaseFile(
      JsonTool jsonTool, Map<String, UrlAlias> urls, Path destinationFilePath
  ) {
    String json = jsonTool.toJson(urls);
    try {
      Files.write(destinationFilePath, json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

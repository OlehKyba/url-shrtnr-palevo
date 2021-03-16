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
import java.util.ArrayList;
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
  // All User urls, keyed by email
  private final Map<String, List<UrlAlias>> urlsMapByEmail;

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
    this.urlsMapByEmail = makeUrlsMapByEmail(urlMapByAlias);
  }

  @Override
  public synchronized void createUrlAlias(UrlAlias urlAlias) throws AliasAlreadyExist {
    if (urlMapByAlias.containsKey(urlAlias.alias())) {
      throw new AliasAlreadyExist();
    }

    urlMapByAlias.put(urlAlias.alias(), urlAlias);
    putInMapByEmail(urlsMapByEmail, urlAlias);

    syncUrlsWithJsonDatabaseFile();
  }

  @Nullable
  @Override
  public synchronized UrlAlias findUrlAlias(String alias) {
    return urlMapByAlias.get(alias);
  }

  @Override
  public void deleteUrlAlias(String email, String alias) throws PermissionDenied {
    UrlAlias urlAlias = urlMapByAlias.get(alias);

    if (urlAlias == null) {
      throw new RuntimeException("UrlAlias record not found!");
    }

    if (!urlAlias.email().equals(email)) {
      throw new PermissionDenied();
    }

    urlMapByAlias.remove(alias);
    List<UrlAlias> userUrls = urlsMapByEmail.get(email);
    userUrls.remove(urlAlias);

    syncUrlsWithJsonDatabaseFile();
  }

  @Override
  public synchronized List<UrlAlias> getAllAliasesForUser(String userEmail) {
    return urlsMapByEmail.getOrDefault(userEmail, new ArrayList<>());
  }

  private static Map<String, List<UrlAlias>> makeUrlsMapByEmail(
      Map<String, UrlAlias> urlMapByAlias
  ) {
    Map<String, List<UrlAlias>> urlsMapByEmail = new HashMap<>();

    for (UrlAlias urlAlias : urlMapByAlias.values()) {
      putInMapByEmail(urlsMapByEmail, urlAlias);
    }
    return urlsMapByEmail;
  }

  private static void putInMapByEmail(
      Map<String, List<UrlAlias>> urlsMapByEmail, UrlAlias urlAlias
  ) {
    List<UrlAlias> urlsByEmailList = urlsMapByEmail.computeIfAbsent(urlAlias.email(),
        s -> new ArrayList<>());
    urlsByEmailList.add(urlAlias);
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

  private void syncUrlsWithJsonDatabaseFile() {
    String json = jsonTool.toJson(urlMapByAlias);
    try {
      Files.writeString(jsonFilePath, json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

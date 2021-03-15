package edu.kpi.testcourse.storage;

import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.UrlShortenerConfig;
import edu.kpi.testcourse.serialization.JsonToolJacksonImpl;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlRepositoryFileImplTest {
  UrlShortenerConfig appConfig;
  UrlRepository urlRepository;

  @BeforeEach
  void setUp() {
    try {
      appConfig = new UrlShortenerConfig(
        Files.createTempDirectory("url-repository-file-test"));
      Files.write(appConfig.storageRoot().resolve("url-repository.json"), "{}".getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    urlRepository = new UrlRepositoryFileImpl(new JsonToolJacksonImpl(), appConfig);
  }

  @AfterEach
  void tearDown() {
    try {
      Files.delete(appConfig.storageRoot().resolve("url-repository.json"));
      Files.delete(appConfig.storageRoot());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldCreateAlias() {
    //GIVEN
    UrlAlias url = new UrlAlias("alias", "http://www.google.com", "user@test.com");

    //WHEN
    urlRepository.createUrlAlias(url);

    //THEN
    assertThat(urlRepository.findUrlAlias("alias")).isEqualTo(url);
  }

  @Test
  void serializesOneUrl() throws IOException {
    // GIVEN
    String alias = "testAlias";
    String email = "user@example.org";
    String destinationUrl = "http://www.youtube.com";
    UrlAlias url = new UrlAlias(alias, destinationUrl, email);

    // WHEN
    urlRepository.createUrlAlias(url);

    // THEN
    Assertions.assertThat(
      Files.readString(appConfig.storageRoot()
        .resolve("url-repository.json"), StandardCharsets.UTF_8)
    ).contains(alias, email, destinationUrl);
  }

  @Test
  void deserializesOneUrl() {
    // GIVEN
    UrlAlias url = new UrlAlias("test", "http://www.facebook.com", "user@example.org");
    urlRepository.createUrlAlias(url);

    // WHEN
    // The new repository instance must read the data in constructor.
    urlRepository = new UrlRepositoryFileImpl(new JsonToolJacksonImpl(), appConfig);

    // THEN
    Assertions.assertThat(urlRepository.findUrlAlias("test")).isEqualTo(url);
  }

  @Test
  void shouldThrowError_whenAliasAlreadyExists() {
    // GIVEN
    UrlAlias urlAlias = new UrlAlias("alias", "http://twitter.com", "test@gmail.com");
    urlRepository.createUrlAlias(urlAlias);

    // WHEN + THEN
    assertThrows(AliasAlreadyExist.class, () -> urlRepository.createUrlAlias(urlAlias));
  }

  @Test
  void shouldFindNull_whenRepositoryIsEmpty() {
    // GIVEN
    String notExistsAlias = "alias";

    // WHEN + THEN
    assertThat(urlRepository.findUrlAlias(notExistsAlias)).isNull();
  }
}

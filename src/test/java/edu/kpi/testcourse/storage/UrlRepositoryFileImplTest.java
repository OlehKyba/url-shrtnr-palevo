package edu.kpi.testcourse.storage;

import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.logic.UrlShortenerConfig;
import edu.kpi.testcourse.serialization.JsonToolJacksonImpl;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import edu.kpi.testcourse.storage.UrlRepository.PermissionDenied;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
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

  @Test
  void shouldGetAllAliasesForUser() {
    // GIVEN
    UrlAlias url1 = new UrlAlias("alias1", "http://www.test1.com", "user@test.com");
    UrlAlias url2 = new UrlAlias("alias2", "http://www.test2.com", "user@test.com");
    UrlAlias url3 = new UrlAlias("alias3", "http://www.test3.com", "user@test.com");
    UrlAlias url4 = new UrlAlias("alias4", "http://www.test4.com", "notTargetUser@test.com");
    urlRepository.createUrlAlias(url1);
    urlRepository.createUrlAlias(url2);
    urlRepository.createUrlAlias(url3);
    urlRepository.createUrlAlias(url4);

    // WHEN
    List<UrlAlias> urls = urlRepository.getAllAliasesForUser("user@test.com");

    // THEN
    assertThat(urls.size()).isEqualTo(3);
    assertThat(urls).asList().contains(url1, url2, url3);
  }

  @Test
  void shouldEmptyListAliasesForUser() {
    // GIVEN
    UrlAlias url = new UrlAlias("alias", "http://www.test.com", "notTargetUser@test.com");
    urlRepository.createUrlAlias(url);

    // WHEN
    List<UrlAlias> urls = urlRepository.getAllAliasesForUser("user@test.com");

    // THEN
    assertThat(urls).asList().isEmpty();
  }

  @Test
  void shouldDeleteUrl() {
    // GIVEN
    UrlAlias url = new UrlAlias("alias", "http://www.test.com", "user@test.com");
    urlRepository.createUrlAlias(url);

    // WHEN
    urlRepository.deleteUrlAlias("user@test.com", "alias");

    // THEN
    assertThat(urlRepository.findUrlAlias("alias")).isNull();
    assertThat(urlRepository.getAllAliasesForUser("user@test.com"))
                            .asList()
                            .doesNotContain(url);
  }

  @Test
  void shouldThrowRuntimeError_whenNoAliasForDeletion() {
    // GIVEN
    String notExistsAlias = "alias";
    String email = "user@test.com";

    // WHEN + THEN
    assertThrows(RuntimeException.class,
      () -> urlRepository.deleteUrlAlias(email, notExistsAlias));
  }

  @Test
  void shouldThrowPermissionDenied_whenAliasDoesNotBelongToUser() {
    // GIVEN
    UrlAlias urlAlias = new UrlAlias("alias", "http://google.com", "user@test.com");
    urlRepository.createUrlAlias(urlAlias);

    // WHEN + THEN
    assertThrows(PermissionDenied.class,
      () -> urlRepository.deleteUrlAlias("test@user.com", "alias"));
  }
}

package edu.kpi.testcourse.storage;

import edu.kpi.testcourse.entities.UrlAlias;
import io.micronaut.context.annotation.Aliases;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An in-memory fake implementation of {@link UrlRepository}.
 */
public class UrlRepositoryFakeImpl implements UrlRepository {
  private final HashMap<String, UrlAlias> aliases = new HashMap<>();

  @Override
  public void createUrlAlias(UrlAlias urlAlias) {
    if (aliases.containsKey(urlAlias.alias())) {
      throw new UrlRepository.AliasAlreadyExist();
    }

    aliases.put(urlAlias.alias(), urlAlias);
  }

  @Override
  public @Nullable UrlAlias findUrlAlias(String alias) {
    return aliases.get(alias);
  }

  @Override
  public void deleteUrlAlias(String email, String alias) {
    // TODO: We should implement it
    throw new UnsupportedOperationException();
  }

  /**
   * Create a list of UrlAlias objects by email.
   *
   * @param userEmail gets user email
   * @return list of UrlAlias objects
   * @throws edu.kpi.testcourse.storage.UrlRepository.PermissionDenied
   *         error if emails are not equal
   */
  @Override
  public List<UrlAlias> getAllAliasesForUser(String userEmail) {
    List<UrlAlias> aliasesList = new ArrayList<>();
    for (UrlAlias urlAlias: aliases.values()) {
      if (urlAlias.email().equals(userEmail)) {
        aliasesList.add(urlAlias);
      }
      else {
        throw new PermissionDenied();
      }
    }
    return aliasesList;
  }
}

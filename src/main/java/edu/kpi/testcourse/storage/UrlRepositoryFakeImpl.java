package edu.kpi.testcourse.storage;

import edu.kpi.testcourse.entities.UrlAlias;
import java.util.HashMap;
import java.util.LinkedList;
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

  /**
   * Delete the UrlAlias if exists from aliases HashMap.
   *
   * @param email of the user to whom the UrlAlias belongs
   * @param alias the UrlAlias which should be removed
   *
   * @throws RuntimeException if no such email or alias
   * @throws edu.kpi.testcourse.storage.UrlRepository.PermissionDenied if passed email is not equal to one which is stored in found UrlAlias object
   */

  @Override
  public void deleteUrlAlias(String email, String alias) {
    UrlAlias foundUrlAlias = aliases.get(alias);

    if (foundUrlAlias == null || foundUrlAlias.email() == null) {
      throw new RuntimeException();
    }

    if (foundUrlAlias.email().equals(email)) {
      aliases.remove(alias);
    } else {
      throw new PermissionDenied();
    }
  }

  @Override
  public List<UrlAlias> getAllAliasesForUser(String userEmail) {
    // TODO: We should implement it
    throw new UnsupportedOperationException();
  }
}

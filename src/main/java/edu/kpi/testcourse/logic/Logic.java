package edu.kpi.testcourse.logic;

import edu.kpi.testcourse.entities.UrlAlias;
import edu.kpi.testcourse.entities.User;
import edu.kpi.testcourse.storage.UrlRepository;
import edu.kpi.testcourse.storage.UrlRepository.AliasAlreadyExist;
import edu.kpi.testcourse.storage.UserRepository;
import java.util.List;

/**
 * Business logic of the URL shortener application.
 */
public class Logic {
  private final UserRepository users;
  private final UrlRepository urls;
  private final HashUtils hashUtils;

  /**
   * Creates an instance.
   */
  public Logic(UserRepository users, UrlRepository urls) {
    this.users = users;
    this.urls = urls;
    this.hashUtils = new HashUtils();
  }

  /**
   * Create a new user.
   *
   * @param email users email
   * @param password users password
   * @throws UserIsAlreadyCreated is thrown if user is already created
   */
  public void createNewUser(String email, String password) throws UserIsAlreadyCreated {
    if (users.findUser(email) != null) {
      throw new UserIsAlreadyCreated();
    } else {
      users.createUser(new User(email, hashUtils.generateHash(password)));
    }
  }

  /**
   * Gives an answer if user is registered and password is correct.
   *
   * @param email a users email
   * @param password a users password
   * @return if user is registered and password is correct
   */
  public boolean isUserValid(String email, String password) {
    User user = users.findUser(email);
    if (user == null) {
      return false;
    }

    return hashUtils.validatePassword(password, user.passwordHash());
  }

  /**
   * Create a new URL alias (shortened version).
   *
   * @param email an email of a user that creates the alias
   * @param url a full URL
   * @param alias a proposed alias
   *
   * @return a shortened URL
   */
  public String createNewAlias(String email, String url, String alias) throws AliasAlreadyExist {
    String finalAlias;
    if (alias == null || alias.isEmpty()) {
      // TODO: Generate short alias
      throw new UnsupportedOperationException("Is not implemented yet");
    } else {
      finalAlias = alias;
    }

    urls.createUrlAlias(new UrlAlias(finalAlias, url, email));

    return finalAlias;
  }

  /**
   * Get full URL by alias.
   *
   * @param alias a short URL alias
   * @return a full URL
   */
  public String findFullUrl(String alias) {
    UrlAlias urlAlias = urls.findUrlAlias(alias);

    if (urlAlias != null) {
      return urlAlias.destinationUrl();
    }

    return null;
  }

  /**
   * Error for situation when we are trying to register already registered user.
   */
  public static class UserIsAlreadyCreated extends Throwable {
    public UserIsAlreadyCreated() {
      super("User with such email is already created");
    }
  }

  /**
   * GetAllAliasesForUser wrapper. 
   *
   * @param userEmail user email
   * @return list of UrlAlias objects
   */
  public List<UrlAlias> getAllAliasesForUser(String userEmail) {
    return urls.getAllAliasesForUser(userEmail);
  }
  
  /**
   * DeleteUrlAlias wrapper.
   *
   * @param email user email
   * @param alias alias which has to be deleted
   */
  public void deleteAlias(String email, String alias) {
    urls.deleteUrlAlias(email, alias);
  }
}

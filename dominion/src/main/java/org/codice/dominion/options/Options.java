/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.dominion.options;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import org.codice.dominion.Dominion;
import org.codice.dominion.conditions.Conditions;
import org.codice.dominion.interpolate.Interpolate;
import org.codice.dominion.options.Option.Annotation;
import org.codice.dominion.options.Options.Repeatables.AddLocalGroups;
import org.codice.dominion.options.Options.Repeatables.AddLocalUsers;
import org.codice.dominion.options.Options.Repeatables.AddMavenRepositories;
import org.codice.dominion.options.Options.Repeatables.GrantPermissions;
import org.codice.dominion.options.Options.Repeatables.RemoveConfigProperties;
import org.codice.dominion.options.Options.Repeatables.UpdateConfigProperties;
import org.codice.maven.MavenUrl;

/**
 * This class defines annotations that can be used to configure containers. It is solely used for
 * scoping.
 */
public class Options {
  /**
   * Constant internally used in annotation definition to indicate a particular <code>String</code>
   * attribute is not yet defined.
   */
  public static final String NOT_DEFINED = "_not_defined_";

  /** Sets of possible locations in a file where to insert content. */
  public enum Location {
    /** Prepends the content at the beginning of the existing file. */
    PREPEND,

    /** Appends the content at the end of the existing file. */
    APPEND
  }

  /** Option to install the Dominion driver specific configuration. */
  // make sure we have at least one user capable of SSH to the container
  @SuppressWarnings("squid:S2068" /* hard-coded password is for testing */)
  @Options.AddLocalUser(
    userId = Dominion.DOMINION_USER_ID,
    password = "{dominion.password:-dominion}",
    roles = {
      UserRoles.GROUP,
      UserRoles.ADMIN,
      UserRoles.MANAGER,
      UserRoles.VIEWER,
      UserRoles.SYSTEM_ADMIN,
      UserRoles.SYSTEM_BUNDLES,
      UserRoles.SSH
    }
  )
  // the following provides support for Jenkins pipeline withMaven() block
  @Options.OverrideAndPropagateMavenSettings("{env.MVN_SETTINGS:-}")
  @Options.OverrideAndPropagateMavenLocalRepository("{maven.repo.local:-}")
  @Options.PropagateMavenRepositoriesFromActiveProfiles
  @Options.AddMavenRepository({
    // required to allow dominion and other libraries to include their own codice artifacts
    "{snapshots.repository.url:-http://artifacts.codice.org/content/repositories/snapshots/}@snapshots@noreleases@id=snapshots",
    "{releases.repository.url:-http://artifacts.codice.org/content/repositories/releases/}@id=releases"
  })
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface Install {}

  /** This option allows to update a file with the content provided. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Options.Repeatables.UpdateFiles.class)
  public @interface UpdateFile {
    /**
     * Specifies the target file to update relative from the home directory where the distribution
     * was expanded (e.g. <code>"{karaf.home}"</code>).
     *
     * @return the relative target file to update
     */
    @Interpolate
    String target();

    /**
     * Specifies the location in the target file where to append the content (defaults to append).
     *
     * @return the location where to insert the content
     */
    Location location() default Location.APPEND;

    /**
     * Specifies the filename to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the source filename to copy
     */
    @Interpolate
    String file() default Options.NOT_DEFINED;

    /**
     * Specifies the url to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the source url to copy
     */
    @Interpolate
    String url() default Options.NOT_DEFINED;

    /**
     * Specifies the Maven url of an artifact to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the artifact maven url to copy
     */
    MavenUrl artifact() default
        @MavenUrl(groupId = Options.NOT_DEFINED, artifactId = Options.NOT_DEFINED);

    /**
     * Specifies the text to copy to the target file. Each entry will represent a different line in
     * the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the content text to copy
     */
    @Interpolate
    String[] content() default Options.NOT_DEFINED;

    /**
     * Specifies the resource name to copy to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the resource name to copy
     */
    @Interpolate
    String resource() default Options.NOT_DEFINED;
  }

  /**
   * If you do not want to replace (or extend) values in a file but rather simply want to replace a
   * configuration file "brute force" this option is the one. It simply removes the original file
   * and replaces it with the one configured here.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Options.Repeatables.ReplaceFiles.class)
  public @interface ReplaceFile {
    /**
     * Specifies the target file to replace relative from the home directory where the distribution
     * was expanded (e.g. <code>"{karaf.home}"</code>).
     *
     * @return the relative target file to replace
     */
    @Interpolate
    String target();

    /**
     * Specifies the filename to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the source filename to copy
     */
    @Interpolate
    String file() default Options.NOT_DEFINED;

    /**
     * Specifies the url to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the source url to copy
     */
    @Interpolate
    String url() default Options.NOT_DEFINED;

    /**
     * Specifies the Maven url of an artifact to copy its content to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the artifact maven url to copy
     */
    MavenUrl artifact() default
        @MavenUrl(groupId = Options.NOT_DEFINED, artifactId = Options.NOT_DEFINED);

    /**
     * Specifies the text to copy to the target file. Each entry will represent a different line in
     * the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the content text to copy
     */
    @Interpolate
    String[] content() default Options.NOT_DEFINED;

    /**
     * Specifies the resource name to copy to the target file.
     *
     * <p><i>Note:</i> One of {@link #file}, {@link #url}, {@link #artifact()}, {@link #content}, or
     * {@link #resource} must be specified.
     *
     * @return the resource name to copy
     */
    @Interpolate
    String resource() default Options.NOT_DEFINED;
  }

  /**
   * This option allows to set a specific configuration propertyin a configuration file with a given
   * value or to add a value to the set of values associated with a specific key in a configuration
   * file. If the key doesn't exist, one is added with the specified value.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(UpdateConfigProperties.class)
  public @interface UpdateConfigProperty {
    /**
     * Specifies the target configuration file to update relative from the home directory where the
     * distribution was expanded (e.g. <code>"{karaf.home}"</code>).
     *
     * @return the relative target config file to update
     */
    @Interpolate
    String target();

    /**
     * Specifies the operation to perform on the specified config key (defaults to set).
     *
     * @return the operation to perform on the specified config key
     */
    Operation operation() default Operation.SET;

    /**
     * Specifies the config key to update.
     *
     * @return the config key to update
     */
    @Interpolate
    String key();

    /**
     * Specifies the config value to set the config key with or add to the existing one.
     *
     * @return the config value to set the config key with or add to the existing one
     */
    @Interpolate
    String value();

    /** Sets of possible operations to perform while updating the key. */
    public enum Operation {
      /** Replaces the complete value with the one specified. */
      SET,

      /** Replaces the complete value with the absolute path of the one specified. */
      SET_ABSOLUTE_PATH,

      /** Adds the specified value to the end of those already defined for the specified key. */
      ADD,

      /**
       * Removes the specified value from those defined for the specified key.
       *
       * <p><i>Note:</i> Removals will actually be processed after all other updates.
       */
      REMOVE
    }
  }

  /**
   * This option allows to remove a specific configuration property given its key (and its value)
   * from a configuration file
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(RemoveConfigProperties.class)
  public @interface RemoveConfigProperty {
    /**
     * Specifies the target configuration file to update relative from the home directory where the
     * distribution was expanded (e.g. <code>"{karaf.home}"</code>).
     *
     * @return the relative target config file to update
     */
    @Interpolate
    String target();

    /**
     * Specifies the config key to remove.
     *
     * @return the config key to remove
     */
    @Interpolate
    String key();
  }

  /** Option to set a system property. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.SetSystemProperties.class)
  public @interface SetSystemProperty {
    /**
     * Specifies the system property key to set.
     *
     * @return the key for the system property to set
     */
    @Interpolate
    String key();

    /**
     * Specifies the value for the system property (defaults to empty string).
     *
     * @return the value for the system property
     */
    @Interpolate
    String value() default "";
  }

  /**
   * Option to propagate a system property from the driver VM to the container VM. Only meaningful
   * for remote containers.
   *
   * <p>If the given system property is set in the driver VM, Dominion will set the system property
   * with the same key to the same value in the container VM. Ignored if the given system property
   * is not defined.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.PropagateSystemProperties.class)
  public @interface PropagateSystemProperty {
    /**
     * Specifies the system property key(s) to propagate.
     *
     * @return the key(s) for the system property to propagate
     */
    @Interpolate
    String[] value();
  }

  /** Option to set a framework property. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.SetFrameworkProperties.class)
  public @interface SetFrameworkProperty {
    /**
     * Specifies the framework property key to set.
     *
     * @return the key for the framework property to set
     */
    @Interpolate
    String key();

    /**
     * Specifies the value for the framework property (defaults to empty string).
     *
     * @return the value for the framework property
     */
    @Interpolate
    String value() default "";
  }

  /**
   * Option specifying process environment options passed to {@link Runtime#exec(String, String[])}
   * when the container is created. The format for each option is <i>name</i>=<i>value</i>.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.Environments.class)
  public @interface Environment {
    /**
     * Specifies one or more process environment variables.
     *
     * @return one or more options for the container's VM
     */
    @Interpolate
    String[] value();
  }

  /** Option specifying a raw virtual machine option for the container. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.VMOptions.class)
  public @interface VMOption {
    /**
     * Specifies one or more options for the container's virtual machine.
     *
     * @return one or more options for the container's VM
     */
    @Interpolate
    String[] value();
  }

  /**
   * Option to configure the level for a given logger. For example, with PaxExam this would update
   * the <code>etc/org.ops4j.pax.logging.cfg</code> file.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.SetLogLevels.class)
  public @interface SetLogLevel {
    /**
     * Specifies the logger name to configure.
     *
     * @return the logger name to configure
     */
    @Interpolate
    String name();

    /**
     * Specifies the level at which to log for the specified logger name.
     *
     * <p>If the level is empty, then no configuration changes will occur.
     *
     * @return the level at which to log for the specified logger name
     */
    @Interpolate
    String level();
  }

  /**
   * Option to configure the levels for multiple loggers. For example, with PaxExam this would
   * update the <code>etc/org.ops4j.pax.logging.cfg</code> file.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(Repeatables.SetLogLevelss.class)
  public @interface SetLogLevels {
    /**
     * Specifies the log levels at which to log using the format <code>
     * "[logger.name]=[level];[logger.name]=[level]"</code>.
     *
     * <p>If the value is empty, then no configuration changes will occur.
     *
     * @return a formatted string specifying multiple logger names and their corresponding levels at
     *     which to log
     */
    @Interpolate
    String value();
  }

  /**
   * Option to update the root logger level. For example, with PaxExam this would update the <code>
   * etc/org.ops4j.pax.logging.cfg</code> file.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface SetRootLogLevel {
    /**
     * Specifies the level for the root logger.
     *
     * <p>If the level is empty, then no configuration changes will occur.
     *
     * @return the level for the root logger
     */
    @Interpolate
    public String value();
  }

  /** Option to set a system timeout. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface SetSystemTimeout {
    /**
     * Specifies the time unit for the timeout value (defaults to milliseconds).
     *
     * @return the time unit for the timeout value
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * Specifies the amount of time in the specified units for the system timeout.
     *
     * @return the amount of time in the specified units for the system timeout
     */
    long value();
  }

  /**
   * Option to delete the test directories (a.k.a. runtime folder) after a test is over. By default
   * the Dominion framework will delete the runtime folder.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface DeleteRuntimeFolder {
    /**
     * Specifies whether to delete the runtime folder or not (defaults to true).
     *
     * @return <code>true</code> to delete the runtime folder; <code>false</code> to keep it
     */
    boolean value() default true;
  }

  /**
   * Option to keep the test directories (a.k.a. runtime folder) after a test is over for later
   * evaluation. By default the Dominion framework will delete the runtime folder.
   */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface KeepRuntimeFolder {
    /**
     * Specifies whether to keep the runtime folder or not (defaults to true).
     *
     * @return <code>true</code> to keep the runtime folder; <code>false</code> to delete it
     */
    boolean value() default true;
  }

  /**
   * Option to keep the test directories (a.k.a. runtime folder) after a test is over for later
   * evaluation if the system property {@link #PROPERTY_KEY} is not defined or defined and set to
   * <code>true</code>. By default the Dominion framework will not delete the runtime folder if this
   * annotation is used. It will be deleted if the system property {@link #PROPERTY_KEY} is set to
   * <code>false</code>.
   */
  @Conditions.BooleanSystemProperty(
    value = EnableKeepingRuntimeFolder.PROPERTY_KEY,
    defaultsTo = true
  )
  @KeepRuntimeFolder
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface EnableKeepingRuntimeFolder {
    public static final String PROPERTY_KEY = "keepRuntimeFolder";
  }

  /** Option to clean all caches. By default all caches will be cleaned. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface CleanCaches {
    /**
     * Specifies whether to clean all caches or not (defaults to true).
     *
     * @return <code>true</code> to clean all caches; <code>false</code> to keep them
     */
    boolean value() default true;
  }

  /** Option to keep all caches. By default all caches will be preserved. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface KeepCaches {
    /**
     * Specifies whether to keep all caches or not (defaults to true).
     *
     * @return <code>true</code> to keep all caches; <code>false</code> to clean them
     */
    boolean value() default true;
  }

  /**
   * Option which enables remote debugging for the container. The debug port will be dynamically
   * generated. The container's JVM will be suspended awaiting a debugger's connection if the system
   * property {@link #PROPERTY_KEY} is defined and set to <code>true</code>. By default it will not
   * suspend itself to wait for a debug connection.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface EnableRemoteDebugging {
    public static final String PROPERTY_KEY = "remote.debug";
  }

  /**
   * Option that ensures that information about the overridden location of the local Maven
   * repository will be passed to the container and used by the driver when resolving maven
   * artifacts.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface OverrideAndPropagateMavenLocalRepository {
    /**
     * Specifies the overridden location of the local Maven repository.
     *
     * @return the overridden location of the local Maven repository
     */
    @Interpolate
    String value();
  }

  /**
   * Option that ensures that information about the overridden location of the Maven settings file
   * will be passed to the container and used by the driver when resolving maven artifacts.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface OverrideAndPropagateMavenSettings {
    /**
     * Specifies the overridden location of the local Maven settings.
     *
     * @return the overridden location of the local Maven settings
     */
    @Interpolate
    String value();
  }

  /**
   * Option that ensures that all repositories that are configured in maven settings.xml file in
   * active profiles are searchable for maven artifacts for both the container and the driver.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface PropagateMavenRepositoriesFromActiveProfiles {}

  /**
   * Option to add new maven repositories to be searchable for maven artifacts for both the
   * container and the driver.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(AddMavenRepositories.class)
  public @interface AddMavenRepository {
    /**
     * Specifies the maven repository(ies) to add to the set of repositories that can be consulted
     * for retrieving maven artifacts.
     *
     * <p>Usually repositories (not local repository) contains either releases or snapshot
     * artifacts. In order to speed up the artifact locations discovery you can mark the repository
     * with the type of artifacts it contains. This is similar with setting the <code>
     * release/enabled</code> or <code>snapshots/enabled</code> tags in <code>pom.xml</code> or
     * <code>settings.xml</code>.
     *
     * <p>By default (no marking) the repositories are considered as containing only releases (same
     * behavior as Maven). You can alter this default behavior by adding the following to repository
     * urls (not case sensitive):
     *
     * <p>to enable snapshots - add <code>@snapshots</code> to disable releases - add <code>
     * @noreleases</code>
     *
     * <p>An <code>@id</code> is required for all URLs otherwise it will be ignored (e.g., <code>
     * http://repository.ops4j.org/mvn-snapshots@snapshots@id=ops4j-snapshot</code>). If repository
     * access requests authentication the user name and password must be specified in the repository
     * URL as for example <code>http://user:password@repository.ops4j.org/maven2</code>.
     *
     * @return the maven repository url(s) to add to the set of repositories that can be consulted
     */
    @Interpolate
    String[] value();
  }

  /** Options for adding a new local user or replacing an existing user. */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(AddLocalUsers.class)
  public @interface AddLocalUser {
    /**
     * Specifies the unique user id.
     *
     * @return the unique user id
     */
    @Interpolate
    String userId();

    /**
     * Specifies the password for the user.
     *
     * @return the password for the user
     */
    @Interpolate
    String password();

    /**
     * Specifies optional roles to be added to the user (see {@link UserRoles} for known roles).
     *
     * <p><i>Note:</i> At least one role or group must be specified.
     *
     * @return optional roles to be added to the user
     */
    @Interpolate
    String[] roles() default NOT_DEFINED;

    /**
     * Specifies optional groups to be added to the user.
     *
     * <p><i>Note:</i> At least one role or group must be specified.
     *
     * @return optional groups to be added to the user
     */
    @Interpolate
    String[] groups() default NOT_DEFINED;
  }

  /** Options for adding a new local group or replacing an existing group. */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(AddLocalGroups.class)
  public @interface AddLocalGroup {
    /**
     * Specifies the unique group id.
     *
     * @return the unique group id
     */
    @Interpolate
    String groupId();

    /**
     * Specifies roles to be added to the group (see {@link UserRoles} for known roles).
     *
     * @return roles to be added to the group
     */
    @Interpolate
    String[] roles();
  }

  /**
   * Option to grant permission(s) to a given codebase.
   *
   * <p><i>Note:</i> Dominion doesn't provide extension support for this option. Therefore unless an
   * extension point for this option is provided by a registered {@link Option.Factory}, any
   * references will be ignored. This is because security permissions are provided to system in
   * different ways. DDF, for example, supports them in a directory named <code>default/xxxx.policy
   * </code> in a format defined by ProGrade. A different application might be expecting something
   * completely different. from somewhere
   *
   * <p><i>Note:</i> If none of {@link #codebase} or {@link #artifact} are specified, it will
   * default to the artifact defined by the maven project where this annotation is used.
   */
  @Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Repeatable(GrantPermissions.class)
  public @interface GrantPermission {
    /**
     * Specifies the maven urls for artifacts for which we are granting the permission(s).
     *
     * @return the maven urls for artifacts we are granting the permission(s)
     */
    MavenUrl[] artifact() default
        @MavenUrl(groupId = Options.NOT_DEFINED, artifactId = Options.NOT_DEFINED);

    /**
     * Specifies the codebases for which we are granting the permission(s).
     *
     * @return the codebases we are granting the permission(s)
     */
    @Interpolate
    String[] codebase() default Options.NOT_DEFINED;

    /**
     * Specifies the permission(s) to be granted to the specified codebase.
     *
     * @return the set of permission(s) to be granted
     */
    Permission[] permission();
  }

  /** This interface is defined purely to provide scoping. */
  public interface Repeatables {
    /** Defines several {@link UpdateFile} annotations. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @interface UpdateFiles {
      UpdateFile[] value();
    }

    /** Defines several {@link ReplaceFile} annotations. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @interface ReplaceFiles {
      ReplaceFile[] value();
    }

    /** Defines several {@link UpdateConfigProperty} annotations. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @interface UpdateConfigProperties {
      UpdateConfigProperty[] value();
    }

    /** Defines several {@link RemoveConfigProperty} annotations. */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @interface RemoveConfigProperties {
      RemoveConfigProperty[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link SetSystemProperty} annotations. */
    public @interface SetSystemProperties {
      SetSystemProperty[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link PropagateSystemProperty} annotations. */
    public @interface PropagateSystemProperties {
      PropagateSystemProperty[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link SetFrameworkProperty} annotations. */
    public @interface SetFrameworkProperties {
      SetFrameworkProperty[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link Environment} annotations. */
    public @interface Environments {
      Environment[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link VMOption} annotations. */
    public @interface VMOptions {
      VMOption[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link SetLogLevel} annotations. */
    public @interface SetLogLevels {
      SetLogLevel[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link Options.SetLogLevels} annotations. */
    public @interface SetLogLevelss {
      Options.SetLogLevels[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link AddMavenRepository} annotations. */
    public @interface AddMavenRepositories {
      AddMavenRepository[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link AddLocalUser} annotations. */
    public @interface AddLocalUsers {
      AddLocalUser[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link AddLocalGroup} annotations. */
    public @interface AddLocalGroups {
      AddLocalGroup[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    /** Defines several {@link GrantPermission} annotations. */
    public @interface GrantPermissions {
      GrantPermission[] value();
    }
  }

  private Options() {}
}

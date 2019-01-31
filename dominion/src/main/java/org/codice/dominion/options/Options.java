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
import org.codice.dominion.interpolate.Interpolate;

/**
 * This class defines annotations that can be used to configure containers. It is solely used for
 * scoping.
 */
public class Options {
  /** Option to install the Dominion driver specific configuration. */
  @Option.Annotation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface Install {}

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

  /** This interface is defined purely to provide scoping. */
  public interface Repeatables {
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
  }

  private Options() {}
}

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
package org.codice.pax.exam.service;

/** General exception thrown when a service operation fails. */
public class ServiceException extends RuntimeException {
  /**
   * Creates a new service exception with the specified message.
   *
   * @param message the error message
   */
  public ServiceException(String message) {
    super(message);
  }

  /**
   * Creates a new service exception with the specified message.
   *
   * @param cause the cause for this exception
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new service exception with the specified message and cause.
   *
   * @param message the error message
   * @param cause the cause for this exception
   */
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}

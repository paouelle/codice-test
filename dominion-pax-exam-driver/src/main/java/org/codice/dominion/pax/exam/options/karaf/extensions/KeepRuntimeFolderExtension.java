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
package org.codice.dominion.pax.exam.options.karaf.extensions;

import org.codice.dominion.options.karaf.KarafOptions.KeepRuntimeFolder;
import org.codice.dominion.pax.exam.options.PaxExamOption.Extension;
import org.codice.dominion.resources.ResourceLoader;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;

/** Extension point for the {@link KeepRuntimeFolder} option annotation. */
public class KeepRuntimeFolderExtension implements Extension<KeepRuntimeFolder> {
  @Override
  public Option[] options(
      KeepRuntimeFolder annotation, Class<?> testClass, ResourceLoader resourceLoader) {
    // by default Karaf deletes the runtime folder, so do nothing if we should delete it
    return annotation.value() ? new Option[] {KarafDistributionOption.keepRuntimeFolder()} : null;
  }
}

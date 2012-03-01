/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.fusesource.fabric.api.Container;
import org.fusesource.fabric.api.Version;
import org.fusesource.fabric.commands.support.FabricCommand;

@Command(name = "version-delete", scope = "fabric", description = "Delete a version")
public class VersionDelete extends FabricCommand {

    @Argument(index = 0, name = "version", description = "The version to delete", required = true, multiValued = false)
    private String versionName;

    @Override
    protected Object doExecute() throws Exception {
        Version version = fabricService.getVersion(versionName);
        if (version == null) {
            throw new IllegalArgumentException("Cannot find version: " + versionName);
        }
        StringBuilder sb = new StringBuilder();
        for (Container container : fabricService.getContainers()) {
            if (version.equals(container.getVersion())) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(container.getId());
            }
        }
        if (sb.length() > 0) {
            throw new IllegalArgumentException("Version " + versionName + " is still in used by the following containers: " + sb.toString());
        }
        version.delete();
        return null;
    }
}
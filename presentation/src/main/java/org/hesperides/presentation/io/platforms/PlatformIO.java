/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;

@Value
public class PlatformIO {

    @SerializedName("platform_name")
    String platformName;
    @SerializedName("application_name")
    String applicationName;
    @SerializedName("application_version")
    String applicationVersion;
    @SerializedName("production")
    boolean isProductionPlatform;
    List<ModuleIO> modules;
    @SerializedName("version_id")
    Long versionId;

    @Value
    public static class ModuleIO {

        Long id;
        String name;
        String version;
        @SerializedName("working_copy")
        boolean isWorkingCopy;
        @SerializedName("properties_path")
        String propertiesPath;
        String path;
        List<InstanceIO> instances;

        @Value
        public static class InstanceIO {

            String name;
            @SerializedName("key_values")
            List<KeyValueIO> keyValues;

            @Value
            public static class KeyValueIO {
                String name;
                String value;
            }
        }
    }
}

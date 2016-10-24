/**
 * COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved.
 * <p>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dchq.docker.volume.driver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Intesar Mohammed
 */
public class CapabilitiesResponse extends Base {

    @JsonProperty("Capabilities")
    protected Capability capabilities = new Capability();

    public Capability getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Capability capabilities) {
        this.capabilities = capabilities;
    }

    class Capability {

        @JsonProperty("Scope")
        private String scope = "global";

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}

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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * @author Intesar Mohammed
 */
public class BaseRequest extends Base {

    protected Map<String, Object> props = new HashedMap();

    @JsonAnyGetter
    public Map<String, Object> getProps() {
        return props;
    }

    @JsonAnySetter
    public void setProp(String name, Object value) {
        props.put(name, value);
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }
}


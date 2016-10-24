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

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author Intesar Mohammed
 */
public class Base implements Serializable {
    private static long serialVersionUID = 1L;

    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            Class<?> objClass = this.getClass();

            Field[] fields = objClass.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                Object value = field.get(this);

                if (value != null) {
                    sb.append(name + ": " + value.toString() + "\n");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

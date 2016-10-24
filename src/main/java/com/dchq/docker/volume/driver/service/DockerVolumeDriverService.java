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

package com.dchq.docker.volume.driver.service;

import com.dchq.docker.volume.driver.dto.*;

/**
 * @author Intesar Mohammed
 */
public interface DockerVolumeDriverService {

    /**
     * Handshake API
     *
     * @return
     */
    public ActivateResponse activate();

    /**
     * Get the list of capabilities the driver supports.
     * The driver is not required to implement this endpoint, however in such cases the default values will be taken.
     *
     * @return
     */
    public CapabilitiesResponse capabilities();

    /**
     * Instruct the plugin that the user wants to create a volume, given a user specified volume name.
     * The plugin does not need to actually manifest the volume on the filesystem yet (until Mount is called).
     * Opts is a map of driver specific options passed through from the user request.
     *
     * @param request
     * @return
     */
    public BaseResponse create(CreateRequest request);

    /**
     * Delete the specified volume from disk.
     * This request is issued when a user invokes docker rm -v to remove volumes associated with a container.
     *
     * @param request
     * @return
     */
    public BaseResponse remove(RemoveRequest request);

    /**
     * Get the volume info.
     *
     * @param request
     * @return
     */
    public GetResponse get(GetRequest request);

    /**
     * Docker needs reminding of the path to the volume on the host.
     *
     * @param request
     * @return
     */
    public MountResponse path(PathRequest request);

    /**
     * Get the list of volumes registered with the plugin.
     *
     * @return
     */
    public ListResponse list();

    /**
     * Docker requires the plugin to provide a volume, given a user specified volume name.
     * This is called once per container start.
     * If the same volume_name is requested more than once,
     * the plugin may need to keep track of each new mount request and provision at the first mount request and deprovision at the last corresponding unmount request.
     * <p>
     * ID is a unique ID for the caller that is requesting the mount.
     *
     * @param request
     * @return
     */
    public MountResponse mount(MountRequest request);

    /**
     * Indication that Docker no longer is using the named volume.
     * This is called once per container stop.
     * Plugin may deduce that it is safe to deprovision it at this point.
     *
     * @param request
     * @return
     */
    public BaseResponse unmount(MountRequest request);
}

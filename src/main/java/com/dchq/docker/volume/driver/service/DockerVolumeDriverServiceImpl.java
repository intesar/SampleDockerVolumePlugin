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


import com.dchq.docker.volume.driver.adaptor.VolumeAdaptor;
import com.dchq.docker.volume.driver.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Intesar Mohammed
 * @see DockerVolumeDriverService
 */
@Service
public class DockerVolumeDriverServiceImpl implements DockerVolumeDriverService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected VolumeAdaptor adaptor;

    @Override
    public ActivateResponse activate() {
        return new ActivateResponse();
    }

    @Override
    public CapabilitiesResponse capabilities() {
        return new CapabilitiesResponse();
    }

    @Override
    public BaseResponse create(CreateRequest request) {
        return adaptor.create(request);
    }

    @Override
    public BaseResponse remove(RemoveRequest request) {
        return adaptor.remove(request);
    }

    @Override
    public MountResponse mount(MountRequest request) {
        return adaptor.mount(request);
    }

    @Override
    public BaseResponse unmount(MountRequest request) {
        return new BaseResponse();
    }

    @Override
    public GetResponse get(GetRequest request) {
        return adaptor.get(request);
    }

    @Override
    public MountResponse path(PathRequest request) {
        return adaptor.path(request);
    }

    @Override
    public ListResponse list() {
        return adaptor.list();
    }


}

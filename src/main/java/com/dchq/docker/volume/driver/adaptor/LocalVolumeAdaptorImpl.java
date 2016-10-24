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

package com.dchq.docker.volume.driver.adaptor;


import com.dchq.docker.volume.driver.dto.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @author Intesar Mohammed
 * @see VolumeAdaptor
 * <p>
 * This class implements a basic Local Volume Driver capabilites.
 */
@Component
public class LocalVolumeAdaptorImpl implements VolumeAdaptor {

    final Logger logger = LoggerFactory.getLogger(getClass());
    String TMP_LOC = System.getProperty("java.io.tmpdir");

    public LocalVolumeAdaptorImpl() {
        try {
            File file = new File(TMP_LOC, "/volumes");
            FileUtils.forceMkdir(file);

            this.TMP_LOC = file.getAbsolutePath();

        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        logger.info("Temp directory location [{}]", TMP_LOC);
    }

    @Override
    public BaseResponse create(CreateRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            File file = new File(new File(TMP_LOC), request.getName());
            FileUtils.forceMkdir(file);
            logger.info("Created Volume [{}] on path [{}]", file.getName(), file.getAbsoluteFile());

        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public BaseResponse remove(RemoveRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            File file = new File(new File(TMP_LOC), request.getName());
            FileUtils.forceDelete(file);
            logger.info("Removed Volume [{}] on path [{}]", file.getName(), file.getAbsoluteFile());
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public MountResponse mount(MountRequest request) {
        MountResponse response = new MountResponse();
        try {
            File file = new File(new File(TMP_LOC), request.getName());
            if (file.isDirectory()) {
                response.setMountpoint(file.getAbsolutePath());
            }
            logger.info("Mounted Volume [{}] on path [{}]", file.getName(), file.getAbsoluteFile());
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public BaseResponse unmount(MountRequest request) {
        logger.info("Volume [{}] unmounted...", request.getName());
        return new BaseResponse();
    }

    @Override
    public GetResponse get(GetRequest request) {
        GetResponse response = new GetResponse();
        try {
            File directory = new File(TMP_LOC, request.getName());
            if (directory.isDirectory()) {
                Volume vol = new Volume();
                vol.setName(request.getName());
                vol.getStatus().put("state", "connected");
                vol.setMountpoint(directory.getAbsolutePath());
                response.setVolume(vol);
            } else {
                response.setErr(String.format("No volume exists with the name [%s]", request.getName()));
            }
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public MountResponse path(PathRequest request) {
        MountResponse response = new MountResponse();
        try {
            File directory = new File(TMP_LOC, request.getName());
            if (directory.isDirectory()) {
                response.setMountpoint(FileUtils.getTempDirectoryPath());
            } else {
                response.setErr(String.format("No volume exists with the name [%s]", request.getName()));
            }
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    public ListResponse list() {
        ListResponse response = new ListResponse();
        try {
            File directory = new File(TMP_LOC);
            File[] fList = directory.listFiles();
            for (File file : fList) {
                if (file.isDirectory()) {
                    Volume vol = new Volume();
                    vol.setName(file.getName());
                    vol.getStatus().put("state", "connected");
                    vol.setMountpoint(file.getAbsolutePath());
                    response.getVolumes().add(vol);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.setErr(e.getLocalizedMessage());
        }
        return response;
    }


}

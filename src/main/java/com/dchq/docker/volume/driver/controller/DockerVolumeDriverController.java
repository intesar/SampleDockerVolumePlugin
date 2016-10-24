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

package com.dchq.docker.volume.driver.controller;


import com.dchq.docker.volume.driver.dto.*;
import com.dchq.docker.volume.driver.service.DockerVolumeDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Volume Controller
 *
 * @author Intesar Mohammed
 */

@RestController
// without this "application/vnd.docker.plugins.v*.*+json" you'll get 406 error.
@RequestMapping(consumes = MediaType.ALL_VALUE, produces = {"application/vnd.docker.plugins.v1.2+json","application/vnd.docker.plugins.v1.3+json"})
public class DockerVolumeDriverController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    final static public String ACTIVATE = "/Plugin.Activate";
    final static public String CAPABILITIES = "/VolumeDriver.Capabilities";
    final static public String CREATE = "/VolumeDriver.Create";
    final static public String MOUNT = "/VolumeDriver.Mount";
    final static public String UNMOUNT = "/VolumeDriver.Unmount";
    final static public String GET = "/VolumeDriver.Get";
    final static public String LIST = "/VolumeDriver.List";
    final static public String PATH = "/VolumeDriver.Path";
    final static public String REMOVE = "/VolumeDriver.Remove";

    @Autowired
    protected DockerVolumeDriverService service;

    @Autowired
    protected CustomConverter converter;

    @RequestMapping(value = ACTIVATE, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<ActivateResponse> activate() {
        logger.info("Received [{}] request...", ACTIVATE);
        ActivateResponse response = service.activate();
        logger.info("Sending [{}]", response);
        return new ResponseEntity<ActivateResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = CAPABILITIES, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<CapabilitiesResponse> capabilities() {
        logger.info("Received [{}] request...", CAPABILITIES);
        CapabilitiesResponse response = service.capabilities();
        logger.info("Sending [{}]", response);
        return new ResponseEntity<CapabilitiesResponse>(response, HttpStatus.OK);
    }


    @RequestMapping(value = CREATE, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<BaseResponse> create(@RequestBody String request) {
        logger.info("Received [{}] request...", CREATE);
        logger.info("Request body [{}]", request);
        BaseResponse response = service.create(converter.convertToCreateRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<BaseResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = REMOVE, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<BaseResponse> remove(@RequestBody String request) {
        logger.info("Received [{}] request...", REMOVE);
        logger.info("Request body [{}]", request);
        BaseResponse response = service.remove(converter.convertToRemoveRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<BaseResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = MOUNT, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<MountResponse> mount(@RequestBody String request) {
        logger.info("Received [{}] request...", MOUNT);
        logger.info("Request body [{}]", request);
        MountResponse response = service.mount(converter.convertToMountRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<MountResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = UNMOUNT, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<BaseResponse> unmount(@RequestBody String request) {
        logger.info("Received [{}] request...", UNMOUNT);
        logger.info("Request body [{}]", request);
        BaseResponse response = service.unmount(converter.convertToMountRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<BaseResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = GET, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<GetResponse> get(@RequestBody String request) {
        logger.info("Received [{}] request...", GET);
        logger.info("Request body [{}]", request);
        GetResponse response = service.get(converter.convertToGetRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<GetResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = LIST, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<ListResponse> list() {
        logger.info("Received [{}] request...", LIST);
        ListResponse response = service.list();
        logger.info("Sending [{}]", response);
        return new ResponseEntity<ListResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = PATH, method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<MountResponse> list(@RequestBody String request) {
        logger.info("Received [{}] request...", PATH);
        logger.info("Request body [{}]", request);
        MountResponse response = service.path(converter.convertToPathRequest(request));
        logger.info("Sending [{}]", response);
        return new ResponseEntity<MountResponse>(response, HttpStatus.OK);
    }

}
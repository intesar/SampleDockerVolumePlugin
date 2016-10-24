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

import com.dchq.docker.volume.driver.dto.CreateRequest;
import com.dchq.docker.volume.driver.dto.MountRequest;
import com.dchq.docker.volume.driver.dto.PathRequest;
import com.dchq.docker.volume.driver.dto.RemoveRequest;
import com.dchq.docker.volume.driver.main.Application;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Intesar Mohammed
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
public class DockerVolumeDriverControllerIntegrationTests {

    final Logger logger = LoggerFactory.getLogger(getClass());
    final String baseUrl = "";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void activateTest() {
        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.ACTIVATE, null, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Implements\":[\"VolumeDriver\"]}");
        logger.info("Response [{}]", response);
    }

    @Test
    public void capabilitiesTest() {
        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CAPABILITIES, null, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Capabilities\":{\"Scope\":\"global\"}}");
        logger.info("Response [{}]", response);
    }

    @Test
    public void createTest() {
        CreateRequest request = new CreateRequest();
        request.setName(RandomStringUtils.randomAlphabetic(6));
        request.getOpts().put("size", "5g");

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");
        logger.info("Response [{}]", response);
    }

    @Test
    public void removeTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");

        RemoveRequest removeRequest = new RemoveRequest();
        removeRequest.setName(name);

        String removeResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.REMOVE, request, String.class);
        Assert.assertNotNull(removeResponse);
        Assert.assertEquals(removeResponse, "{\"Err\":\"\"}");
        logger.info("Response [{}]", removeResponse);
    }

    @Test
    public void mountTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");

        MountRequest mountRequest = new MountRequest();
        mountRequest.setName(name);

        String mountResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.MOUNT, mountRequest, String.class);
        Assert.assertNotNull(mountResponse);
        Assert.assertTrue(StringUtils.contains(mountResponse, "\"Err\":\"\""));
        logger.info("Response [{}]", mountResponse);
    }

    @Test
    public void unmountTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");

        MountRequest unmountRequest = new MountRequest();
        unmountRequest.setName(name);

        String unmountResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.UNMOUNT, unmountRequest, String.class);
        Assert.assertNotNull(unmountResponse);
        Assert.assertTrue(StringUtils.contains(unmountResponse, "\"Err\":\"\""));
        logger.info("Response [{}]", unmountResponse);
    }

    @Test
    public void pathTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");

        PathRequest pathRequest = new PathRequest();
        pathRequest.setName(name);

        String pathResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.PATH, pathRequest, String.class);
        Assert.assertNotNull(pathResponse);
        Assert.assertTrue(StringUtils.contains(pathResponse, "\"Err\":\"\""));
        logger.info("Response [{}]", pathResponse);
    }

    @Test
    public void getTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");

        PathRequest getRequest = new PathRequest();
        getRequest.setName(name);

        String getResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.GET, getRequest, String.class);
        Assert.assertNotNull(getResponse);
        Assert.assertTrue(StringUtils.contains(getResponse, "\"Err\":\"\""));
        logger.info("Response [{}]", getResponse);
    }

    @Test
    public void listTest() {

        String name = RandomStringUtils.randomAlphabetic(6);
        CreateRequest request = new CreateRequest();
        request.setName(name);

        String response = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.CREATE, request, String.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response, "{\"Err\":\"\"}");


        String listResponse = restTemplate.postForObject(baseUrl + DockerVolumeDriverController.LIST, null, String.class);
        Assert.assertNotNull(listResponse);
        Assert.assertTrue(StringUtils.contains(listResponse, "\"Err\":\"\""));
        logger.info("Response [{}]", listResponse);
    }


}

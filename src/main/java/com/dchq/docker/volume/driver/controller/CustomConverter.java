package com.dchq.docker.volume.driver.controller;

import com.dchq.docker.volume.driver.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Intesar Mohammed
 *         This is workaround for spring mvc content-type missing header.
 */
@Component
public class CustomConverter {

    final Logger logger = LoggerFactory.getLogger(getClass());
    protected ObjectMapper mapper = new ObjectMapper();

    public CreateRequest convertToCreateRequest(String val) {
        CreateRequest req = new CreateRequest();
        try {
            req = mapper.readValue(val, CreateRequest.class);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return req;
    }

    public RemoveRequest convertToRemoveRequest(String val) {
        RemoveRequest req = new RemoveRequest();
        try {
            req = mapper.readValue(val, RemoveRequest.class);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return req;
    }

    public GetRequest convertToGetRequest(String val) {
        GetRequest req = new GetRequest();
        try {
            req = mapper.readValue(val, GetRequest.class);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return req;
    }

    public MountRequest convertToMountRequest(String val) {
        MountRequest req = new MountRequest();
        try {
            req = mapper.readValue(val, MountRequest.class);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return req;
    }

    public PathRequest convertToPathRequest(String val) {
        PathRequest req = new PathRequest();
        try {
            req = mapper.readValue(val, PathRequest.class);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return req;
    }
}

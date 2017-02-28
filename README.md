
# Java based implementation of Docker Volume Plugin/Driver
Implements sample local volume driver based on docker plugin architecture.

1.  [Tech Stack] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/README.md#technology-stack)
2.  [Understanding Docker Plugin Spec] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/README.md#14-understanding-docker-plugin-spec)
3.  [Endpoint Implementation] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/README.md#24-endpoint-implementation)
4.  [How to build the plugin] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/README.md#34-how-to-build-the-plugin)
5.  [How to install/debug the plugin] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/README.md#44-how-to-installdebug-the-plugin)
5.  [Recommendations/Learning] ()


## Technology Stack
* Docker version v1.13+ (and v1.12+ legacy driver)
* Java 1.8+
* OS: Linux
* Gradle 2.4+

### About us
* Free SaaS Product [HyperCloud] (https://dchq.io)
* Learn more about HyperGrid [HyperGrid] (http://hypergrid.com/)
* Request a demo [here] (http://pages.hypergrid.com/request_a_demo_hypergrid.html)
* Contact us [here] (http://hypergrid.com/about/contact/)


## 1/4. Understanding Docker Plugin Spec

* Reference
  * https://docs.docker.com/engine/extend/plugin_api/
  * https://docs.docker.com/engine/extend/plugins_volume/

### Requires implementing the following endpoints.
1. /Plugin.Activate
  * Request body: No
  * Response: { "Implements": ["VolumeDriver"] }

2. /VolumeDriver.Capabilities
  * Request body: No
  * Response: { "Capabilities": { "Scope": "global" } }

3. /VolumeDriver.Create
  * Request body: { "Name": "volume_name", "Opts": {} } 
  * Response: { "Err": "" }
  * Notes: Should only create the volume. At this point volume shoudn't be attached/mounted to host/VM.

4. /VolumeDriver.Mount
  * Request body: { "Name": "volume_name", "ID": "b87d7442095999a92b65b3d9691e697b61713829cc0ffd1bb72e4ccd51aa4d6c" }
  * Response: { "Err": "" }
  * Notes: Attach and mount the volume and remember the containeri-id which is using it. If more than one container mounts the volume then this endpoint is called that many times but you don't have to attach/mount more than one, but you still need to remember how many containers are using it.

5. /VolumeDriver.Unmount
  * Request body: { "Name": "volume_name", "ID": "b87d7442095999a92b65b3d9691e697b61713829cc0ffd1bb72e4ccd51aa4d6c" }
  * Response: { "Err": "" }
  * Notes: You should detach/unmount only if no other containers are using it.

6. /VolumeDriver.Get
  * Request body: { "Name": "volume_name"} 
  * Response: { "Volume": { "Name": "volume_name", "Mountpoint": "/path/to/directory/on/host", "Status": {} }, "Err": "" }
  * Notes: Mountpoing is optional. If the volume is only created then it doesn't have a mountpoint.

7. /VolumeDriver.List
  * Request body: No
  * Response: { "Volumes": [ { "Name": "volume_name", "Mountpoint": "/path/to/directory/on/host" } ], "Err": "" }
  * Notes: Mountpoing is optional.

8. /VolumeDriver.Path
  * Request body: { "Name": "volume_name"} 
  * Response: { "Mountpoint": "/path/to/directory/on/host", "Err": "" }
  * Notes: Mountpoing is optional.

9. /VolumeDriver.Remove
  * Request body: { "Name": "volume_name" }
  * Response: { "Err": "" }

### How Docker Volume orchestration works.
  * docker volume ls
    * /VolumeDriver.List
  * docker volume create 
    * /VolumeDriver.Get (If volume found it simply return the volume. ** Ideally docker should throw error ** )
    * /VolumeDriver.Create
  * docker volume inspect
    * /VolumeDriver.Get
  * docker volume rm
    * /VolumeDriver.Remove
  * docker run/start
    * /VolumeDriver.Mount    
  * docker stop 
    * /VolumeDriver.Unmount    
    
## 2/4 Endpoint Implementation 
  * (Legacy) [Java Controller code] (https://github.com/intesar/SampleDockerVolumePlugin/blob/master/src/main/java/com/dchq/docker/volume/driver/controller/DockerVolumeDriverController.java)
  * Sample java code implements all required endpoints.
  ```
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
 * @author Shoukath Ali
 * @author Luqman Shareef
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
  ```

### Endpoint Implementation (Unix Sockets)
  * https://github.com/jnr/jnr-unixsocket (Java doesn't natively support Unix Sockets so we used this framework)
  * Here is our Sample Unix-Socket based implementation
    * [Github repository] (https://github.com/intesar/SampleDockerVolumePluginUnixSocket)
    * [Code] ( https://github.com/intesar/SampleDockerVolumePluginUnixSocket/blob/master/src/main/java/com/dchq/docker/volume/driver/controller/SocketController.java)
  * Sample code implements unix-socket. Note only this approach works with latest spec.
  ```
  package com.dchq.docker.volume.driver.controller;

import com.dchq.docker.volume.driver.dto.Base;
import com.dchq.docker.volume.driver.dto.BaseResponse;
import com.dchq.docker.volume.driver.service.DockerVolumeDriverService;
import jnr.enxio.channels.NativeSelectorProvider;
import jnr.unixsocket.UnixServerSocket;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Intesar Mohammed
 * @author Shoukath Ali
 * @author Luqman Shareef
 */
public class SocketController {

    final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    static DockerVolumeDriverService service;
    static CustomConverter converter;

    java.io.File path = null;

    //String SOCKET_PATH = "/run/docker/plugins/hypercloud.sock";

    public void loadSocketListener(final String SOCKET_PATH, DockerVolumeDriverService service, CustomConverter converter) {

        this.service = service;
        this.converter = converter;

        try {
            logger.info("Registering socket [{}]", SOCKET_PATH);
            path = new java.io.File(SOCKET_PATH);
            //FileUtils.forceMkdirParent(path);
            path.deleteOnExit();
            UnixSocketAddress address = new UnixSocketAddress(path);
            UnixServerSocketChannel channel = UnixServerSocketChannel.open();

            try {
                Selector sel = NativeSelectorProvider.getInstance().openSelector();
                channel.configureBlocking(false);
                channel.socket().bind(address);
                logger.debug("channel.register begin");
                channel.register(sel, SelectionKey.OP_ACCEPT, new ServerActor(channel, sel));
                logger.debug("channel.register end");
                while (sel.select() >= 0) {
                    logger.debug("Selector > 0");
                    Set<SelectionKey> keys = sel.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    boolean running = false;
                    boolean cancelled = false;
                    while (iterator.hasNext()) {
                        logger.debug("SelectionKey.hasNext");
                        SelectionKey k = iterator.next();
                        Actor a = (Actor) k.attachment();
                        if (a.rxready(path)) {
                            running = true;
                        } else {
                            k.cancel();
                            cancelled = true;
                        }
                        iterator.remove();
                    }
                    if (!running && cancelled) {
                        logger.info("No Actors Running any more");
                        channel.register(sel, SelectionKey.OP_ACCEPT, new ServerActor(channel, sel));
                        //break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(UnixServerSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            logger.info("UnixServer EXIT");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.deleteQuietly(path);
        }
    }

    static interface Actor {
        public boolean rxready(java.io.File path);
    }

    static final class ServerActor implements Actor {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
        private final UnixServerSocketChannel channel;
        private final Selector selector;

        public ServerActor(UnixServerSocketChannel channel, Selector selector) {
            this.channel = channel;
            this.selector = selector;
            logger.debug("ServerActor instantiated!");
        }

        public final boolean rxready(java.io.File path) {
            try {
                UnixSocketChannel client = channel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ, new ClientActor(client));
                logger.debug("ServerActor ready!");
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }

    static final class ClientActor implements Actor {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

        String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\n" + "Content-Type: application/vnd.docker.plugins.v1.2+json\r\n\r\n";

        private final UnixSocketChannel channel;

        public ClientActor(UnixSocketChannel channel) {
            this.channel = channel;
            logger.debug("ClientActor instantiated!");
        }

        public final boolean rxready(java.io.File path) {
            try {
                logger.debug("ClientActor ready!");
                ByteBuffer buf = ByteBuffer.allocate(1024);
                int n = channel.read(buf);
                UnixSocketAddress remote = channel.getRemoteSocketAddress();

                if (n > 0) {
                    // System.out.printf("Read in %d bytes from %s\n", n, remote);
                    //buf.flip();
                    //channel.write(buf);
                    String req = new String(buf.array(), 0, buf.position());
                    //System.out.print("Data From Client :" + req + "\n");
                    buf.flip();
                    Base response = null;
                    RequestWrapper request = HttpRequestParser.parse(req);

                    response = getBaseResponse(request);
                    String responseText = HTTP_RESPONSE + converter.convertFromBaseResponse(response);

                    logger.info("Response text [{}]", responseText);

                    //buf.flip();
                    ByteBuffer bb = ByteBuffer.wrap(responseText.getBytes(Charset.defaultCharset()));
                    logger.debug("bb [{}]", bb.toString());
                    channel.write(bb);

                    //channel.finishConnect();
                    channel.close();


                    return false;
                } else if (n < 0) {
                    return false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        private Base getBaseResponse(RequestWrapper request) {

            String requestType = request.getPath();
            Base response = new Base();

            switch (requestType) {
                case "/Plugin.Activate":
                    response = service.activate();
                    break;
                case "/VolumeDriver.Capabilities":
                    response = service.capabilities();
                    break;
                case "/VolumeDriver.Create":
                    response = service.create(converter.convertToCreateRequest(request.getBody()));
                    break;
                case "/VolumeDriver.Mount":
                    response = service.mount(converter.convertToMountRequest(request.getBody()));
                    break;
                case "/VolumeDriver.Unmount":
                    response = service.unmount(converter.convertToMountRequest(request.getBody()));
                    break;
                case "/VolumeDriver.Get":
                    response = service.get(converter.convertToGetRequest(request.getBody()));
                    break;
                case "/VolumeDriver.List":
                    response = service.list();
                    break;
                case "/VolumeDriver.Path":
                    response = service.path(converter.convertToPathRequest(request.getBody()));
                    break;
                case "/VolumeDriver.Remove":
                    response = service.remove(converter.convertToRemoveRequest(request.getBody()));
                    break;
            }

            if (response == null) {

                response = new BaseResponse();
                //response.setErr("Invalid Request");

            }

            return response;
        }
    }

}
  ```


## 3/4 How to build the plugin
* (Reference Doc) [https://github.com/docker/docker/blob/master/docs/extend/index.md]
* You need to wrap your plugin code in a container. So you need Dockerfile, here is one sample file.
```
FROM java:8
RUN mkdir -p /opt/dchq
RUN mkdir -p /opt/dchq/log
RUN mkdir -p /opt/dchq/config
RUN mkdir -p /opt/dchq/data
RUN touch /opt/dchq/data/mount.properties
RUN mkdir -p /run/docker/plugins /var/lib/hypercloud/volumes
COPY DCHQ-HBS-driver.jar /opt/dchq/DCHQ-HBS-driver.jar
EXPOSE 4434
#WORKDIR /opt/hbs/
#RUN java -jar /opt/dchq/DCHQ-HBS-driver.jar
ENV JAVA_OPTS=""
ENV proxy.host="https://10.0.1.12"
ENTRYPOINT ["java", "-jar", "/opt/dchq/DCHQ-HBS-driver.jar"]

```
* Build the image
```
docker build -t rootfsimage .
```
* Get id
```
id=$(docker create rootfsimage true) # id was cd851ce43a403 when the image was created
```
* Create folder structure
```
mkdir -p myplugin/rootfs
```
* export image contents into rootfs folder
```
sudo docker export "$id" | sudo tar -x -C myplugin/rootfs
```
* Delete image
```
docker rm -vf "$id"
docker rmi rootfsimage
```
* Create a config file. Sample contents
```
{
  "description": "HyperCloud Block Storage Service Plugin",
  "documentation": "https://dchq.io",
  "entrypoint": [
    "java",
    "-jar",
    "/opt/dchq/DCHQ-HBS-driver.jar"
  ],
  "Env": [
    {
      "Description": "",
      "Name": "proxy.host",
      "Settable": [
        "value"
      ],
      "Value": "https://10.0.1.12"
    }
  ],
  "interface": {
    "types": [
      "docker.volumedriver/1.0"
    ],
    "socket": "hypercloud.sock"
  },
  "Linux": {
    "Capabilities": [
      "CAP_SYS_ADMIN"
    ],
    "AllowAllDevices": true,
    "Devices": null
  },
  "mounts": [
    {
      "source": "/dev",
      "destination": "/dev",
      "type": "bind",
      "options": [
        "rbind"
      ]
    },
    {
      "source": "/usr/bin/",
      "destination": "/usr/bin/",
      "type": "bind",
      "options": [
        "rbind"
      ]
    },
    {
      "source": "/opt/dchq/config/",
      "destination": "/opt/dchq/config/",
      "type": "bind",
      "options": [
        "rbind"
      ]
    }
  ],
  "Network": {
    "Type": "host"
  },
  "PropagatedMount": "/var/lib/hypercloud/volumes",
  "User": {},
  "WorkDir": ""
}
```
  * Notes: 
    - Our plugin needs 'CAP_SYS_ADMIN' privelege 
    - PropagatedMount (/var/lib/hypercloud/volumes) is where our volumes are mounted
    - We needed access to host/vm's /dev location as well
    - Our sock file name is 'hypercloud.sock'. Make sure to create the sock file in '/run/docker/plugins/hypercloud.sock', Docker will detec from this location.
    
* Create plugin (point to the folder where config and rootfs is)
```
docker plugin create hypergrid/hypercloud:1.0 myplugin
```

* Once the plugin is created you can test or push it. Note, don't create the 'hypercloud' repository using hub (UI). When you push the plugin to non existing repo docker will create the repo and mark it as plugin repo. 
```
docker login (first time)
docker plugin push hypergrid/hypercloud:1.0 
```

## 4/4 How to install/debug the plugin

* listing plugins
```
docker plugin ls
```

* install plugins
```
docker plugin install hypergrid/hypercloud:1.5
```

* inspect plugins
```
docker plugin inspect hypergrid/hypercloud:1.5
```

* enable plugins (runs)
```
docker plugin enable hypergrid/hypercloud:1.5
```

* disable plugins (stops)
```
docker plugin disable hypergrid/hypercloud:1.5
```

* remove plugins (stops)
```
docker plugin rm -f hypergrid/hypercloud:1.5
```

* Using docker-runc to obtain shell into the plugin.
```
docker-runc list (list running docker process)
docker-runc exec -t [plugin-id] sh
```



### Sample Docker Volume commands for creating, deleting, listing, inspecting volumes

```
docker volume create --driver hypergrid/hypercloud:1.5 --name dchqvol116
```
```
docker volume inspect dchqvol116
```
```
docker volume ls | grep dchqvol
```
```
docker volume remove dchqvol116
```

```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/Plugin.Activate
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Capabilities
```

```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Create -d '{"Name":"vol-100"}'
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Mount -d '{"Name":"vol-100", "ID": "id-123"}'
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Unmount -d '{"Name":"vol-100", "ID": "id-123"}'
```

```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Get -d '{"Name":"vol-100"}'
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.List
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Path -d '{"Name":"vol-100"}'
```
```
curl  -X POST --unix-socket /tmp/hypercloud.sock http://localhost/VolumeDriver.Remove -d '{"Name":"vol-100"}'
```


### How to clone and write you're own driver implementation

* Implement this interface com.dchq.docker.volume.driver.adaptor.VolumeAdaptor.java
* Checkout the sample implementation com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java
* Comment @Component in com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java
* Checkout application.properties file for more information on enabling wire level logging and cert creation.


### Issues

* CustomConverter.java is only their because Docker doesn't send "content-type" header. The latest version of Spring doesn't like POST request with missing content-type header.



### Integration Tests  & sample code.

* com.dchq.docker.volume.driver.controller.DockerVolumeDriverControllerIntegrationTests
* API produces "application/vnd.docker.plugins.v1.2+json"


## 5 Recommendations/Learnings.
  * How unix-socket based approach is going to work on Windows?
  * Any repository created using hub/UI is tagged as image repository and pushing a plugin to this repo won't work.
  * TLS support for unix-sockets.
  * Volume create for duplicate volume name doesn't fail (bug) [https://github.com/docker/docker/issues/31407].
  * Docker expects plugin to remember how many containers are using volume. I'm not sure how this will work in cases of docker crashes/restarts.
  * No better way of managing senstive information (credentails) in plugins.

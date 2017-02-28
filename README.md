
# Java based implementation of Docker Volume Plugin/Driver
Implements sample local volume driver based on docker plugin architecture.


## Technology Stack
==================================================
* Docker version v1.13+ (and v1.12+ legacy driver)
* Java 1.8+
* OS: Linux
* Gradle 2.4+

### Free SaaS Product [DCHQ.io] (https://dchq.io)

### Learn more about HyperGrid [HyperGrid] (http://hypergrid.com/)



## 1/4. Understanding Docker Plugin Spec

* Reference
  * https://docs.docker.com/engine/extend/plugin_api/
  * https://docs.docker.com/engine/extend/plugins_volume/

* /Plugin.Activate
  * Request body: No
  * Response: { "Implements": ["VolumeDriver"] }

* /VolumeDriver.Capabilities
  * Request body: No
  * Response: { "Capabilities": { "Scope": "global" } }
  
* /VolumeDriver.Create
  * Request body: { "Name": "volume_name", "Opts": {} } 
  * Response: { "Err": "" }
  * Notes: Should only create the volume. Nor should you attach to the host/vm nor you should mount at this point.
  
* /VolumeDriver.Mount
  * Request body: { "Name": "volume_name", "ID": "b87d7442095999a92b65b3d9691e697b61713829cc0ffd1bb72e4ccd51aa4d6c" }
  * Response: { "Err": "" }
  * Notes: Attach and mount the volume and remember the containeri-id is using it. If more than one container mounts the volume then this endpoint is called that many times but you don't have to attach/mount more than one, but you still need to remember how many containers are using it.

* /VolumeDriver.Unmount
  * Request body: { "Name": "volume_name", "ID": "b87d7442095999a92b65b3d9691e697b61713829cc0ffd1bb72e4ccd51aa4d6c" }
  * Response: { "Err": "" }
  * Notes: You should detach/unmount only if no other containers are using it.


* /VolumeDriver.Get
  * Request body: { "Name": "volume_name"} 
  * Response: { "Volume": { "Name": "volume_name", "Mountpoint": "/path/to/directory/on/host", "Status": {} }, "Err": "" }
  * Notes: Mountpoing is optional. If the volume is only created then it doesn't have a mountpoint.

* /VolumeDriver.List
  * Request body: No
  * Response: { "Volumes": [ { "Name": "volume_name", "Mountpoint": "/path/to/directory/on/host" } ], "Err": "" }

* /VolumeDriver.Path
  * Request body: { "Name": "volume_name"} 
  * Response: { "Mountpoint": "/path/to/directory/on/host", "Err": "" }

* /VolumeDriver.Remove
  * Request body: { "Name": "volume_name" }
  * Response: { "Err": "" }

* How Docker Volume orchestration works.
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

### Endpoint Implementation (Unix Sockets)
  * https://github.com/jnr/jnr-unixsocket (Java doesn't natively support Unix Sockets so we used this framework)
  * Here is our Sample Unix-Socket based implementation
    * [Github repository] (https://github.com/intesar/SampleDockerVolumePluginUnixSocket)
    * [Code] ( https://github.com/intesar/SampleDockerVolumePluginUnixSocket/blob/master/src/main/java/com/dchq/docker/volume/driver/controller/SocketController.java)


## 3/4 How to build the plugin
* (Reference Doc) [https://github.com/docker/docker/blob/master/docs/extend/index.md]
* You need to wrap your plugin code in a container. So you need Dockerfile, here is one sample.
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
ENV proxy.host="https://10.0.1.12"N"
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
docker volume create --driver dchqvol --name dchqvol116
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


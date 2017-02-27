
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
==================================================
  * https://github.com/jnr/jnr-unixsocket (Java doesn't natively support Unix Sockets so we used this framework)
  * Here is our Sample Unix-Socket based implementation
    * [Github repository] (https://github.com/intesar/SampleDockerVolumePluginUnixSocket)
    * [Code] ( https://github.com/intesar/SampleDockerVolumePluginUnixSocket/blob/master/src/main/java/com/dchq/docker/volume/driver/controller/SocketController.java)

How to build this code.
==================================================
* ./gradlew clean ; ./gradlew build

How to run this code.
==================================================
* ./gradlew bootrun

How to clone and write you're own driver implementation
==================================================
* Implement this interface com.dchq.docker.volume.driver.adaptor.VolumeAdaptor.java
* Checkout the sample implementation com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java
* Comment @Component in com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java
* Checkout application.properties file for more information on enabling wire level logging and cert creation.


Issues
==================================================
* CustomConverter.java is only their because Docker doesn't send "content-type" header. The latest version of Spring doesn't like POST request with missing content-type header.



Integration Tests  & sample code.
==================================================
* com.dchq.docker.volume.driver.controller.DockerVolumeDriverControllerIntegrationTests
* API produces "application/vnd.docker.plugins.v1.2+json"


How to deploy and test
==================================================
* Copy dchqvol.json file to /etc/docker/plugins/ on the docker host.
* Download the executable "wget https://www.dropbox.com/s/ixl0gc7bdfxqtwa/SampleDockerVolumeDriver-1.0-SNAPSHOT-1477279416108.jar?dl=1 -O dchqvol.jar
* java -jar dchqvol.jar

Installing as init.d service
==================================================
* sudo ln -s /opt/dchq/dchqvol.jar /etc/init.d/dchqvol
* service dchqvol start

Installing as systemd service
==================================================
* Create this file /etc/systemd/system/dchqvol.service with below content

[Unit]
Description=dchqvol
After=syslog.target

[Service]
User=dchqvol
ExecStart=/opt/dchq/dchqvol.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target


* systemctl enable dchqvol.service


Sample Docker Volume commands for creating, deleting, listing, inspecting volumes
==================================================
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

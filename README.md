Java based implementation of Docker Volume Plugin/Driver
==============================================================
Implements creation of local volumes.


Technology Stack
==================================================
* Docker version v1.13+ (v1.12+ legacy driver)
* Java 1.8+
* OS: Linux
* Gradle 2.4+

Docke Plugin Spec
==================================================
* Reference
  * https://docs.docker.com/engine/extend/plugin_api/
  * https://docs.docker.com/engine/extend/plugins_volume/

* /Plugin.Activate
  * Request body: No
  * Response: { "Implements": ["VolumeDriver"] }

* /VolumeDriver.Capabilities
  * Request body: No
  * Response: { "Capabilities": { "Scope": "global" } }
  
* /VolumeDriver.Create -d '{"Name":"vol-100"}'
* /VolumeDriver.Mount -d '{"Name":"vol-100", "ID": "id-123"}'
* /VolumeDriver.Unmount -d '{"Name":"vol-100", "ID": "id-123"}'
* /VolumeDriver.Get -d '{"Name":"vol-100"}'
* /VolumeDriver.List
* /VolumeDriver.Path -d '{"Name":"vol-100"}'
* /VolumeDriver.Remove -d '{"Name":"vol-100"}'


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
* docker volume create --driver dchqvol --name dchqvol116
* docker volume inspect --driver dchqvol --name dchqvol116
* docker volume ls | grep dchqvol
* docker volume remove --driver dchqvol --name dchqvol116

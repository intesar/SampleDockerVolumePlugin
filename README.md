Java based implementation of Docker Volume Driver
==================================================
This implementation creates volumes in the temp directory of the host system.


Technology Stack
==================================================
* Docker version v1.12+
* Java 1.8+
* OS: MacOS, Linux or Windows
* IDE: IntelliJ or Eclipse or Netbeans
* Gradle 2.4+


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
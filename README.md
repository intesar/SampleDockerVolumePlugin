Java based implementation of Docker Volume Driver
==================================================
This implementation creates volumes in temp directory of host system.


Stack
=====
* Docker version v1.12+
* Java 1.8+
* OS: MacOS, Linux or Windows
* IDE: IntelliJ or Eclipse or Netbeans
* Gradle 2.4+


How to build this code.
========================
* ./gradlew clean ; ./gradlew build
* ./gradlew bootrun


How to clone and write you're own driver implementation
=======================================================
* Implement this interface com.dchq.docker.volume.driver.adaptor.VolumeAdaptor.java
* Checkout the sample implementation com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java
* Comment @Component in com.dchq.docker.volume.driver.adaptor.LocalVolumeAdaptorImpl.java


Issues
========================
* CustomConverter.java is only their because Docker doesn't send "content-type" header. The latest version of Spring doesn't like POST request with missing content-type header.



Integration Tests  & sample code.
==================================
* com.dchq.docker.volume.driver.controller.DockerVolumeDriverControllerIntegrationTests
* API produces "application/vnd.docker.plugins.v1.2+json"

How to deploy and test
======================
* Copy dchqvol.json file to /etc/docker/plugins/ on the docker host.
* java -jar

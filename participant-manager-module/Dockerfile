# first build common module, then run:
# export DOCKER_ID=<YourDockerID>
# docker image build --tag $DOCKER_ID/participant-manager-module . --build-arg DOCKER_ID
FROM mohankgonthina/participant_manager_common_module:latest AS maven_builder
COPY participant-manager-service /app/participant-manager-module/participant-manager-service
COPY pom.xml /app/participant-manager-module/
WORKDIR /app/participant-manager-module/
RUN mvn clean package -Pqa -Dmaven.test.skip=true
#RUN mvn package -Pqa

# to run
FROM tomcat:9.0.33-jdk11-openjdk

# Download and import cert file to keystore
RUN openssl s_client -connect 35.193.185.224:9000 </dev/null \
    | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /usr/local/openjdk-11/lib/security/gcpserverreg1_qa.cert

# Import certificate into keystore
RUN \
    cd /usr/local/openjdk-11/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias "gcpserverreg1" -file gcpserverreg1_qa.cert

COPY --from=maven_builder /app/participant-manager-module/participant-manager-service/target/participant-manager-service.war /usr/local/tomcat/webapps/
RUN chmod 755 /usr/local/tomcat/webapps/participant-manager-service.war
CMD ["catalina.sh", "run"]

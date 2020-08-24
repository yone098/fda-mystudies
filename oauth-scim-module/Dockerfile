FROM mohankgonthina/oauth_scim_common_module:latest AS maven_builder
COPY oauth-scim-service /app/oauth-scim-module/oauth-scim-service
COPY pom.xml /app/oauth-scim-module/
WORKDIR /app/oauth-scim-module/

# Download and import cert file to keystore
RUN openssl s_client -connect 35.193.185.224:9000 </dev/null \
    | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > gcpserverreg1_qa.cert

# Trust this certificate? [no]: echo yes
# RUN echo yes | keytool -importcert -file gcpserverreg1_qa.cert -alias "https://35.193.185.224" -cacerts -storepass changeit

ENV JAVA_HOME=/opt/java/openjdk/bin/java
RUN echo $JAVA_HOME
RUN java -version
RUN whereis java
RUN which java
RUN pwd
RUN ls
RUN ls $JAVA_HOME/lib/security

# COPY /app/oauth-scim-module/gcpserverreg1_qa.cert $JAVA_HOME/lib/security

RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias "https://35.193.185.224" -file gcpserverreg1_qa.cert

RUN mvn clean package -Pqa -Dmaven.test.skip=true

# Torun
FROM tomcat:9.0.33-jdk11-openjdk
COPY --from=maven_builder /app/oauth-scim-module/oauth-scim-service/target/oauth-scim-service.war /usr/local/tomcat/webapps/
RUN chmod 755 /usr/local/tomcat/webapps/oauth-scim-service.war
CMD ["catalina.sh", "run"]
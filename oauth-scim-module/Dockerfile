FROM mohankgonthina/oauth_scim_common_module:latest AS maven_builder
COPY oauth-scim-service /app/oauth-scim-module/oauth-scim-service
COPY pom.xml /app/oauth-scim-module/
WORKDIR /app/oauth-scim-module/


# Build the war file
RUN mvn clean package -Pqa -Dmaven.test.skip=true

# Torun
FROM tomcat:9.0.33-jdk11-openjdk

# Download and import cert file to keystore
RUN openssl s_client -connect 35.193.185.224:9000 </dev/null \
    | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /usr/local/openjdk-11/lib/security/gcpserverreg1_qa.cert

# Import certificate into keystore
RUN \
    cd /usr/local/openjdk-11/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias "gcpserverreg1" -file gcpserverreg1_qa.cert

COPY --from=maven_builder /app/oauth-scim-module/oauth-scim-service/target/auth-server.war /usr/local/tomcat/webapps/
RUN chmod 755 /usr/local/tomcat/webapps/auth-server.war
CMD ["catalina.sh", "run"]

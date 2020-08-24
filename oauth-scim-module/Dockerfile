FROM mohankgonthina/oauth_scim_common_module:latest AS maven_builder
COPY oauth-scim-service /app/oauth-scim-module/oauth-scim-service
COPY pom.xml /app/oauth-scim-module/
WORKDIR /app/oauth-scim-module/
RUN mvn clean package -Pqa -Dmaven.test.skip=true

# Download and import cert file to keystore
openssl s_client -connect 35.193.185.224:9000 </dev/null \
    | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > gcpserverreg1_qa.cert

keytool -importcert -file gcpserverreg1_qa.cert -alias "https://35.193.185.224" -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit

#Torun
FROM tomcat:9.0.33-jdk11-openjdk
COPY --from=maven_builder /app/oauth-scim-module/oauth-scim-service/target/oauth-scim-service.war /usr/local/tomcat/webapps/
RUN chmod 755 /usr/local/tomcat/webapps/oauth-scim-service.war
CMD ["catalina.sh", "run"]
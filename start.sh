# App: getting-started
export MY_SOLID_IDP=https://login.inrupt.com
echo $MY_SOLID_IDP
export MY_SOLID_CLIENT_ID={YOUR_CLIENT_ID}
echo $MY_SOLID_CLIENT_ID
export MY_SOLID_CLIENT_SECRET={YOUR_CLIENT_SECRET}
echo $MY_SOLID_CLIENT_SECRET
export MY_AUTH_FLOW=client_secret_basic
echo $MY_AUTH_FLOW
./mvnw spring-boot:run
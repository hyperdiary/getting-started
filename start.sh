# App: getting-started
export MY_SOLID_IDP=https://login.inrupt.com
echo $MY_SOLID_IDP
export MY_SOLID_CLIENT_ID=595d3b5f-dc30-48a7-ab59-75ddb178d352
echo $MY_SOLID_CLIENT_ID
export MY_SOLID_CLIENT_SECRET=9d35a426-6098-4d8b-8d9d-c677778b5d53
echo $MY_SOLID_CLIENT_SECRET
export MY_AUTH_FLOW=client_secret_basic
echo $MY_AUTH_FLOW
./mvnw spring-boot:run
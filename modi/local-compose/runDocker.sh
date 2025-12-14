cd ..
cd modi-discovery
docker build -t modi-discovery:latest .

cd ..
cd modi-config
docker build -t modi-config:latest .

cd ..
cd modi-gateway
docker build -t modi-gateway:latest .

cd ..
cd account-service
docker build -t account-service:latest .
# Build stage
FROM node:14 as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM node:14-alpine
WORKDIR /app
COPY --from=build /app/build ./build
RUN npm install -g serve

# copy entrypoint script and give permission for execution
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 3000

# run entrypoint script
ENTRYPOINT ["/app/entrypoint.sh"]
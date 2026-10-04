# The frontend as it ships: built once with Node, served by nginx (no Node at runtime).
#   docker build -t aifu-frontend frontend
# The api service must be reachable as http://api:8080 (the compose network); see nginx.conf.

FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# nginx without root: listens on 8080, writes only to its temp directories
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://127.0.0.1:8080/healthz || exit 1

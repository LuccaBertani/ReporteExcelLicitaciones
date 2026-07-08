# ---------- Etapa 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

# Igual que en el backend: copiamos primero solo lo necesario para instalar
# dependencias, así Docker cachea esta capa mientras no cambie el package.json.
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ---------- Etapa 2: runtime ----------
# Servimos los archivos estáticos generados (carpeta dist/) con nginx.
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

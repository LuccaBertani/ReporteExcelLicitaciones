# ---------- Etapa 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

# Igual que en el backend: copiamos primero solo lo necesario para instalar
# dependencias, así Docker cachea esta capa mientras no cambie el package.json.
COPY package*.json ./
RUN npm install

COPY . .

# La URL del backend queda fijada en el momento del build (Vite la incrusta
# en el bundle, no se puede cambiar después en runtime). En Railway se define
# como variable del servicio del frontend; en local, si no se pasa, usa el
# valor por defecto de api/config.js (http://localhost:8080/api/v1).
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ---------- Etapa 2: runtime ----------
# Servimos los archivos estáticos generados (carpeta dist/) con nginx.
# Puerto FIJO en 8080 (sin depender de la detección automática de Railway,
# que nos estaba dando problemas): en Railway, Settings → Networking →
# el "target port" del dominio tiene que ser 8080 también.
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

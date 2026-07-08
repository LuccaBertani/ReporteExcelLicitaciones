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
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Usamos un ARCHIVO TEMPLATE en vez de un nginx.conf fijo: la imagen oficial
# de nginx reemplaza automáticamente ${PORT} por el valor real al arrancar el
# contenedor. Esto hace falta porque Railway asigna un puerto dinámico
# (variable de entorno PORT) distinto en cada deploy; en Docker Compose local
# seguimos fijándolo en 80.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV PORT=80
EXPOSE 80

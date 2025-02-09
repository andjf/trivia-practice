FROM node:18 AS node

WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .

RUN npm run build --omit=dev

FROM nginx:latest

COPY --from=node /app/dist/ui/browser /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

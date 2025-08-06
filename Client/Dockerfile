# Stage 1: Angular Build
FROM node:18-alpine as build
WORKDIR /app

# Step 1: Copy only package files first to leverage Docker cache
COPY package.json package-lock.json ./
RUN npm install --legacy-peer-deps

# Step 2: Now copy the rest of the app (excluding node_modules via .dockerignore)
COPY . .

# Step 3: Build Angular app
RUN npm run build:prod

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist/client-v2/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

 
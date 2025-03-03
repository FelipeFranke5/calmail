FROM nginx:latest
WORKDIR /etc/nginx
COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
RUN mkdir -p /etc/nginx/conf.d/
COPY ./nginx/conf.d/dev.conf /etc/nginx/conf.d/default.conf
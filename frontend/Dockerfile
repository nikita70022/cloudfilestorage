FROM nginx:1.28-alpine

#RUN rm -f /etc/nginx/conf.d/default.conf
RUN rm -rf /usr/share/nginx/html/*

#COPY default.conf /etc/nginx/conf.d/default.conf
COPY ./assets /usr/share/nginx/html/assets
COPY config.js /usr/share/nginx/html/
COPY index.html /usr/share/nginx/html/

RUN nginx -t

#EXPOSE 3030

CMD ["nginx", "-g", "daemon off;"]
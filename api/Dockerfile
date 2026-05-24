FROM php:8.2-apache

RUN docker-php-ext-install pdo pdo_mysql

# Habilitar mod_rewrite para APIs se necessário
RUN a2enmod rewrite

COPY . /var/www/html/

EXPOSE 80
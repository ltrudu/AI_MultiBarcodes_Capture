# Unified Docker Image for AI MultiBarcode Capture
# Contains Apache+PHP, MySQL, and phpMyAdmin in a single container

FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install all required packages
RUN apt-get update && apt-get install -y \
    apache2 \
    php8.1 \
    php8.1-mysql \
    libapache2-mod-php8.1 \
    php8.1-curl \
    php8.1-cli \
    php8.1-mbstring \
    php8.1-xml \
    php8.1-zip \
    mysql-server \
    mysql-client \
    wget \
    curl \
    supervisor \
    nano \
    && rm -rf /var/lib/apt/lists/*

# Install phpMyAdmin
RUN wget https://files.phpmyadmin.net/phpMyAdmin/5.2.1/phpMyAdmin-5.2.1-all-languages.tar.gz \
    && tar xzf phpMyAdmin-5.2.1-all-languages.tar.gz \
    && mv phpMyAdmin-5.2.1-all-languages /var/www/html/phpmyadmin \
    && rm phpMyAdmin-5.2.1-all-languages.tar.gz

# Configure Apache
RUN a2enmod rewrite headers expires env
COPY apache/000-default.conf /etc/apache2/sites-available/000-default.conf

# Configure phpMyAdmin
COPY phpmyadmin/config.inc.php /var/www/html/phpmyadmin/config.inc.php

# Configure MySQL
COPY mysql/my.cnf /etc/mysql/conf.d/custom.cnf

# Copy application source code
COPY src/ /var/www/html/

# Copy database initialization script
COPY database/init.sql /tmp/init.sql

# Create necessary directories first
RUN mkdir -p /scripts

# Copy startup script and fix line endings
COPY startup.sh /startup.sh
RUN sed -i 's/\r$//' /startup.sh && chmod +x /startup.sh

# Create necessary directories and set permissions
RUN mkdir -p /var/run/mysqld \
    && mkdir -p /var/log/supervisor \
    && mkdir -p /var/log/mysql \
    && mkdir -p /var/log/apache2 \
    && mkdir -p /scripts \
    && chown mysql:mysql /var/run/mysqld \
    && chown mysql:mysql /var/lib/mysql \
    && chown -R www-data:www-data /var/www/html \
    && chmod -R 755 /var/www/html

# Initialize MySQL data directory (if not already done)
RUN service mysql start && sleep 5 && service mysql stop || true

# Environment variables with defaults
ENV MYSQL_ROOT_PASSWORD=
ENV MYSQL_DATABASE=barcode_wms
ENV MYSQL_USER=wms_user
ENV MYSQL_PASSWORD=wms_password

# Expose ports (will be dynamic based on WEB_PORT)
EXPOSE 3500

# Start the services
CMD ["/startup.sh"]
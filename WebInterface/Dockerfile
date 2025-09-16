FROM php:8.2-apache

# Install system dependencies
RUN apt-get update && apt-get install -y \
    git \
    curl \
    libpng-dev \
    libonig-dev \
    libxml2-dev \
    zip \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install PHP extensions
RUN docker-php-ext-install pdo_mysql mbstring exif pcntl bcmath gd

# Enable Apache modules
RUN a2enmod rewrite headers expires ssl

# Copy custom Apache configuration
COPY apache/000-default.conf /etc/apache2/sites-available/000-default.conf
COPY apache/ssl-default.conf /etc/apache2/sites-available/ssl-default.conf

# Generate SSL certificates inside container
RUN mkdir -p /etc/ssl/certs /etc/ssl/private && \
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/wms.key \
    -out /etc/ssl/certs/wms.crt \
    -subj "/C=US/ST=State/L=City/O=WMS/CN=wms.local" \
    -addext "subjectAltName=DNS:localhost,DNS:wms.local,IP:127.0.0.1,IP:192.168.1.188" && \
    chmod 600 /etc/ssl/private/wms.key && \
    chmod 644 /etc/ssl/certs/wms.crt

# Enable SSL site
RUN a2ensite ssl-default

# Set working directory
WORKDIR /var/www/html

# Copy application source
COPY src/ /var/www/html/

# Set proper permissions
RUN chown -R www-data:www-data /var/www/html \
    && chmod -R 755 /var/www/html

# Expose port 80
EXPOSE 80

CMD ["apache2-foreground"]
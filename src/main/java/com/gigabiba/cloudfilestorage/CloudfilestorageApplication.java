package com.gigabiba.cloudfilestorage;

import com.gigabiba.cloudfilestorage.storage.minio.config.MinioConfig;
import com.gigabiba.cloudfilestorage.storage.minio.properties.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.*;

@SpringBootApplication
public class CloudfilestorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudfilestorageApplication.class, args);
	}

}

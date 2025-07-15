package taeyun.malanalter.config

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import taeyun.malanalter.config.property.MinioProperties

@Configuration
class MinioConfig(val minioProperties: MinioProperties) {

    @Bean
    fun minioClient(properties: MinioProperties): MinioClient {
        return MinioClient.builder()
            .endpoint(minioProperties.url)
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .build()
    }
}
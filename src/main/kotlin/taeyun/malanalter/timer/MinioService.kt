package taeyun.malanalter.timer

import io.github.oshai.kotlinlogging.KotlinLogging
import io.minio.MinioClient
import io.minio.PutObjectArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import taeyun.malanalter.config.property.MinioProperties
import java.util.*

private val logger =KotlinLogging.logger {  }

@Service
class MinioService(val minioClient: MinioClient, val properties: MinioProperties) {

    suspend fun uploadFile(file: MultipartFile) = withContext(Dispatchers.IO) {
        val fileName = generateFileName(file.originalFilename ?: "unknown")
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(properties.bucketName)
                    .`object`(fileName)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build()
            )
            logger.info { "파일 업로드 성공 파일 이름 : $fileName" }
        } catch (e: Exception) {
            logger.error { "파일 업로드 실패 ${e.message}" }
        }

    }

    private fun generateFileName(originalFileName: String): String {
        val extension = originalFileName.substringAfterLast(".", "")
        val nameWithoutExtension = originalFileName.substringBeforeLast(".")
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return if (extension.isNotEmpty()) {
            "${timestamp}_${nameWithoutExtension}_${uuid}.$extension"
        } else {
            "${timestamp}_${nameWithoutExtension}_${uuid}"
        }
    }
}
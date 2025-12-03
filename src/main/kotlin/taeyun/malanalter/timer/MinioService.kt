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

    suspend fun uploadFile(file: MultipartFile, isSuccess: Boolean) = withContext(Dispatchers.IO) {
        val pathPrefix = if (isSuccess) "success/" else "failure/"
        val fileName = generateFileName(file.originalFilename ?: "unknown")
        val objectName = pathPrefix + fileName
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(properties.imageBucketName)
                    .`object`(objectName)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build()
            )
            if (!isSuccess) {
                logger.info { "추론 실패 이미지 저장 : $fileName" }
            }
        } catch (e: Exception) {
            logger.error { "파일 업로드 실패 ${e.message}" }
        }

    }

    /**
     * Uploads JSON data as a file to MinIO
     * @param jsonContent The JSON string content to upload
     * @param fileName The base name for the file (timestamp and UUID will be appended)
     * @param folder The folder path in the bucket (e.g., "party-archives/")
     */
    suspend fun uploadJsonData(jsonContent: String, fileName: String, folder: String = "party-archives/") = withContext(Dispatchers.IO) {
        val generatedFileName = generateFileName("$fileName.json")
        val objectName = folder + generatedFileName
        try {
            val contentBytes = jsonContent.toByteArray(Charsets.UTF_8)
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(properties.chatBucketName)
                    .`object`(objectName)
                    .stream(contentBytes.inputStream(), contentBytes.size.toLong(), -1)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .build()
            )
            logger.info { "JSON data uploaded successfully: $objectName" }
            objectName
        } catch (e: Exception) {
            logger.error(e) { "Failed to upload JSON data: $objectName" }
            throw e
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
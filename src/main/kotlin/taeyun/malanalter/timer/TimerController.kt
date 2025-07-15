package taeyun.malanalter.timer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


private val logger = KotlinLogging.logger {  }
@RestController
class TimerController(val minioService: MinioService) {

    @PostMapping("/timer/image", consumes = arrayOf(MediaType.MULTIPART_FORM_DATA_VALUE))
    suspend fun uploadImage(@RequestParam("image") image: MultipartFile): ResponseEntity<String> {
        minioService.uploadFile(image)
        return ResponseEntity.ok("Image uploaded successfully")
    }
}
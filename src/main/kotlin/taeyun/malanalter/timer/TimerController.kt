package taeyun.malanalter.timer

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class TimerController(val minioService: MinioService) {

    @PostMapping("/timer/image", consumes = arrayOf(MediaType.MULTIPART_FORM_DATA_VALUE))
    suspend fun uploadImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam("inferenceSuccess") isSuccess: Boolean
    ): ResponseEntity<String> {
        minioService.uploadFile(image, isSuccess)
        return ResponseEntity.ok("Image uploaded successfully")
    }
}
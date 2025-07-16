package taeyun.malanalter.timer

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import taeyun.malanalter.timer.preset.PresetService
import taeyun.malanalter.timer.preset.dto.PresetDto

@RestController
class TimerController(val minioService: MinioService, val presetService: PresetService) {

    /**
     * 유저가 추출 요청한 이미지를 코루틴으로 minio에 저장하는 로직
     * uploadFile이 Dispatcher.IO 쓰레드에 동작하면서 nio thread는 반환된다.
     */
    @PostMapping("/timer/image", consumes = arrayOf(MediaType.MULTIPART_FORM_DATA_VALUE))
    suspend fun uploadImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam("inferenceSuccess") isSuccess: Boolean
    ): ResponseEntity<String> {
        minioService.uploadFile(image, isSuccess)
        return ResponseEntity.ok("Image uploaded successfully")
    }


    @GetMapping("/api/preset")
    fun getUserPresetList(): List<PresetDto> {
        return presetService.getUserPreset()
    }
}
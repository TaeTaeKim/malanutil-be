package taeyun.malanalter.party.chat

import org.springframework.web.bind.annotation.*

@RequestMapping("/party/chat")
@RestController
class PartyChatController(
    private val partyChatService: PartyChatService
) {

    @GetMapping
    fun getPartyChat() : List<PartyChatDto>{
        return partyChatService.getUserPartyChat()

    }

    @PostMapping
    fun sendMessage(@RequestBody partyChatMessage: PartyChatMessage){
        return partyChatService.sendMessage(partyChatMessage.message)

    }

}
package taeyun.malanalter.config.exception

import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.ZoneId

data class ErrorNotification(
    val content: String="# 🚨에러 발생",
    val embeds: List<Embed>
) {

    companion object{

        fun fromException(ex: Exception) : ErrorNotification{
            val embed = Embed(description = toDiscordErrorAlertContent(ex))
            return ErrorNotification(embeds= listOf(embed))
        }

        fun toDiscordErrorAlertContent(ex:Exception): String{
            val sb = StringBuilder()
            sb.append("### 발생시간\n")
            sb.append("${LocalDateTime.now(ZoneId.of("Asia/Seoul"))}\n")
            sb.append("### 에러메세지 : ${ex.message}\n")
            if (ex is AlerterServerError && ex.rootCause != null) {
                sb.append("### 에러: ${ex.rootCause.javaClass.simpleName}")
            } else {
                sb.append("### 에러: ${ex.javaClass.simpleName}")
            }
            sb.append("### Stack Trace\n")
            sb.append("```\n")
            sb.append(getStackTrace(ex).substring(0,1000))
            sb.append("```")

            return sb.toString()
        }

        private fun   getStackTrace(e: Exception): String {
            val stringWriter =  StringWriter();
            e.printStackTrace(PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }
    data class Embed(
        val title: String = "에러정보",
        val description: String
    )
}

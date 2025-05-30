package taeyun.malanalter.schema

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SchemaValidator : ApplicationRunner {


    override fun run(args: ApplicationArguments?) {
        schemaCreation(AlertItemTable)

    }

    private fun schemaCreation(alertItemTable: AlertItemTable) {
        // check local profile activated
        System.getProperty("spring.profiles.active")?.let { profile ->
            if (profile != "local") {
                println("Schema validation is only performed in local profile.")
                return
            }
        }
        transaction {
            println("Checking if the database schema is valid...")
            // 데이터베이스에 테이블이 존재하는지 확인하고, 없으면 생성
            val listTables = SchemaUtils.listTables()
            if(listTables.isEmpty()|| !listTables.contains("public."+alertItemTable.tableName)) {
                println("Creating table: ${alertItemTable.tableName}")
                SchemaUtils.create(alertItemTable)
            } else {
                println("Table ${alertItemTable.tableName} already exists.")
            }
        }
    }


}
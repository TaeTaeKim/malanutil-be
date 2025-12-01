package taeyun.malanalter.party.pat

/**
 * PartyLeaderService 동시성 테스트
 *
 * PostgreSQL 전용 테스트입니다.
 * @ExposedTest 애노테이션이 localhost:5433의 PostgreSQL 데이터베이스에 연결합니다.
 *
 * 테스트 시나리오:
 * 1. 같은 유저를 두 개의 다른 포지션에 동시에 수락할 때 하나만 성공해야 함
 * 2. Partial Unique Index (unique_assigned_user_completed)가 동시성 문제를 방지함
 *
 * 사전 요구사항:
 * - PostgreSQL이 localhost:5433에서 실행 중이어야 함
 *   docker run --name test-postgres -p 5433:5432 -e POSTGRES_PASSWORD=test postgres
 */
// todo : Test container 를 구성해야 gitaction 에서 정상적으로 동작할 수 있음
//@ExposedTest
//@Import(
//    PartyLeaderService::class,
//    PartyLeaderServiceConcurrencyTest.MockConfig::class
//)
//class PartyLeaderServiceConcurrencyTest(
//    private val partyLeaderService: PartyLeaderService,
//    private val partyRedisService: PartyRedisService
//) : FunSpec({
//
//    beforeSpec {
//
//
//        // Mock Redis to prevent actual publishing
//        every { partyRedisService.publishMessage(any(), any()) } returns Unit
//    }
//
//    test("동시에 같은 유저를 다른 포지션에 수락할 때 하나만 성공해야 한다") {
//        // Given: 테스트 데이터 설정
//        val testUserId = 1L
//        val testLeaderId = 2L
//        val testCharacterId = "char-1234"
//        val testLeaderCharacterId = "char-5678"
//
//        val (userId, characterId, partyId, position1Id, position2Id, applicant1Id, applicant2Id) =
//            transaction {
//                // 1. 테스트 유저 생성
//                val userId = Users.insertAndGetId {
//                    it[id] = testUserId
//                    it[username] = "TestUser"
//                }.value
//
//                // 2. 파티 리더 생성
//                val leaderId = Users.insertAndGetId {
//                    it[id] = testLeaderId
//                    it[username] = "LeaderUser"
//                }.value
//
//                // 3. 테스트 유저의 캐릭터 생성
//                val characterId = CharacterTable.insertAndGetId {
//                    it[id] = testCharacterId
//                    it[CharacterTable.userId] = userId
//                    it[name] = "TestCharacter"
//                    it[level] = 250
//                    it[job] = "히어로"
//                    it[comment] = "테스트 캐릭터"
//                }.value
//
//                // 4. 리더의 캐릭터 생성
//                val leaderCharacterId = CharacterTable.insertAndGetId {
//                    it[id] = testLeaderCharacterId
//                    it[CharacterTable.userId] = leaderId
//                    it[name] = "LeaderCharacter"
//                    it[level] = 250
//                    it[job] = "팔라딘"
//                    it[comment] = "리더 테스트 캐릭터"
//                }.value
//
//                // 5. 파티 생성
//                val partyId = UUID.randomUUID().toString()
//                PartyTable.insert {
//                    it[id] = partyId
//                    it[mapId] = 1L
//                    it[hasPosition] = true
//                    it[description] = "Test Party"
//                    it[channel] = "123"
//                    it[numPeople] = 2
//                    it[PartyTable.leaderId] = leaderId
//                    it[leaderCharacter] = leaderCharacterId
//                    it[discordNotification] = false
//                    it[status] = PartyStatus.RECRUITING
//                }
//
//                // 6. 두 개의 RECRUITING 포지션 생성
//                val position1Id = UUID.randomUUID().toString()
//                val position2Id = UUID.randomUUID().toString()
//
//                PositionTable.insert {
//                    it[id] = position1Id
//                    it[PositionTable.partyId] = partyId
//                    it[name] = "Position 1"
//                    it[status] = PositionStatus.RECRUITING
//                    it[orderNumber] = 0
//                }
//
//                PositionTable.insert {
//                    it[id] = position2Id
//                    it[PositionTable.partyId] = partyId
//                    it[name] = "Position 2"
//                    it[status] = PositionStatus.RECRUITING
//                    it[orderNumber] = 1
//                }
//
//                // 7. 동일한 유저가 두 포지션 모두에 지원
//                val applicant1Id = ApplicantTable.insertAndGetId {
//                    it[ApplicantTable.partyId] = partyId
//                    it[positionId] = position1Id
//                    it[ApplicantTable.characterId] = characterId
//                    it[applyUserId] = userId
//                }.value
//
//                val applicant2Id = ApplicantTable.insertAndGetId {
//                    it[ApplicantTable.partyId] = partyId
//                    it[positionId] = position2Id
//                    it[ApplicantTable.characterId] = characterId
//                    it[applyUserId] = userId
//                }.value
//
//                // 테스트에 필요한 모든 ID 반환
//                TestData(userId, characterId, partyId, position1Id, position2Id, applicant1Id, applicant2Id)
//            }
//
//        // When: 두 코루틴이 동시에 같은 유저를 다른 포지션에 수락 시도
//        val latch = CountDownLatch(2) // 두 코루틴이 동시에 시작하도록 동기화
//        val successCount = AtomicInteger(0)
//        val failureCount = AtomicInteger(0)
//        val exceptions = mutableListOf<Exception>()
//
//        val request1 = ApplyAcceptReq(
//            applyId = applicant1Id.toString(),
//            partyId = partyId,
//            positionId = position1Id,
//            applicantUserId = userId.toString(),
//            applicantCharacterId = characterId,
//            mapId = 1L
//        )
//
//        val request2 = ApplyAcceptReq(
//            applyId = applicant2Id.toString(),
//            partyId = partyId,
//            positionId = position2Id,
//            applicantUserId = userId.toString(),
//            applicantCharacterId = characterId,
//            mapId = 1L
//        )
//
//        // 동시 실행 테스트
//        runBlocking {
//            val job1 = launch(Dispatchers.IO) {
//                latch.countDown()
//                latch.await() // 모든 코루틴이 준비될 때까지 대기
//                try {
//                    partyLeaderService.acceptApplicant(request1)
//                    successCount.incrementAndGet()
//                } catch (e: Exception) {
//                    synchronized(exceptions) {
//                        exceptions.add(e)
//                    }
//                    failureCount.incrementAndGet()
//                }
//            }
//
//            val job2 = launch(Dispatchers.IO) {
//                latch.countDown()
//                latch.await() // 모든 코루틴이 준비될 때까지 대기
//                delay(5) // 약간의 지연으로 경쟁 상태 유도
//                try {
//                    partyLeaderService.acceptApplicant(request2)
//                    successCount.incrementAndGet()
//                } catch (e: Exception) {
//                    synchronized(exceptions) {
//                        exceptions.add(e)
//                    }
//                    failureCount.incrementAndGet()
//                }
//            }
//
//            // 두 작업 완료 대기
//            job1.join()
//            job2.join()
//        }
//
//        try {
//            // Then: 정확히 하나만 성공하고 하나는 실패해야 함
//            successCount.get() shouldBe 1
//            failureCount.get() shouldBe 1
//
//            // 실패한 요청은 PartyBadRequest 예외를 던져야 함 (USER_ALREADY_IN_PARTY)
//            exceptions.size shouldBe 1
//
//            // 데이터베이스 상태 검증
//            transaction {
//                // 유저는 정확히 하나의 포지션에만 할당되어야 함
//                val assignedPositions = PositionTable.selectAll()
//                    .where { PositionTable.assignedUserId eq userId }
//                    .count()
//
//                assignedPositions shouldBe 1
//
//                // COMPLETED 상태인 포지션도 정확히 하나여야 함
//                val completedPositions = PositionTable.selectAll()
//                    .where { PositionTable.status eq PositionStatus.COMPLETED }
//                    .count()
//
//                completedPositions shouldBe 1
//            }
//        } finally {
//            // 테스트 후 모든 데이터 삭제 (전용 테스트 데이터베이스이므로 안전)
//            transaction {
//                // Foreign key 순서에 맞춰 삭제
//                ApplicantTable.deleteAll()
//                PositionTable.deleteAll()
//                PartyTable.deleteAll()
//                CharacterTable.deleteAll()
//                Users.deleteAll()
//            }
//        }
//    }
//}) {
//    /**
//     * Mock configuration for testing
//     * Provides mocked dependencies as Spring beans
//     */
//    @TestConfiguration
//    class MockConfig {
//        @Bean
//        fun talentPoolService(): TalentPoolService = mockk<TalentPoolService>()
//
//        @Bean
//        fun partyRedisService(): PartyRedisService = mockk<PartyRedisService>(relaxed = true)
//    }
//}
//
///**
// * 테스트 데이터를 담는 데이터 클래스
// */
//private data class TestData(
//    val userId: Long,
//    val characterId: String,
//    val partyId: String,
//    val position1Id: String,
//    val position2Id: String,
//    val applicant1Id: UUID,
//    val applicant2Id: UUID
//)
//
///**
// * Kotest matcher helper for type checking
// */
//private inline fun <reified T : Any> instanceOf(): T? = null
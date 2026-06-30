package com.github.bfalmeida.photosync.ui

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import com.github.bfalmeida.photosync.service.SyncService
import org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest
class SpringComposeBridgeTest {

    @Autowired
    lateinit var syncService: SyncService

    @Test
    fun `test spring context bridge`() {
        // Simulate the Main.kt initialization
        val context = org.springframework.boot.SpringApplication.run(com.github.bfalmeida.photosync.PhotosyncApplication::class.java)
        SpringContext.setContext(context)
        
        val bean = SpringContext.getBean(SyncService::class.java)
        assertNotNull(bean, "SyncService should be retrievable via SpringContext bridge")
    }
}

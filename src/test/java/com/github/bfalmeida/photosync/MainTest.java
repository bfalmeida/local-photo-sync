package com.github.bfalmeida.photosync;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testConstructor_WithValidContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        Main main = new Main(context);
        
        // Verify the Main instance was created without exception
        assertNotNull(main);
    }

    @Test
    void testConstructor_AcceptsNullContext() {
        // Main accepts null ApplicationContext without throwing
        Main main = new Main(null);
        assertNotNull(main);
    }

    @Test
    void testExit_WithConfigurableContext_ClosesContext() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        Main main = new Main(context);
        
        // SpringApplication.exit delegates to ConfigurableApplicationContext.close()
        // when given a ConfigurableApplicationContext.
        // System.exit() is not called here because SpringApplication.exit() only calls
        // System.exit() when running with a real context and the exit code is non-zero.
        // With a mocked context, close() is called but System.exit() is never reached.
        main.exit();
        
        // Verify the context was closed
        verify(context).close();
    }

    @Test
    void testExit_WithNonConfigurableContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        Main main = new Main(context);
        
        // When context is not ConfigurableApplicationContext,
        // SpringApplication.exit still completes without throwing
        assertDoesNotThrow(() -> main.exit());
    }

    @Test
    void testExit_WithNullContext() {
        Main main = new Main(null);
        
        // SpringApplication.exit throws IllegalArgumentException when context is null
        assertThrows(IllegalArgumentException.class, () -> main.exit());
    }
}

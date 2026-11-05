package com.github.bfalmeida.photosync;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testConstructor_StoresContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        Main main = new Main(context);

        // Verify the context is actually stored in the private final field
        assertSame(context, ReflectionTestUtils.getField(main, "context"));
    }

    @Test
    void testExit_WithConfigurableContext_ClosesContext() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        Main main = new Main(context);

        // SpringApplication.exit() never calls System.exit(); it computes an exit code,
        // closes the context if it is a ConfigurableApplicationContext, and returns the code.
        // Main.exit() discards the return value, so this call does not terminate the JVM.
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

    @Test
    void testMainHasComponentAnnotation() {
        // Verify @Component is present; removing it would unregister the bean
        assertTrue(Main.class.isAnnotationPresent(
            org.springframework.stereotype.Component.class));
    }

    @Test
    void testExitHasCommandAnnotation() {
        // Verify @Command(name = "exit") is present; removing it would remove the shell command
        var method = assertDoesNotThrow(() -> Main.class.getMethod("exit"));
        var annotation = method.getAnnotation(org.springframework.shell.core.command.annotation.Command.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{"exit"}, annotation.name());
    }
}

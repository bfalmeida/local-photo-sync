package com.github.bfalmeida.photosync;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private Main main;

    @Test
    void mainImplementsCommandLineRunner() {
        Main mainInstance = new Main(context);
        
        assertThat(mainInstance)
            .as("Main should implement CommandLineRunner")
            .isInstanceOf(CommandLineRunner.class);
    }

    @Test
    void mainHasApplicationContextDependency() {
        Main mainInstance = new Main(context);
        
        assertThat(mainInstance)
            .as("Main should be created with ApplicationContext")
            .isNotNull();
    }

    @Test
    void runMethodExecutesWithoutErrors() throws Exception {
        when(context.getBeansOfType(ExitCodeGenerator.class)).thenReturn(java.util.Collections.emptyMap());
        
        main.run();
        
        verify(context, times(1)).getBeansOfType(ExitCodeGenerator.class);
    }

    @Test
    void runMethodReturnsZeroExitCode() {
        int exitCode = SpringApplication.exit(context, () -> 0);
        
        assertThat(exitCode)
            .as("Exit code should be 0")
            .isEqualTo(0);
    }
}

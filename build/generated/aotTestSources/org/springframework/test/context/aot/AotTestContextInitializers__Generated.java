package org.springframework.test.context.aot;

import com.github.bfalmeida.photosync.service.FilenameDateExtractorTest__TestContext001_ApplicationContextInitializer;
import com.github.bfalmeida.photosync.service.SyncServiceIntegrityTest__TestContext002_ApplicationContextInitializer;
import java.lang.Class;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.aot.generate.Generated;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Generated mappings for {@link AotTestContextInitializers}.
 */
@Generated
public class AotTestContextInitializers__Generated {
  public static Map<String, Supplier<ApplicationContextInitializer<? extends ConfigurableApplicationContext>>> getContextInitializers(
      ) {
    Map<String, Supplier<ApplicationContextInitializer<? extends ConfigurableApplicationContext>>> map = new HashMap<>();
    map.put("com.github.bfalmeida.photosync.service.FilenameDateExtractorTest", () -> new FilenameDateExtractorTest__TestContext001_ApplicationContextInitializer());
    map.put("com.github.bfalmeida.photosync.ui.SpringComposeBridgeTest", () -> new FilenameDateExtractorTest__TestContext001_ApplicationContextInitializer());
    map.put("com.github.bfalmeida.photosync.service.SyncServiceIntegrityTest", () -> new SyncServiceIntegrityTest__TestContext002_ApplicationContextInitializer());
    return map;
  }

  public static Map<String, Class<? extends ApplicationContextInitializer<?>>> getContextInitializerClasses(
      ) {
    Map<String, Class<? extends ApplicationContextInitializer<?>>> map = new HashMap<>();
    map.put("com.github.bfalmeida.photosync.service.FilenameDateExtractorTest", FilenameDateExtractorTest__TestContext001_ApplicationContextInitializer.class);
    map.put("com.github.bfalmeida.photosync.ui.SpringComposeBridgeTest", FilenameDateExtractorTest__TestContext001_ApplicationContextInitializer.class);
    map.put("com.github.bfalmeida.photosync.service.SyncServiceIntegrityTest", SyncServiceIntegrityTest__TestContext002_ApplicationContextInitializer.class);
    return map;
  }
}

package com.github.bfalmeida.photosync;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link PhotosyncApplication}.
 */
@Generated
public class PhotosyncApplication__BeanDefinitions {
  /**
   * Get the bean definition for 'photosyncApplication'.
   */
  public static BeanDefinition getPhotosyncApplicationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PhotosyncApplication.class);
    beanDefinition.setTargetType(PhotosyncApplication.class);
    ConfigurationClassUtils.initializeConfigurationClass(PhotosyncApplication.class);
    beanDefinition.setInstanceSupplier(PhotosyncApplication$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}

package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link MediaFileScanner}.
 */
@Generated
public class MediaFileScanner__BeanDefinitions {
  /**
   * Get the bean definition for 'mediaFileScanner'.
   */
  public static BeanDefinition getMediaFileScannerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(MediaFileScanner.class);
    beanDefinition.setInstanceSupplier(MediaFileScanner::new);
    return beanDefinition;
  }
}

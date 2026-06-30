package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link FilenameDateExtractor}.
 */
@Generated
public class FilenameDateExtractor__BeanDefinitions {
  /**
   * Get the bean definition for 'filenameDateExtractor'.
   */
  public static BeanDefinition getFilenameDateExtractorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FilenameDateExtractor.class);
    beanDefinition.setInstanceSupplier(FilenameDateExtractor::new);
    return beanDefinition;
  }
}

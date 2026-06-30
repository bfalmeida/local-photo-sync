package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ExifMetadataService}.
 */
@Generated
public class ExifMetadataService__TestContext001_BeanDefinitions {
  /**
   * Get the bean instance supplier for 'exifMetadataService'.
   */
  private static BeanInstanceSupplier<ExifMetadataService> getExifMetadataServiceInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ExifMetadataService>forConstructor(FilenameDateExtractor.class)
            .withGenerator((registeredBean, args) -> new ExifMetadataService(args.get(0)));
  }

  /**
   * Get the bean definition for 'exifMetadataService'.
   */
  public static BeanDefinition getExifMetadataServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ExifMetadataService.class);
    beanDefinition.setInstanceSupplier(getExifMetadataServiceInstanceSupplier());
    return beanDefinition;
  }
}

package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link FileCopyService}.
 */
@Generated
public class FileCopyService__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'fileCopyService'.
   */
  private static BeanInstanceSupplier<FileCopyService> getFileCopyServiceInstanceSupplier() {
    return BeanInstanceSupplier.<FileCopyService>forConstructor(HashingService.class)
            .withGenerator((registeredBean, args) -> new FileCopyService(args.get(0)));
  }

  /**
   * Get the bean definition for 'fileCopyService'.
   */
  public static BeanDefinition getFileCopyServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FileCopyService.class);
    beanDefinition.setInstanceSupplier(getFileCopyServiceInstanceSupplier());
    return beanDefinition;
  }
}

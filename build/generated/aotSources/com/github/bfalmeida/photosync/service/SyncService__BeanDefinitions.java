package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SyncService}.
 */
@Generated
public class SyncService__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'syncService'.
   */
  private static BeanInstanceSupplier<SyncService> getSyncServiceInstanceSupplier() {
    return BeanInstanceSupplier.<SyncService>forConstructor(MediaFileScanner.class, FilenameDateExtractor.class, ExifMetadataService.class, FileCopyService.class, ValkeyStateService.class, HashingService.class, int.class)
            .withGenerator((registeredBean, args) -> new SyncService(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6)));
  }

  /**
   * Get the bean definition for 'syncService'.
   */
  public static BeanDefinition getSyncServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SyncService.class);
    beanDefinition.setInstanceSupplier(getSyncServiceInstanceSupplier());
    return beanDefinition;
  }
}

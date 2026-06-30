package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.service.ValkeyStateService;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SyncViewModel}.
 */
@Generated
public class SyncViewModel__TestContext001_BeanDefinitions {
  /**
   * Get the bean instance supplier for 'syncViewModel'.
   */
  private static BeanInstanceSupplier<SyncViewModel> getSyncViewModelInstanceSupplier() {
    return BeanInstanceSupplier.<SyncViewModel>forConstructor(SyncService.class, ValkeyStateService.class)
            .withGenerator((registeredBean, args) -> new SyncViewModel(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'syncViewModel'.
   */
  public static BeanDefinition getSyncViewModelBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SyncViewModel.class);
    beanDefinition.setInstanceSupplier(getSyncViewModelInstanceSupplier());
    return beanDefinition;
  }
}

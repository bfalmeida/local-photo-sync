package org.springframework.boot.autoconfigure.data.redis;

import java.lang.Object;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * Bean definitions for {@link RedisReactiveAutoConfiguration}.
 */
@Generated
public class RedisReactiveAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'redisReactiveAutoConfiguration'.
   */
  public static BeanDefinition getRedisReactiveAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RedisReactiveAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(RedisReactiveAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'reactiveRedisTemplate'.
   */
  private static BeanInstanceSupplier<ReactiveRedisTemplate> getReactiveRedisTemplateInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ReactiveRedisTemplate>forFactoryMethod(RedisReactiveAutoConfiguration.class, "reactiveRedisTemplate", ReactiveRedisConnectionFactory.class, ResourceLoader.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(RedisReactiveAutoConfiguration.class).reactiveRedisTemplate(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'reactiveRedisTemplate'.
   */
  public static BeanDefinition getReactiveRedisTemplateBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ReactiveRedisTemplate.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(ReactiveRedisTemplate.class, Object.class, Object.class));
    beanDefinition.setInstanceSupplier(getReactiveRedisTemplateInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'reactiveStringRedisTemplate'.
   */
  private static BeanInstanceSupplier<ReactiveStringRedisTemplate> getReactiveStringRedisTemplateInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ReactiveStringRedisTemplate>forFactoryMethod(RedisReactiveAutoConfiguration.class, "reactiveStringRedisTemplate", ReactiveRedisConnectionFactory.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(RedisReactiveAutoConfiguration.class).reactiveStringRedisTemplate(args.get(0)));
  }

  /**
   * Get the bean definition for 'reactiveStringRedisTemplate'.
   */
  public static BeanDefinition getReactiveStringRedisTemplateBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ReactiveStringRedisTemplate.class);
    beanDefinition.setInstanceSupplier(getReactiveStringRedisTemplateInstanceSupplier());
    return beanDefinition;
  }
}

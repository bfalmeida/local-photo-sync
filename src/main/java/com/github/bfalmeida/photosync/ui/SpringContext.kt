package com.github.bfalmeida.photosync.ui

import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

object SpringContext {
    private var context: ApplicationContext? = null

    fun setContext(ctx: ApplicationContext) {
        context = ctx
    }

    fun <T> getBean(beanClass: Class<T>): T {
        return context?.getBean(beanClass) 
            ?: throw IllegalStateException("SpringContext not initialized. Call setContext first.")
    }

    fun <T> getBean(beanName: String, beanClass: Class<T>): T {
        return context?.getBean(beanName, beanClass)
            ?: throw IllegalStateException("SpringContext not initialized. Call setContext first.")
    }
}

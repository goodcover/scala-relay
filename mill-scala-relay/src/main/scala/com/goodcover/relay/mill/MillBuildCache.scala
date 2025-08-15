package com.goodcover.relay.mill

import com.goodcover.relay.build.{BuildCache, BuildCacheFactory}
import mill.javalib.api.JvmWorkerApi.Ctx

import java.io.File

/**
  * Mill implementation of BuildCache using Mill's caching system
  */
class MillBuildCache(name: String)(implicit ctx: Ctx) extends BuildCache {
  override def clean(): Unit = {
    // Mill handles caching automatically, so we don't need to implement explicit cleaning
    // The cache will be invalidated when inputs change
  }
}

/**
  * Mill implementation of BuildCacheFactory
  */
class MillBuildCacheFactory(implicit ctx: Ctx) extends BuildCacheFactory {
  override def apply(name: String): BuildCache = new MillBuildCache(name)
}

object MillBuildCacheFactory {
  def apply()(implicit ctx: Ctx): MillBuildCacheFactory = new MillBuildCacheFactory()
}

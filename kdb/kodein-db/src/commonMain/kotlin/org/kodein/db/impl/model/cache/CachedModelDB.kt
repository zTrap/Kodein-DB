package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.*
import org.kodein.memory.Closeable
import org.kodein.memory.cache.Sized

class CachedModelDB(override val mdb: ModelDB, override val cache: ModelCache, override val cacheCopyMaxSize: Int) : BaseCachedModelRead, ModelDB {

    private val listener = object : DBListener {
        override fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
            if (Cache.Skip in options) {
                cache.evict(getKey())
            } else {
                cache.put(getKey().asHeapKey(), model, size)
            }
        }

        override fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) {
            if (Cache.Skip in options) {
                cache.evict(key)
            } else {
                cache.delete(key)
            }
        }
    }

    init {
        register(listener)
    }

    override fun register(listener: DBListener): Closeable = mdb.register(listener)
    override fun put(model: Any, vararg options: Options.Write): Int = mdb.put(model, *options)
    override fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Sized<Key<M>> = mdb.putAndGetHeapKey(model, *options)
    override fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Sized<Key.Native<M>> = mdb.putAndGetNativeKey(model, *options)
    override fun delete(key: Key<*>, vararg options: Options.Write) = mdb.delete(key, *options)
    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)
    override fun close() = mdb.close()

    override fun newBatch(): ModelDB.Batch = mdb.newBatch()

    override fun newSnapshot(vararg options: Options.Read): ModelDB.Snapshot {
        val maxSize = maxSize(options)
        return CachedModelSnapshot(mdb.newSnapshot(*options), cache.newCopy(maxSize), maxSize)
    }
}

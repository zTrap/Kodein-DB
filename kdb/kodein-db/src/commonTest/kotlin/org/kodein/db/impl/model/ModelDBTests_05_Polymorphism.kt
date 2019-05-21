package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.findByType
import org.kodein.db.model.putAll
import org.kodein.db.orm.kotlinx.asRef
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_05_Polymorphism : ModelDBTests() {

    override fun testTypeTable() = TypeTable {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }

    @Test
    fun test01_Polymorphism() {
        val gilbert = Adult("Gilbert", "BRYS", Date(1, 9, 1954))
        val veronique = Adult("Véronique", "BRYS", Date(17, 10, 1957))
        val salomon = Child("Salomon", "BRYS", Date(15, 12, 1986), mdb.getKey(gilbert).asRef() to mdb.getKey(veronique).asRef())
        val maroussia = Child("Maroussia", "BRYS", Date(18, 8, 1988), mdb.getKey(gilbert).asRef() to mdb.getKey(veronique).asRef())
        val benjamin = Child("Benjamin", "BRYS", Date(23, 6, 1992), mdb.getKey(gilbert).asRef() to mdb.getKey(veronique).asRef())

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        mdb.findByType<Person>().use {
            assertTrue(it.isValid())
            val benji = it.model()
            assertEquals(benjamin, benji)
            assertNotSame(benjamin, benji)

            it.next()
            assertTrue(it.isValid())
            val guilou = it.model()
            assertEquals(gilbert, guilou)
            assertNotSame(gilbert, guilou)

            it.next()
            assertTrue(it.isValid())
            val yaya = it.model()
            assertEquals(maroussia, yaya)
            assertNotSame(maroussia, yaya)

            it.next()
            assertTrue(it.isValid())
            val monmon = it.model()
            assertEquals(salomon, monmon)
            assertNotSame(salomon, monmon)

            it.next()
            assertTrue(it.isValid())
            val vero = it.model()
            assertEquals(veronique, vero)
            assertNotSame(veronique, vero)

            it.next()
            assertFalse(it.isValid())
        }
    }

}

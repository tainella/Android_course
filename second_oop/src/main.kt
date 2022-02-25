import blue_duck.duck
import blue_duck.yellow_duck
import blue_duck.red_duck
import blue_duck.factory
import printer.printer

fun main() {
    val obj = tryme(5, 4)
    println(obj.hello())
    println(obj.d)

    val bld = duck(10)
    println(bld.getColor())
    val yld = yellow_duck(11)
    println(yld.getColor())

    val list = listOf(
        duck(12),
        yellow_duck(13),
        red_duck(14)
    )
    println("Наши утки")
        list.forEach { duck ->
            println (duck.getColor())
            println (duck.age)
    }

    val color = readLine()!! //!! значит не может равняться null
    val age = readLine()!!.toInt()
    val fact = factory()

    val duck = fact.create(color, age)
    println(duck.getColor())

    if (duck is red_duck) println(duck.getageandcolor())

    println("стратегии")
    val printer = printer()
    printer.printDuck(duck, "ageAndcolor")
}
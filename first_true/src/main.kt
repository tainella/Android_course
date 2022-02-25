fun main() {
    val a: Int = 9 //val статичная
    var b = 9 //может определить тип сам; 9.0 double, 9F (обязательно) float
    val list1 = listOf(1, 1, 1)
    val list: MutableList<Int> = mutableListOf(0, 0, 0, 0) //mutable изменяемый, без него статичный (тип можно не писать)
    //list нельзя переприсвоить, но можно изменять внутри, если mutable

    val text = if (a % 2 == 0)  {
        "Четное"
    }
    else {
        "Нечетное"
    }

    when(a) { //switch, можно тоже приравнять переменной
        9 -> { //скобки не обязательны
            println("9")
        }
        34 -> println("34")
        else -> println("0")
    }

    for (i in 0..10) {
        println(i)
    }

    var count = 0

    while (count < 10) {
        count += 1
        println(count)
    }

    for (i in list.indices) {
        list[i] = i
    }
    println(list)

    hello(7)

    println(hello1(9, 5F))

    println(sum(10, 1F))
}

fun hello(a: Int = 9) { //всегда указываются типы данных
    println("hello $a")
}

fun hello1(a: Int, b: Float): Float { //всегда указываются типы данных
    return a + b
}
//opt+Enter автоподвод функций

fun sum(a: Int, b: Float) = (a + b).toInt()